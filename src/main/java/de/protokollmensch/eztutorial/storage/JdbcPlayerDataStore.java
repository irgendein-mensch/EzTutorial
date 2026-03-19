package de.protokollmensch.eztutorial.storage;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.models.PlayerProgress;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcPlayerDataStore implements PlayerDataStore {

    private final EzTutorial plugin;
    private final Map<UUID, PlayerProgress> cache;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public JdbcPlayerDataStore(EzTutorial plugin, String jdbcUrl, String username, String password) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        createTableIfNeeded();
        loadCache();
    }

    private Connection createConnection() throws SQLException {
        if (username == null || username.isEmpty()) {
            return DriverManager.getConnection(jdbcUrl);
        }
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void createTableIfNeeded() {
        String sql = "CREATE TABLE IF NOT EXISTS eztutorial_players ("
            + "uuid VARCHAR(36) PRIMARY KEY,"
            + "prompted BOOLEAN NOT NULL,"
            + "active BOOLEAN NOT NULL,"
            + "completed BOOLEAN NOT NULL,"
            + "current_task INT NOT NULL"
            + ")";

        try (Connection connection = createConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException exception) {
            plugin.getLogger().severe("Could not create tutorial table: " + exception.getMessage());
        }
    }

    private void loadCache() {
        String sql = "SELECT uuid, prompted, active, completed, current_task FROM eztutorial_players";

        try (Connection connection = createConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                PlayerProgress progress = new PlayerProgress(uuid);
                progress.setPrompted(resultSet.getBoolean("prompted"));
                progress.setActive(resultSet.getBoolean("active"));
                progress.setCompleted(resultSet.getBoolean("completed"));
                progress.setCurrentTaskIndex(resultSet.getInt("current_task"));
                cache.put(uuid, progress);
            }
        } catch (SQLException exception) {
            plugin.getLogger().severe("Could not load tutorial player data: " + exception.getMessage());
        }
    }

    @Override
    public PlayerProgress getOrCreate(UUID uniqueId) {
        return cache.computeIfAbsent(uniqueId, PlayerProgress::new);
    }

    @Override
    public void save(PlayerProgress progress) {
        cache.put(progress.getUniqueId(), progress);

        String sql = "INSERT INTO eztutorial_players(uuid, prompted, active, completed, current_task) "
            + "VALUES(?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE prompted=VALUES(prompted), active=VALUES(active), completed=VALUES(completed), current_task=VALUES(current_task)";

        boolean sqlite = jdbcUrl.startsWith("jdbc:sqlite:");
        if (sqlite) {
            sql = "INSERT INTO eztutorial_players(uuid, prompted, active, completed, current_task) "
                + "VALUES(?, ?, ?, ?, ?) "
                + "ON CONFLICT(uuid) DO UPDATE SET prompted=excluded.prompted, active=excluded.active, completed=excluded.completed, current_task=excluded.current_task";
        }

        try (Connection connection = createConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, progress.getUniqueId().toString());
            statement.setBoolean(2, progress.isPrompted());
            statement.setBoolean(3, progress.isActive());
            statement.setBoolean(4, progress.isCompleted());
            statement.setInt(5, progress.getCurrentTaskIndex());
            statement.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().severe("Could not save tutorial player data: " + exception.getMessage());
        }
    }

    @Override
    public void reset(UUID uniqueId) {
        PlayerProgress progress = new PlayerProgress(uniqueId);
        save(progress);
    }

    @Override
    public void saveAll() {
        for (PlayerProgress progress : cache.values()) {
            save(progress);
        }
    }
}


package de.protokollmensch.eztutorial.storage;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.models.PlayerProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class YamlPlayerDataStore implements PlayerDataStore {

    private final EzTutorial plugin;
    private final Map<UUID, PlayerProgress> cache;
    private final File file;
    private FileConfiguration data;

    public YamlPlayerDataStore(EzTutorial plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.file = new File(plugin.getDataFolder(), "players.yml");
        loadFile();
        loadAllIntoCache();
    }

    private void loadFile() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder: " + plugin.getDataFolder().getAbsolutePath());
        }

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Could not create players.yml file: " + file.getAbsolutePath());
                }
            } catch (IOException exception) {
                plugin.getLogger().severe("Could not create players.yml: " + exception.getMessage());
            }
        }

        this.data = YamlConfiguration.loadConfiguration(file);
    }

    private void loadAllIntoCache() {
        ConfigurationSection playersSection = data.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (String key : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerProgress progress = new PlayerProgress(uuid);
                progress.setPrompted(data.getBoolean(path(uuid, "prompted"), false));
                progress.setActive(data.getBoolean(path(uuid, "active"), false));
                progress.setCompleted(data.getBoolean(path(uuid, "completed"), false));
                progress.setCurrentTaskIndex(data.getInt(path(uuid, "current-task"), 0));
                cache.put(uuid, progress);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Invalid UUID entry in players.yml: " + key);
            }
        }
    }

    @Override
    public PlayerProgress getOrCreate(UUID uniqueId) {
        return cache.computeIfAbsent(uniqueId, PlayerProgress::new);
    }

    @Override
    public void save(PlayerProgress progress) {
        UUID uuid = progress.getUniqueId();
        data.set(path(uuid, "prompted"), progress.isPrompted());
        data.set(path(uuid, "active"), progress.isActive());
        data.set(path(uuid, "completed"), progress.isCompleted());
        data.set(path(uuid, "current-task"), progress.getCurrentTaskIndex());
        saveFile();
    }

    @Override
    public void reset(UUID uniqueId) {
        PlayerProgress progress = new PlayerProgress(uniqueId);
        cache.put(uniqueId, progress);
        save(progress);
    }

    @Override
    public void saveAll() {
        for (PlayerProgress progress : cache.values()) {
            data.set(path(progress.getUniqueId(), "prompted"), progress.isPrompted());
            data.set(path(progress.getUniqueId(), "active"), progress.isActive());
            data.set(path(progress.getUniqueId(), "completed"), progress.isCompleted());
            data.set(path(progress.getUniqueId(), "current-task"), progress.getCurrentTaskIndex());
        }
        saveFile();
    }

    private String path(UUID uniqueId, String key) {
        return "players." + uniqueId + "." + key;
    }

    private void saveFile() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save players.yml: " + exception.getMessage());
        }
    }
}


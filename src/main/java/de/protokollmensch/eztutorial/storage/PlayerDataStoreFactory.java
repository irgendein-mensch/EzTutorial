package de.protokollmensch.eztutorial.storage;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.utils.Config;

import java.io.File;

public final class PlayerDataStoreFactory {

    private PlayerDataStoreFactory() {
    }

    public static PlayerDataStore create(EzTutorial plugin, Config config) {
        String type = config.getStorageType();

        if ("sqlite".equalsIgnoreCase(type)) {
            File sqliteFile = new File(plugin.getDataFolder(), config.getSqliteFile());
            String url = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();
            plugin.getLogger().info("Using SQLite storage: " + sqliteFile.getAbsolutePath());
            return new JdbcPlayerDataStore(plugin, url, "", "");
        }

        if ("mysql".equalsIgnoreCase(type)) {
            String url = "jdbc:mysql://"
                + config.getMysqlHost() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            plugin.getLogger().info("Using MySQL storage: " + config.getMysqlHost() + ":" + config.getMysqlPort());
            return new JdbcPlayerDataStore(plugin, url, config.getMysqlUsername(), config.getMysqlPassword());
        }

        plugin.getLogger().info("Using YAML storage (players.yml)");
        return new YamlPlayerDataStore(plugin);
    }
}


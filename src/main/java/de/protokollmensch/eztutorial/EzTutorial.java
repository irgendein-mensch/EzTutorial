package de.protokollmensch.eztutorial;

import de.protokollmensch.eztutorial.commands.EzTutorialCommand;
import de.protokollmensch.eztutorial.commands.TutorialCommand;
import de.protokollmensch.eztutorial.listeners.InventoryClickListener;
import de.protokollmensch.eztutorial.listeners.PlayerCommandListener;
import de.protokollmensch.eztutorial.listeners.PlayerJoinListener;
import de.protokollmensch.eztutorial.listeners.PlayerMoveListener;
import de.protokollmensch.eztutorial.services.TutorialService;
import de.protokollmensch.eztutorial.storage.PlayerDataStore;
import de.protokollmensch.eztutorial.storage.PlayerDataStoreFactory;
import de.protokollmensch.eztutorial.gui.TutorialGUI;
import de.protokollmensch.eztutorial.utils.Config;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzTutorial extends JavaPlugin {

    private Config config;
    private PlayerDataStore playerDataStore;
    private TutorialService tutorialService;
    private TutorialGUI tutorialGUI;

    @Override
    public void onEnable() {
        this.config = new Config(this);
        config.load();
        this.playerDataStore = PlayerDataStoreFactory.create(this, config);
        this.tutorialService = new TutorialService(this, config, playerDataStore);
        this.tutorialGUI = new TutorialGUI(this, tutorialService);

        tutorialService.setTutorialGUI(tutorialGUI);

        registerCommands();
        registerListeners();

        getLogger().info("[EzTutorial] Plugin successfully enabled");
    }

    @Override
    public void onDisable() {
        if (playerDataStore != null) {
            playerDataStore.saveAll();
            playerDataStore.close();
        }
    }

    private void registerCommands() {
        PluginCommand ezTutorialCommand = getCommand("eztutorial");
        if (ezTutorialCommand != null) {
            EzTutorialCommand command = new EzTutorialCommand(this, tutorialService);
            ezTutorialCommand.setExecutor(command);
            ezTutorialCommand.setTabCompleter(command);
        } else {
            getLogger().severe("Command 'eztutorial' is missing in plugin.yml");
        }

        PluginCommand tutorialCommand = getCommand("tutorial");
        if (tutorialCommand != null) {
            TutorialCommand command = new TutorialCommand(this, tutorialService);
            tutorialCommand.setExecutor(command);
            tutorialCommand.setTabCompleter(command);
        } else {
            getLogger().severe("Command 'tutorial' is missing in plugin.yml");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(tutorialService), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(tutorialService), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(tutorialService), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(tutorialGUI), this);
    }

    public Config getConfigManager() {
        return config;
    }

    public TutorialService getTutorialService() {
        return tutorialService;
    }

    public void reloadPlugin() {
        config.reload();
        if (tutorialGUI != null) {
            tutorialGUI.refreshOpenTutorialInventories();
        }
    }
}
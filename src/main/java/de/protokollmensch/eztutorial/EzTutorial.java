package de.protokollmensch.eztutorial;

import org.bukkit.plugin.java.JavaPlugin;

public final class EzTutorial extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("§b[EzTutorial] Plugin successfully enabled");
    }

    @Override
    public void onDisable() {
    }
}

package de.protokollmensch.eztutorial.utils;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.models.DisplayMode;
import de.protokollmensch.eztutorial.models.TaskType;
import de.protokollmensch.eztutorial.models.TutorialTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");

    private final EzTutorial plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration guiConfig;
    private FileConfiguration tutorialConfig;
    private FileConfiguration storageConfig;

    public Config(EzTutorial plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.mainConfig = loadFile("config.yml");
        this.messagesConfig = loadFile("messages.yml");
        this.guiConfig = loadFile("gui.yml");
        this.tutorialConfig = loadFile("tutorial.yml");
        this.storageConfig = loadFile("storage.yml");
    }

    public void reload() {
        load();
    }

    public void save() {
        saveFile(mainConfig, "config.yml");
        saveFile(messagesConfig, "messages.yml");
        saveFile(guiConfig, "gui.yml");
        saveFile(tutorialConfig, "tutorial.yml");
        saveFile(storageConfig, "storage.yml");
    }

    public String getStorageType() {
        return storageConfig.getString("storage.type", "yml").toLowerCase();
    }

    public DisplayMode getTaskStartDisplayMode() {
        return DisplayMode.fromString(mainConfig.getString("display.task-start.mode", "ACTIONBAR"), DisplayMode.ACTIONBAR);
    }

    public DisplayMode getTaskCompleteDisplayMode() {
        return DisplayMode.fromString(mainConfig.getString("display.task-complete.mode", "ACTIONBAR"), DisplayMode.ACTIONBAR);
    }

    public String getActionbarTaskStartText() {
        return color(mainConfig.getString("display.actionbar.task-start", "&bTask %task_current%/%task_total%: &f%task_description%"));
    }

    public String getActionbarTaskCompleteText() {
        return color(mainConfig.getString("display.actionbar.task-complete", "&aTask completed!"));
    }

    public String getTitleTaskStartTitle() {
        return color(mainConfig.getString("display.title.task-start.title", "&bTask %task_current%/%task_total%"));
    }

    public String getTitleTaskStartSubtitle() {
        return color(mainConfig.getString("display.title.task-start.subtitle", "%task_description%"));
    }

    public String getTitleTaskCompleteTitle() {
        return color(mainConfig.getString("display.title.task-complete.title", "&aTask completed"));
    }

    public String getTitleTaskCompleteSubtitle() {
        return color(mainConfig.getString("display.title.task-complete.subtitle", "&7Moving to the next task"));
    }

    public String getScoreboardTitle() {
        return color(mainConfig.getString("display.scoreboard.title", "&b&lTutorial"));
    }

    public List<String> getScoreboardTaskStartLines() {
        return colorize(mainConfig.getStringList("display.scoreboard.task-start-lines"));
    }

    public List<String> getScoreboardTaskCompleteLines() {
        return colorize(mainConfig.getStringList("display.scoreboard.task-complete-lines"));
    }

    public String getMysqlHost() {
        return storageConfig.getString("storage.mysql.host", "127.0.0.1");
    }

    public int getMysqlPort() {
        return storageConfig.getInt("storage.mysql.port", 3306);
    }

    public String getMysqlDatabase() {
        return storageConfig.getString("storage.mysql.database", "eztutorial");
    }

    public String getMysqlUsername() {
        return storageConfig.getString("storage.mysql.username", "root");
    }

    public String getMysqlPassword() {
        return storageConfig.getString("storage.mysql.password", "");
    }

    public String getSqliteFile() {
        return storageConfig.getString("storage.sqlite.file", "players.db");
    }

    public boolean isGuiEnabled() {
        return tutorialConfig.getBoolean("tutorial.gui-enabled", true);
    }

    public boolean isAutoPromptFirstJoin() {
        return tutorialConfig.getBoolean("tutorial.auto-prompt-first-join", true);
    }

    public boolean isAutoStartFirstJoin() {
        return tutorialConfig.getBoolean("tutorial.auto-start-first-join", false);
    }


    public boolean isOpenGuiOnTutorialCommand() {
        return tutorialConfig.getBoolean("tutorial.open-gui-on-tutorial-command", true);
    }

    public String getBypassPermission() {
        return tutorialConfig.getString("tutorial.bypass-permission", "eztutorial.bypass");
    }

    public String getSkipPermission() {
        return tutorialConfig.getString("tutorial.skip-permission", "eztutorial.skip");
    }

    public List<String> getPromptMessages() {
        return colorize(messagesConfig.getStringList("messages.first-join-prompt"));
    }

    public List<String> getHelpMessages() {
        return colorize(messagesConfig.getStringList("messages.help"));
    }

    public List<String> getTaskStartMessages() {
        return colorize(messagesConfig.getStringList("messages.task-start"));
    }

    public List<String> getTaskCompleteMessages() {
        return colorize(messagesConfig.getStringList("messages.task-complete"));
    }

    public List<String> getTutorialCompleteMessages() {
        return colorize(messagesConfig.getStringList("messages.tutorial-complete"));
    }

    public String getAlreadyCompletedMessage() {
        return color(messagesConfig.getString("messages.already-completed", "&aYou already completed the tutorial."));
    }

    public String getNoActiveTutorialMessage() {
        return color(messagesConfig.getString("messages.no-active-tutorial", "&cYou do not have an active tutorial right now."));
    }

    public String getReloadedMessage() {
        return color(messagesConfig.getString("messages.reloaded", "&aAll config files were reloaded."));
    }

    public String getPlayerResetMessage() {
        return color(messagesConfig.getString("messages.player-reset", "&aPlayer tutorial progress has been reset."));
    }

    public String getPlayerStartedMessage() {
        return color(messagesConfig.getString("messages.player-started", "&aTutorial has been started for the player."));
    }

    public String getSkipNotAllowedMessage() {
        return color(messagesConfig.getString("messages.skip-not-allowed", "&cYou are not allowed to skip the tutorial."));
    }

    public String getGuiTitle() {
        return color(guiConfig.getString("gui.title", "&b&lTutorial"));
    }

    public int getGuiRows() {
        return Math.max(1, Math.min(6, guiConfig.getInt("gui.rows", 6)));
    }

    public Material getGuiFillerItem() {
        return getMaterial(guiConfig.getString("gui.filler-item", "GRAY_STAINED_GLASS_PANE"), Material.GRAY_STAINED_GLASS_PANE);
    }

    public Material getGuiHeaderItem() {
        return getMaterial(guiConfig.getString("gui.header-item.material", "NETHER_STAR"), Material.NETHER_STAR);
    }

    public String getGuiHeaderName() {
        return color(guiConfig.getString("gui.header-item.name", "&b&lTutorial Overview"));
    }

    public List<String> getGuiHeaderLore() {
        return colorize(guiConfig.getStringList("gui.header-item.lore"));
    }

    public int getGuiHeaderSlot() {
        return Math.max(0, Math.min(getGuiRows() * 9 - 1, guiConfig.getInt("gui.header-item.slot", 4)));
    }

    public Material getGuiTaskItemOpen() {
        return getMaterial(guiConfig.getString("gui.task-item.open", "BOOK"), Material.BOOK);
    }

    public Material getGuiTaskItemDone() {
        return getMaterial(guiConfig.getString("gui.task-item.done", "LIME_DYE"), Material.LIME_DYE);
    }

    public Material getGuiCloseItem() {
        return getMaterial(guiConfig.getString("gui.close-item.material", "BARRIER"), Material.BARRIER);
    }

    public String getGuiCloseName() {
        return color(guiConfig.getString("gui.close-item.name", "&cClose"));
    }

    public List<String> getGuiCloseLore() {
        return colorize(guiConfig.getStringList("gui.close-item.lore"));
    }

    public Material getGuiStartItem() {
        return getMaterial(guiConfig.getString("gui.start-item.material", "EMERALD"), Material.EMERALD);
    }

    public String getGuiStartName() {
        return color(guiConfig.getString("gui.start-item.name", "&aStart / Resume"));
    }

    public List<String> getGuiStartLore() {
        return colorize(guiConfig.getStringList("gui.start-item.lore"));
    }

    public int getGuiCloseSlot() {
        return Math.max(0, Math.min(getGuiRows() * 9 - 1, guiConfig.getInt("gui.close-item.slot", getGuiRows() * 9 - 1)));
    }

    public int getGuiStartSlot() {
        return Math.max(0, Math.min(getGuiRows() * 9 - 1, guiConfig.getInt("gui.start-item.slot", getGuiRows() * 9 - 9)));
    }

    public boolean isGuiSkipButtonEnabled() {
        return guiConfig.getBoolean("gui.skip-item.enabled", true);
    }

    public Material getGuiSkipItem() {
        return getMaterial(guiConfig.getString("gui.skip-item.material", "FEATHER"), Material.FEATHER);
    }

    public String getGuiSkipName() {
        return color(guiConfig.getString("gui.skip-item.name", "&6Skip Tutorial"));
    }

    public List<String> getGuiSkipLore() {
        return colorize(guiConfig.getStringList("gui.skip-item.lore"));
    }

    public int getGuiSkipSlot() {
        return Math.max(0, Math.min(getGuiRows() * 9 - 1, guiConfig.getInt("gui.skip-item.slot", getGuiRows() * 9 - 5)));
    }

    public List<Integer> getGuiTaskSlots() {
        List<Integer> slots = new ArrayList<>(guiConfig.getIntegerList("gui.task-slots"));
        int maxSlot = getGuiRows() * 9 - 1;
        slots.removeIf(slot -> slot < 0 || slot > maxSlot);
        return slots;
    }

    public List<String> getCompletionCommands() {
        return tutorialConfig.getStringList("completion.commands");
    }

    public List<TutorialTask> getTutorialTasks() {
        List<Map<?, ?>> maps = tutorialConfig.getMapList("tutorial.tasks");
        if (maps.isEmpty()) {
            return Collections.emptyList();
        }

        List<TutorialTask> tasks = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            Map<?, ?> map = maps.get(i);
            String id = asString(map.get("id"), "task-" + (i + 1));
            TaskType type = TaskType.fromString(asString(map.get("type"), "MOVE_TO"));
            String description = color(asString(map.get("description"), "&7No description set."));
            String world = asString(map.get("world"), "world");
            double x = asDouble(map.get("x"), 0.0D);
            double y = asDouble(map.get("y"), 64.0D);
            double z = asDouble(map.get("z"), 0.0D);
            double radius = asDouble(map.get("radius"), 3.0D);
            String command = asString(map.get("command"), "rules");
            tasks.add(new TutorialTask(id, type, description, world, x, y, z, radius, command));
        }
        return tasks;
    }

    public String color(String text) {
        String input = text == null ? "" : text;
        Matcher matcher = HEX_COLOR_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group();
            String replacement;
            try {
                replacement = net.md_5.bungee.api.ChatColor.of(hex).toString();
            } catch (IllegalArgumentException exception) {
                replacement = hex;
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);

        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    private List<String> colorize(List<String> raw) {
        List<String> lines = new ArrayList<>(raw.size());
        for (String line : raw) {
            lines.add(color(line));
        }
        return lines;
    }

    private Material getMaterial(String raw, Material fallback) {
        try {
            return Material.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return fallback;
        }
    }

    private FileConfiguration loadFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveFile(FileConfiguration fileConfiguration, String fileName) {
        if (fileConfiguration == null) {
            return;
        }

        try {
            fileConfiguration.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save " + fileName + ": " + exception.getMessage());
        }
    }

    private String asString(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private double asDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
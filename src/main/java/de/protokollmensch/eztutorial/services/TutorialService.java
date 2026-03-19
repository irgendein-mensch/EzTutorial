package de.protokollmensch.eztutorial.services;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.gui.TutorialGUI;
import de.protokollmensch.eztutorial.models.DisplayMode;
import de.protokollmensch.eztutorial.models.PlayerProgress;
import de.protokollmensch.eztutorial.models.TaskType;
import de.protokollmensch.eztutorial.models.TutorialTask;
import de.protokollmensch.eztutorial.storage.PlayerDataStore;
import de.protokollmensch.eztutorial.utils.Config;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TutorialService {

    private final EzTutorial plugin;
    private final Config config;
    private final PlayerDataStore dataStore;
    private TutorialGUI tutorialGUI;

    public TutorialService(EzTutorial plugin, Config config, PlayerDataStore dataStore) {
        this.plugin = plugin;
        this.config = config;
        this.dataStore = dataStore;
    }

    public void setTutorialGUI(TutorialGUI tutorialGUI) {
        this.tutorialGUI = tutorialGUI;
    }

    public void handleJoin(Player player) {
        if (player.hasPermission(config.getBypassPermission())) {
            return;
        }

        PlayerProgress progress = getProgress(player.getUniqueId());

        if (!progress.isPrompted() && config.isAutoPromptFirstJoin()) {
            sendLines(player, config.getPromptMessages());
            progress.setPrompted(true);
            dataStore.save(progress);
        }

        if (!progress.isCompleted() && !progress.isActive() && config.isAutoStartFirstJoin()) {
            start(player, true);
        }
    }

    public void startOrResume(Player player) {
        if (config.isOpenGuiOnTutorialCommand() && config.isGuiEnabled() && tutorialGUI != null) {
            tutorialGUI.open(player);
            return;
        }

        PlayerProgress progress = getProgress(player.getUniqueId());
        if (!progress.isActive()) {
            start(player, false);
            return;
        }

        sendCurrentTask(player);
    }

    public void start(Player player, boolean forceRestart) {
        PlayerProgress progress = getProgress(player.getUniqueId());
        List<TutorialTask> tasks = config.getTutorialTasks();

        if (tasks.isEmpty()) {
            player.sendMessage(config.color("&cNo tutorial tasks are configured."));
            return;
        }

        if (progress.isCompleted() && !forceRestart) {
            player.sendMessage(applyPlaceholders(player, config.getAlreadyCompletedMessage()));
            return;
        }

        if (forceRestart || progress.isCompleted()) {
            progress.setCurrentTaskIndex(0);
            progress.setCompleted(false);
        }

        progress.setActive(true);
        dataStore.save(progress);
        sendCurrentTask(player);
    }

    public void reset(Player target) {
        dataStore.reset(target.getUniqueId());
    }

    public void skip(Player player) {
        if (!player.hasPermission(config.getSkipPermission())) {
            player.sendMessage(config.getSkipNotAllowedMessage());
            return;
        }

        PlayerProgress progress = getProgress(player.getUniqueId());
        progress.setActive(false);
        progress.setCompleted(true);
        progress.setCurrentTaskIndex(config.getTutorialTasks().size());
        dataStore.save(progress);

        sendLines(player, config.getTutorialCompleteMessages());
        runCompletionCommands(player);
    }

    public void handleMove(Player player, Location to) {
        if (to == null || player.hasPermission(config.getBypassPermission())) {
            return;
        }

        PlayerProgress progress = getProgress(player.getUniqueId());
        if (!progress.isActive()) {
            return;
        }

        TutorialTask task = getCurrentTask(progress);
        if (task == null || task.getType() != TaskType.MOVE_TO) {
            return;
        }

        World world = Bukkit.getWorld(task.getWorld());
        if (world == null || !to.getWorld().getName().equalsIgnoreCase(task.getWorld())) {
            return;
        }

        Location target = new Location(world, task.getX(), task.getY(), task.getZ());
        double distanceSquared = to.distanceSquared(target);
        if (distanceSquared <= (task.getRadius() * task.getRadius())) {
            completeCurrentTask(player, progress);
        }
    }

    public void handleCommand(Player player, String enteredCommand) {
        if (enteredCommand == null || player.hasPermission(config.getBypassPermission())) {
            return;
        }

        PlayerProgress progress = getProgress(player.getUniqueId());
        if (!progress.isActive()) {
            return;
        }

        TutorialTask task = getCurrentTask(progress);
        if (task == null || task.getType() != TaskType.COMMAND) {
            return;
        }

        String expected = normalizeCommand(task.getCommand());
        String actual = normalizeCommand(enteredCommand);

        if (actual.equals(expected) || actual.startsWith(expected + " ")) {
            completeCurrentTask(player, progress);
        }
    }

    public void sendStatus(CommandSender sender, Player target) {
        PlayerProgress progress = getProgress(target.getUniqueId());
        int total = Math.max(1, config.getTutorialTasks().size());
        int done = Math.min(progress.getCurrentTaskIndex(), total);

        sender.sendMessage(config.color("&b--- Tutorial Status: &f" + target.getName() + " &b---"));
        sender.sendMessage(config.color("&7Active: &f" + progress.isActive()));
        sender.sendMessage(config.color("&7Completed: &f" + progress.isCompleted()));
        sender.sendMessage(config.color("&7Progress: &f" + done + "&7/&f" + total));
    }

    public PlayerProgress getProgress(UUID uniqueId) {
        return dataStore.getOrCreate(uniqueId);
    }

    public int getTotalTaskCount() {
        return config.getTutorialTasks().size();
    }

    public TutorialTask getCurrentTask(PlayerProgress progress) {
        List<TutorialTask> tasks = config.getTutorialTasks();
        int index = progress.getCurrentTaskIndex();
        if (index < 0 || index >= tasks.size()) {
            return null;
        }
        return tasks.get(index);
    }

    private void sendCurrentTask(Player player) {
        PlayerProgress progress = getProgress(player.getUniqueId());
        TutorialTask task = getCurrentTask(progress);

        if (task == null) {
            finishTutorial(player, progress);
            return;
        }

        int current = progress.getCurrentTaskIndex() + 1;
        int total = Math.max(1, config.getTutorialTasks().size());

        displayTaskStart(player, task, current, total);

        if (config.isGuiEnabled() && tutorialGUI != null && config.isOpenGuiOnTutorialCommand()) {
            tutorialGUI.open(player);
        }
    }

    private void completeCurrentTask(Player player, PlayerProgress progress) {
        progress.setCurrentTaskIndex(progress.getCurrentTaskIndex() + 1);
        dataStore.save(progress);

        displayTaskComplete(player);

        if (progress.getCurrentTaskIndex() >= config.getTutorialTasks().size()) {
            finishTutorial(player, progress);
        } else {
            sendCurrentTask(player);
        }
    }

    private void finishTutorial(Player player, PlayerProgress progress) {
        progress.setActive(false);
        progress.setCompleted(true);
        progress.setCurrentTaskIndex(config.getTutorialTasks().size());
        dataStore.save(progress);

        sendLines(player, config.getTutorialCompleteMessages());
        runCompletionCommands(player);
    }

    private void runCompletionCommands(Player player) {
        for (String command : config.getCompletionCommands()) {
            String resolved = applyPlaceholders(player, command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
        }
    }

    private void sendLines(Player player, List<String> lines) {
        for (String line : lines) {
            player.sendMessage(applyPlaceholders(player, line));
        }
    }

    public String applyPlaceholders(Player player, String text) {
        if (text == null) {
            return "";
        }

        PlayerProgress progress = getProgress(player.getUniqueId());
        int totalTasks = Math.max(1, config.getTutorialTasks().size());
        int done = Math.min(progress.getCurrentTaskIndex(), totalTasks);

        String parsed = text
            .replace("%player_name%", player.getName())
            .replace("%eztutorial_progress%", String.valueOf(done))
            .replace("%eztutorial_total_tasks%", String.valueOf(totalTasks))
            .replace("%eztutorial_completed%", String.valueOf(progress.isCompleted()))
            .replace("%eztutorial_active%", String.valueOf(progress.isActive()));

        return applyPlaceholderApi(player, parsed);
    }

    private String applyPlaceholderApi(Player player, String text) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return text;
        }

        try {
            Class<?> placeholderApiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method method = placeholderApiClass.getMethod("setPlaceholders", Player.class, String.class);
            Object value = method.invoke(null, player, text);
            if (value instanceof String result) {
                return result;
            }
        } catch (ReflectiveOperationException ignored) {
            plugin.getLogger().warning("PlaceholderAPI found, but placeholder parse failed.");
        }

        return text;
    }

    private String normalizeCommand(String command) {
        String normalized = command.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")) {
            return normalized.substring(1);
        }
        return normalized;
    }

    private void displayTaskStart(Player player, TutorialTask task, int current, int total) {
        DisplayMode mode = config.getTaskStartDisplayMode();
        String taskId = task.getId();
        String taskDescription = task.getDescription();

        switch (mode) {
            case CHAT:
                for (String line : config.getTaskStartMessages()) {
                    player.sendMessage(taskPlaceholders(player, line, taskId, taskDescription, current, total));
                }
                break;
            case TITLE:
                String startTitle = taskPlaceholders(player, config.getTitleTaskStartTitle(), taskId, taskDescription, current, total);
                String startSubtitle = taskPlaceholders(player, config.getTitleTaskStartSubtitle(), taskId, taskDescription, current, total);
                player.sendTitle(startTitle, startSubtitle, 10, 45, 10);
                break;
            case SCOREBOARD:
                updateScoreboard(player, config.getScoreboardTaskStartLines(), taskId, taskDescription, current, total);
                break;
            case ACTIONBAR:
            default:
                String actionbar = taskPlaceholders(player, config.getActionbarTaskStartText(), taskId, taskDescription, current, total);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbar));
                break;
        }
    }

    private void displayTaskComplete(Player player) {
        DisplayMode mode = config.getTaskCompleteDisplayMode();
        String noTaskId = "-";
        String noTaskDescription = config.color("&7Task completed");
        int current = Math.max(0, getProgress(player.getUniqueId()).getCurrentTaskIndex());
        int total = Math.max(1, config.getTutorialTasks().size());

        switch (mode) {
            case CHAT:
                for (String line : config.getTaskCompleteMessages()) {
                    player.sendMessage(taskPlaceholders(player, line, noTaskId, noTaskDescription, current, total));
                }
                break;
            case TITLE:
                String completeTitle = taskPlaceholders(player, config.getTitleTaskCompleteTitle(), noTaskId, noTaskDescription, current, total);
                String completeSubtitle = taskPlaceholders(player, config.getTitleTaskCompleteSubtitle(), noTaskId, noTaskDescription, current, total);
                player.sendTitle(completeTitle, completeSubtitle, 8, 35, 8);
                break;
            case SCOREBOARD:
                updateScoreboard(player, config.getScoreboardTaskCompleteLines(), noTaskId, noTaskDescription, current, total);
                break;
            case ACTIONBAR:
            default:
                String actionbar = taskPlaceholders(player, config.getActionbarTaskCompleteText(), noTaskId, noTaskDescription, current, total);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbar));
                break;
        }
    }

    private String taskPlaceholders(Player player, String text, String taskId, String taskDescription, int current, int total) {
        return applyPlaceholders(player, text)
            .replace("%task_id%", taskId)
            .replace("%task_description%", taskDescription)
            .replace("%task_current%", String.valueOf(current))
            .replace("%task_total%", String.valueOf(total));
    }

    private void updateScoreboard(Player player, List<String> templateLines, String taskId, String taskDescription, int current, int total) {
        List<String> lines = new ArrayList<>();
        for (String rawLine : templateLines) {
            lines.add(taskPlaceholders(player, rawLine, taskId, taskDescription, current, total));
        }

        if (lines.isEmpty()) {
            lines.add(config.color("&7No display lines configured."));
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("eztutorial", "dummy", config.getScoreboardTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = Math.min(15, lines.size());
        for (int i = 0; i < score; i++) {
            String line = lines.get(i);
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }
            objective.getScore(line + " " + ChatColor.values()[i]).setScore(score - i);
        }

        player.setScoreboard(scoreboard);
    }
}


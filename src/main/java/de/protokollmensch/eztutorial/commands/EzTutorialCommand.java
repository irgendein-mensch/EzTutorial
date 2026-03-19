package de.protokollmensch.eztutorial.commands;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.services.TutorialService;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EzTutorialCommand implements CommandExecutor, TabCompleter {

    private final EzTutorial plugin;
    private final TutorialService tutorialService;

    public EzTutorialCommand(EzTutorial plugin, TutorialService tutorialService) {
        this.plugin = plugin;
        this.tutorialService = tutorialService;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "===== EzTutorial Admin =====");
        sender.sendMessage(ChatColor.AQUA + "/eztutorial help" + ChatColor.GRAY + " - Show this help");
        sender.sendMessage(ChatColor.AQUA + "/eztutorial reload" + ChatColor.GRAY + " - Reload all configs");
        sender.sendMessage(ChatColor.AQUA + "/eztutorial start <player>" + ChatColor.GRAY + " - Start tutorial for a player");
        sender.sendMessage(ChatColor.AQUA + "/eztutorial reset <player>" + ChatColor.GRAY + " - Reset tutorial progress");
        sender.sendMessage(ChatColor.AQUA + "/eztutorial status <player>" + ChatColor.GRAY + " - Show tutorial status");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("eztutorial.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission for this!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                plugin.reloadPlugin();
                sender.sendMessage(plugin.getConfigManager().getReloadedMessage());
                return true;

            case "start":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eztutorial start <player>");
                    return true;
                }
                Player startTarget = Bukkit.getPlayerExact(args[1]);
                if (startTarget == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                tutorialService.start(startTarget, true);
                sender.sendMessage(plugin.getConfigManager().getPlayerStartedMessage());
                return true;

            case "reset":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eztutorial reset <player>");
                    return true;
                }
                Player resetTarget = Bukkit.getPlayerExact(args[1]);
                if (resetTarget == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                tutorialService.reset(resetTarget);
                sender.sendMessage(plugin.getConfigManager().getPlayerResetMessage());
                return true;

            case "status":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /eztutorial status <player>");
                    return true;
                }
                Player statusTarget = Bukkit.getPlayerExact(args[1]);
                if (statusTarget == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                tutorialService.sendStatus(sender, statusTarget);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /eztutorial help for help.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
            completions.add("reload");
            completions.add("start");
            completions.add("reset");
            completions.add("status");
        }

        if (args.length == 2 && Arrays.asList("start", "reset", "status").contains(args[0].toLowerCase())) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
}
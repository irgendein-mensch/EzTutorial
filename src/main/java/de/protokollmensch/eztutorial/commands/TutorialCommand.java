package de.protokollmensch.eztutorial.commands;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.services.TutorialService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TutorialCommand implements CommandExecutor, TabCompleter {

    private final EzTutorial plugin;
    private final TutorialService tutorialService;

    public TutorialCommand(EzTutorial plugin, TutorialService tutorialService) {
        this.plugin = plugin;
        this.tutorialService = tutorialService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("eztutorial.admin")) {
                sender.sendMessage("You do not have permission for this.");
                return true;
            }

            plugin.reloadPlugin();
            sender.sendMessage(plugin.getConfigManager().getReloadedMessage());
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            tutorialService.startOrResume(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start":
            case "resume":
                tutorialService.start(player, false);
                return true;
            case "status":
                tutorialService.sendStatus(sender, player);
                return true;
            case "skip":
                tutorialService.skip(player);
                return true;
            default:
                tutorialService.startOrResume(player);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.add("start");
            result.add("resume");
            result.add("status");
            result.add("skip");
            result.add("reload");
        }

        return result;
    }
}
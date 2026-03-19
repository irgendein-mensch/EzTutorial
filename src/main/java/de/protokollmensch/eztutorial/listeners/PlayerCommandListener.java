package de.protokollmensch.eztutorial.listeners;

import de.protokollmensch.eztutorial.services.TutorialService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandListener implements Listener {

    private final TutorialService tutorialService;

    public PlayerCommandListener(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.length() <= 1) {
            return;
        }

        tutorialService.handleCommand(event.getPlayer(), message.substring(1));
    }
}


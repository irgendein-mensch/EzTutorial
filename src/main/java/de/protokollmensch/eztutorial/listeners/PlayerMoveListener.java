package de.protokollmensch.eztutorial.listeners;

import de.protokollmensch.eztutorial.services.TutorialService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final TutorialService tutorialService;

    public PlayerMoveListener(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        tutorialService.handleMove(event.getPlayer(), event.getTo());
    }
}


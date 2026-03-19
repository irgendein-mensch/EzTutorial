package de.protokollmensch.eztutorial.listeners;

import de.protokollmensch.eztutorial.services.TutorialService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final TutorialService tutorialService;

    public PlayerJoinListener(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        tutorialService.handleJoin(event.getPlayer());
    }
}


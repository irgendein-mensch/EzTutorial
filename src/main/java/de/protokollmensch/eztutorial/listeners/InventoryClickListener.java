package de.protokollmensch.eztutorial.listeners;

import de.protokollmensch.eztutorial.gui.TutorialGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final TutorialGUI tutorialGUI;

    public InventoryClickListener(TutorialGUI tutorialGUI) {
        this.tutorialGUI = tutorialGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        tutorialGUI.handleClick(event);
    }
}


package de.protokollmensch.eztutorial.gui;

import de.protokollmensch.eztutorial.EzTutorial;
import de.protokollmensch.eztutorial.models.PlayerProgress;
import de.protokollmensch.eztutorial.models.TutorialTask;
import de.protokollmensch.eztutorial.services.TutorialService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialGUI {

    public static class TutorialInventoryHolder implements InventoryHolder {
        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final EzTutorial plugin;
    private final TutorialService tutorialService;

    public TutorialGUI(EzTutorial plugin, TutorialService tutorialService) {
        this.plugin = plugin;
        this.tutorialService = tutorialService;
    }

    public void open(Player player) {
        int rows = plugin.getConfigManager().getGuiRows();
        TutorialInventoryHolder holder = new TutorialInventoryHolder();
        Inventory inventory = Bukkit.createInventory(holder, rows * 9, plugin.getConfigManager().getGuiTitle());
        holder.setInventory(inventory);

        ItemStack filler = createGuiItem(plugin.getConfigManager().getGuiFillerItem(), " ");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }

        PlayerProgress progress = tutorialService.getProgress(player.getUniqueId());
        List<TutorialTask> tasks = plugin.getConfigManager().getTutorialTasks();
        List<Integer> configuredSlots = plugin.getConfigManager().getGuiTaskSlots();

        List<String> headerLore = new ArrayList<>(plugin.getConfigManager().getGuiHeaderLore());
        headerLore.add(plugin.getConfigManager().color("&7Progress: &b" + Math.min(progress.getCurrentTaskIndex(), tasks.size()) + "&7/&b" + tasks.size()));
        headerLore.add(plugin.getConfigManager().color("&7Status: " + (progress.isCompleted() ? "&aCompleted" : (progress.isActive() ? "&eActive" : "&7Not started"))));
        inventory.setItem(
            plugin.getConfigManager().getGuiHeaderSlot(),
            createGuiItem(plugin.getConfigManager().getGuiHeaderItem(), plugin.getConfigManager().getGuiHeaderName(), headerLore.toArray(new String[0]))
        );

        int taskCount = Math.min(tasks.size(), configuredSlots.size());
        for (int i = 0; i < taskCount; i++) {
            TutorialTask task = tasks.get(i);
            boolean done = i < progress.getCurrentTaskIndex();
            Material material = done ? plugin.getConfigManager().getGuiTaskItemDone() : plugin.getConfigManager().getGuiTaskItemOpen();

            List<String> lore = new ArrayList<>();
            lore.add(task.getDescription());
            lore.add(plugin.getConfigManager().color("&7Status: " + (done ? "&aCompleted" : "&eOpen")));
            lore.add(plugin.getConfigManager().color("&8ID: " + task.getId()));

            ItemStack item = createGuiItem(
                material,
                plugin.getConfigManager().color("&bTask " + (i + 1)),
                lore.toArray(new String[0])
            );

            inventory.setItem(configuredSlots.get(i), item);
        }

        String startName = progress.isCompleted()
            ? plugin.getConfigManager().color("&aAlready completed")
            : plugin.getConfigManager().getGuiStartName();

        List<String> startLore = new ArrayList<>(plugin.getConfigManager().getGuiStartLore());
        if (progress.isCompleted()) {
            startLore.add(plugin.getConfigManager().color("&7You can only view your progress now."));
        }

        inventory.setItem(plugin.getConfigManager().getGuiStartSlot(),
            createGuiItem(plugin.getConfigManager().getGuiStartItem(), startName, startLore.toArray(new String[0])));

        inventory.setItem(plugin.getConfigManager().getGuiCloseSlot(),
            createGuiItem(
                plugin.getConfigManager().getGuiCloseItem(),
                plugin.getConfigManager().getGuiCloseName(),
                plugin.getConfigManager().getGuiCloseLore().toArray(new String[0])
            )
        );

        if (plugin.getConfigManager().isGuiSkipButtonEnabled()) {
            inventory.setItem(plugin.getConfigManager().getGuiSkipSlot(),
                createGuiItem(
                    plugin.getConfigManager().getGuiSkipItem(),
                    plugin.getConfigManager().getGuiSkipName(),
                    plugin.getConfigManager().getGuiSkipLore().toArray(new String[0])
                )
            );
        }

        player.openInventory(inventory);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        if (!isTutorialInventory(event.getView())) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (event.getSlot() == plugin.getConfigManager().getGuiCloseSlot()) {
            player.closeInventory();
            return;
        }

        if (event.getSlot() == plugin.getConfigManager().getGuiStartSlot()) {
            player.closeInventory();
            tutorialService.start(player, false);
            return;
        }

        if (plugin.getConfigManager().isGuiSkipButtonEnabled() && event.getSlot() == plugin.getConfigManager().getGuiSkipSlot()) {
            player.closeInventory();
            tutorialService.skip(player);
        }
    }

    public boolean isTutorialInventory(InventoryView view) {
        return view.getTopInventory().getHolder() instanceof TutorialInventoryHolder;
    }

    public void refreshOpenTutorialInventories() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (isTutorialInventory(onlinePlayer.getOpenInventory())) {
                open(onlinePlayer);
            }
        }
    }
}
package com.cjburkey.claimchunk.gui;

import com.cjburkey.claimchunk.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

/**
 * The handler behind ClaimChunk GUI screens. This class acts as the interface for opening GUIs as
 * well as the event listener for players clicking in/closing the screens.
 *
 * @since 0.0.26
 */
public class CCGuiHandler implements Listener {

    private final HashMap<UUID, CCOpenGui> openGuis = new HashMap<>();

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        CCOpenGui openGui = openGuis.get(uuid);
        if (openGui != null) {
            if (e.getInventory().equals(openGui.inventory())) {
                openGui.gui()
                        .onClick(
                                openGui.inventory(),
                                (Player) e.getWhoClicked(),
                                e.getSlot(),
                                e.getClick(),
                                e.getCurrentItem());
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent e) {
        if (openGuis.containsKey(e.getPlayer().getUniqueId())) {
            closeGui((Player) e.getPlayer());
        }
    }

    public void openGui(@NotNull Player player, @NotNull ICCGui gui) {
        if (openGuis.containsKey(player.getUniqueId())) {
            closeGui(player);
        }
        openGuis.put(player.getUniqueId(), new CCOpenGui(gui, createAndShowGui(player, gui)));
        gui.onOpen(openGuis.get(player.getUniqueId()).inventory(), player);
    }

    public void closeGui(@NotNull Player player) {
        if (openGuis.containsKey(player.getUniqueId())) {
            openGuis.get(player.getUniqueId())
                    .gui()
                    .onClose(openGuis.get(player.getUniqueId()).inventory(), player);
            openGuis.remove(player.getUniqueId());
        }
    }

    public void refreshGui(@NotNull Player player, @NotNull ICCGui gui) {
        closeGui(player);
        openGui(player, gui);
    }

    private static Inventory createAndShowGui(@NotNull Player player, @NotNull ICCGui gui) {
        int rowCount = Math.min(Math.max(gui.getRows(), 1), 6);
        Inventory inventory =
                Bukkit.createInventory(player, rowCount * 9, Utils.color(gui.getName()));
        player.openInventory(inventory);
        return inventory;
    }
}

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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

/**
 * The handler behind ClaimChunk GUI screens. This class acts as the interface for opening GUIs as
 * well as the event listener for players clicking in/closing the screens.
 *
 * @since 1.0.0
 */
public class CCGuiHandler implements Listener {

    private final HashMap<UUID, CCOpenGui> openGuis = new HashMap<>();

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        CCOpenGui openGui = openGuis.get(uuid);
        if (openGui != null) {
            if (e.getInventory().equals(openGui.inventory())
                    && e.getSlot() >= 0
                    && e.getSlot() < e.getInventory().getSize()) {
                openGui.gui()
                        .onClick(
                                openGui.inventory(), e.getSlot(), e.getClick(), e.getCurrentItem());
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

    public void openOrRefreshGui(@NotNull ICCGui gui) {
        Player player = gui.getPlayer();
        CCOpenGui openGui = closeGui(player, false);

        final Inventory inventory;
        if (openGui != null && openGui.gui() == gui) {
            inventory = openGui.inventory();
            inventory.clear();
        } else {
            inventory = createAndShowGui(player, gui);
        }

        openGuis.put(player.getUniqueId(), new CCOpenGui(gui, inventory));
        gui.onOpen(openGuis.get(player.getUniqueId()).inventory());
    }

    public void closeGui(@NotNull Player player) {
        closeGui(player, true);
    }

    private @Nullable CCOpenGui closeGui(@NotNull Player player, boolean removeFromMap) {
        CCOpenGui openGui = openGuis.get(player.getUniqueId());
        if (openGui != null) {
            openGui.gui().onClose(openGuis.get(player.getUniqueId()).inventory());
            if (removeFromMap) {
                openGuis.remove(player.getUniqueId());
            }
        }
        return openGui;
    }

    private static Inventory createAndShowGui(@NotNull Player player, @NotNull ICCGui gui) {
        int rowCount = Math.min(Math.max(gui.getRows(), 1), 6);
        Inventory inventory =
                Bukkit.createInventory(player, rowCount * 9, Utils.color(gui.getName()));
        player.openInventory(inventory);
        return inventory;
    }
}

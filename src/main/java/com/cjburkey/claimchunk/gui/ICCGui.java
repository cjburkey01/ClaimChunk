package com.cjburkey.claimchunk.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing a ClaimChunk GUI screen.
 *
 * @since 0.0.26
 */
public interface ICCGui {

    /**
     * Called when this screen is shown to a player.
     *
     * @param inventory The inventory behind this GUI
     * @param player The player the GUI is shown to
     */
    void onOpen(@NotNull Inventory inventory, @NotNull Player player);

    /**
     * Called when this screen is being closed.
     *
     * @param inventory The inventory behind this GUI
     * @param player The player closing the GUI
     */
    void onClose(@NotNull Inventory inventory, @NotNull Player player);

    /**
     * Called when the player clicks on a given slot within the inventory.
     *
     * @param inventory The inventory behind this GUI
     * @param player The player clicking in the GUI being shown to them.
     * @param slot The index of the slot being clicked.
     * @param clickType Which type of click the player performed.
     * @param stack The stack on which the player clicked.
     */
    void onClick(
            @NotNull Inventory inventory,
            @NotNull Player player,
            int slot,
            @NotNull ClickType clickType,
            @Nullable ItemStack stack);

    /**
     * @return The name to be shown at the top of the GUI
     */
    @NotNull
    String getName();

    /**
     * @return The number of rows this GUI should have
     */
    int getRows();
}

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
     * Called when the GUI is being constructed to be shown to the player.
     *
     * <p>Modify the inventory inside this method
     *
     * @param inventory The inventory being shown.
     */
    void onOpen(@NotNull Inventory inventory);

    /**
     * Called when the GUI is closed; nothing usually needs to happen, but just in case, you know?
     *
     * @param inventory The inventory being shown.
     */
    void onClose(@NotNull Inventory inventory);

    /**
     * Called when the player clicks on a given slot within the inventory.
     *
     * @param inventory The inventory being shown.
     * @param slot The index of the slot being clicked.
     * @param clickType Which type of click the player performed.
     * @param stack The stack on which the player clicked.
     */
    void onClick(
            @NotNull Inventory inventory,
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

    /**
     * @return The player this GUI is going to be shown to.
     */
    Player getPlayer();
}

package com.cjburkey.claimchunk.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract wrapper for {@link ICCGui} to make creating inventory menus easier.
 *
 * @since 0.0.26
 */
public abstract class GuiMenuScreen implements ICCGui {

    /** Anonymous callback for item click event. */
    @FunctionalInterface
    public interface GuiItemAction {

        /** Called when the item is clicked. */
        void onClick(
                @NotNull Inventory inventory,
                @NotNull Player player,
                int slot,
                @NotNull ClickType clickType,
                @NotNull ItemStack stack);
    }

    /** Wrapper around an interactive item in the GUI menu. */
    record GuiItemWrapper(@NotNull ItemStack stack, @NotNull GuiItemAction action) {}

    private final int rowCount;
    private final GuiItemWrapper[] actions;
    private final String name;

    protected GuiMenuScreen(int rowCount, @NotNull String name) {
        this.rowCount = Math.min(Math.max(rowCount, 1), 6);
        this.actions = new GuiItemWrapper[this.rowCount * 9];
        this.name = name;
    }

    /**
     * Add an interactive button item to this inventory GUI.
     *
     * @param slot The slot of the inventory in which to put the item.
     * @param stack The stack that represents this action.
     * @param action The on-click callback
     */
    protected void addInteractiveButton(
            int slot, @NotNull ItemStack stack, @NotNull GuiItemAction action) {
        if (slot >= 0 && slot < this.actions.length) {
            this.actions[slot] = new GuiItemWrapper(stack, action);
        }
    }

    @Override
    public void onOpen(@NotNull Inventory inventory, @NotNull Player player) {
        for (int slot = 0; slot < actions.length; slot++) {
            GuiItemWrapper action = actions[slot];
            if (action != null) {
                inventory.setItem(slot, action.stack());
            }
        }
    }

    @Override
    public void onClick(
            @NotNull Inventory inventory,
            @NotNull Player player,
            int slot,
            @NotNull ClickType clickType,
            @Nullable ItemStack stack) {
        assert slot >= 0 && slot < actions.length;

        GuiItemWrapper action = actions[slot];
        if (action != null && stack != null) {
            action.action().onClick(inventory, player, slot, clickType, stack);
        }
    }

    @Override
    public void onClose(@NotNull Inventory inventory, @NotNull Player player) {}

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int getRows() {
        return rowCount;
    }
}

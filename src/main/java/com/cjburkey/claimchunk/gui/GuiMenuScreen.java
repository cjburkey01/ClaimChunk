package com.cjburkey.claimchunk.gui;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        void onClick(@NotNull ClickType clickType, @NotNull ItemStack stack);
    }

    /** Wrapper around an interactive item in the GUI menu. */
    record GuiItemWrapper(@NotNull ItemStack stack, @NotNull GuiItemAction action) {}

    protected final ClaimChunk claimChunk;
    private final int rowCount;
    private final GuiItemWrapper[] actions;
    private final String name;

    protected @Nullable Inventory inventory;
    protected @Nullable Player player;

    protected GuiMenuScreen(ClaimChunk claimChunk, int rowCount, @NotNull String name) {
        this.claimChunk = claimChunk;
        this.rowCount = Math.min(Math.max(rowCount, 1), 6);
        this.actions = new GuiItemWrapper[this.rowCount * 9];
        this.name = name;
    }

    /**
     * Add an interactive button item to this inventory GUI.
     *
     * <p>Must be called in {@link this#onBuildGui()},
     *
     * @param slot The slot of the inventory in which to put the item.
     * @param itemType The material of the item.
     * @param itemName The display name of the item stack.
     * @param itemLore The lore attached to the item.
     * @param action The on-click callback
     */
    protected void addInteractiveButton(
            int slot,
            @NotNull Material itemType,
            @NotNull String itemName,
            @NotNull List<String> itemLore,
            @NotNull GuiItemAction action) {
        if (inventory == null) return;

        Utils.debug("Made GUI stack in slot: " + slot);
        Utils.debug("Max: " + this.actions.length);
        if (slot >= 0 && slot < this.actions.length && itemType != Material.AIR) {
            ItemStack stack = makeStack(itemType, itemName, itemLore);
            if (stack != null) {
                inventory.setItem(slot, stack);
                this.actions[slot] = new GuiItemWrapper(stack, action);
            }
        }
    }

    private @Nullable ItemStack makeStack(
            @NotNull Material itemType, @NotNull String itemName, @NotNull List<String> itemLore) {
        ItemStack stack = new ItemStack(itemType);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        meta.setDisplayName(Utils.color(itemName));
        meta.setLore(itemLore.stream().map(Utils::color).toList());
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void onOpen(@NotNull Inventory inventory, @NotNull Player player) {
        this.inventory = inventory;
        this.player = player;

        onBuildGui();
    }

    /**
     * Called when the GUI is opened so you don't have to override {@link this#onOpen(Inventory,
     * Player)}
     */
    protected abstract void onBuildGui();

    /** Method to reopen this gui (to update item names, etc.) */
    protected void refresh() {
        claimChunk.getGuiHandler().refreshGui(player, this);
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
            action.action().onClick(clickType, stack);
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

package com.cjburkey.claimchunk.gui;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

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
    private final Player player;
    private final int rowCount;
    private final String name;
    private final GuiItemWrapper[] actions;

    protected GuiMenuScreen(
            ClaimChunk claimChunk, @NotNull Player player, int rowCount, @NotNull String name) {
        this.claimChunk = claimChunk;
        this.player = player;
        this.rowCount = Math.min(Math.max(rowCount, 1), 6);
        this.name = name;
        this.actions = new GuiItemWrapper[this.rowCount * 9];
    }

    /**
     * Add an interactive button item to this inventory GUI.
     *
     * @param inventory This GUI's inventory.
     * @param slot The slot of the inventory in which to put the item.
     * @param itemType The material of the item.
     * @param itemName The display name of the item stack.
     * @param itemLore The lore attached to the item.
     * @param action The on-click callback
     */
    protected void addInteractiveButton(
            @NotNull Inventory inventory,
            int slot,
            @NotNull Material itemType,
            @NotNull String itemName,
            @NotNull List<String> itemLore,
            @NotNull GuiItemAction action) {
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

    /** Method to reopen this gui (to update item names, etc.) */
    protected void refresh() {
        claimChunk.getGuiHandler().openOrRefreshGui(this);
    }

    @Override
    public void onClick(
            @NotNull Inventory inventory,
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
    public void onClose(@NotNull Inventory inventory) {}

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int getRows() {
        return rowCount;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the given item name's associated {@link Material} enum variant.
     *
     * @param val The item name, like {@code minecraft:grass_block}.
     * @return The Material, or {@link Material#BARRIER} if the provided item isn't valid.
     */
    protected static @NotNull Material materialFromStr(String val) {
        Material item = Material.matchMaterial(val);
        if (item == null) {
            item = Material.BARRIER;
        }
        return item;
    }

    /**
     * Generate chunk position text based on the message handler.
     *
     * @param chunkPos The position of the chunk
     * @return A localized string representing a position in the given world.
     */
    protected @NotNull String guiChunkPosText(@NotNull ChunkPos chunkPos) {
        return claimChunk
                .getMessages()
                .guiChunkPos
                .replaceAll(Pattern.quote("%%WORLD%%"), chunkPos.world())
                .replaceAll(Pattern.quote("%%X%%"), chunkPos.x() + "")
                .replaceAll(Pattern.quote("%%Z%%"), chunkPos.z() + "");
    }

    /**
     * Helper method to generate localized chunk owner name GUI text.
     *
     * @param chunkName The non-null name of the chunk owner. This replaces {@code %%NAME%%}
     *     verbatim.
     * @return Player-facing localized chunk owner text.
     */
    protected @NotNull String guiChunkOwnerNameText(@NotNull String chunkName) {
        return claimChunk
                .getMessages()
                .guiChunkOwner
                .replaceAll(Pattern.quote("%%NAME%%"), chunkName);
    }

    /**
     * Helper method to get the name for this given chunk owner, or return the localized unknown
     * player text.
     *
     * @param chunkOwner The non-null owner of the chunk.
     * @return The name for the chunk's owner that can be shown to a player in the GUI.
     */
    protected @NotNull String chunkNameOrUnknown(@NotNull UUID chunkOwner) {
        String chunkName = claimChunk.getPlayerHandler().getChunkName(chunkOwner);
        return chunkName != null ? chunkName : claimChunk.getMessages().unknownChunkOwner;
    }

    protected static @NotNull List<String> splitLineLore(@NotNull String loreLine) {
        return Arrays.asList(loreLine.split("\n"));
    }

    /**
     * Helper method to open the given GUI for its respective player.
     *
     * @param gui The gui to open.
     */
    protected void openGui(@NotNull ICCGui gui) {
        claimChunk.getGuiHandler().openOrRefreshGui(gui);
    }
}

package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.gui.ICCGui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MainMenu implements ICCGui {

    @Override
    public void onOpen(@NotNull Inventory inventory, @NotNull Player player) {}

    @Override
    public void onClose(@NotNull Inventory inventory, @NotNull Player player) {}

    @Override
    public void onClick(
            @NotNull Inventory inventory,
            @NotNull Player player,
            int slot,
            @NotNull ClickType clickType,
            @NotNull ItemStack stack) {}

    @Override
    public @NotNull String getName() {
        return "ClaimChunk GUI";
    }

    @Override
    public int getRows() {
        return 0;
    }
}

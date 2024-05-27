package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.gui.GuiMenuScreen;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MainMenu extends GuiMenuScreen {

    public MainMenu() {
        super(1, "ClaimChunk Menu");
        addButtons();
    }

    private void addButtons() {
        addInteractiveButton(
                0,
                new ItemStack(Material.PAPER),
                (Inventory inventory,
                        Player player,
                        int slot,
                        ClickType clickType,
                        ItemStack stack) -> {});
    }
}

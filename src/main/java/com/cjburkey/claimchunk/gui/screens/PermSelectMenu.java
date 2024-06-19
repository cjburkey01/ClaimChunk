package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.ClaimChunkConfig;
import com.cjburkey.claimchunk.gui.GuiMenuScreen;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class PermSelectMenu extends GuiMenuScreen {

    public PermSelectMenu(@NotNull ClaimChunk claimChunk, @NotNull Player player) {
        // Offset to make it 6 tall so that we can have the top row be a menu bar.
        super(claimChunk, player, 1, claimChunk.getMessages().guiPermSelectMenuTitle);
    }

    @Override
    public void onOpen(@NotNull Inventory inventory) {
        V2JsonMessages messages = claimChunk.getMessages();
        ClaimChunkConfig config = claimChunk.getConfigHandler();

        // Back button
        addInteractiveButton(
                inventory,
                0,
                materialFromStr(config.getGuiMenuBackButtonItem()),
                messages.guiMenuBackButtonName,
                splitLineLore(messages.guiPermModifyBackButtonDesc),
                (clickType, stack) -> openGui(new MainMenu(claimChunk, getPlayer())));

        Material item = materialFromStr(config.getGuiPermSelectMenuItem());

        // Permission type selection button
        addInteractiveButton(
                inventory,
                4,
                item,
                messages.guiPermSelectMenuThisChunkName,
                splitLineLore(messages.guiPermSelectMenuThisChunkDesc),
                (clickType, stack) -> openGui(new PermModifyMenu(claimChunk, getPlayer(), false)));
        addInteractiveButton(
                inventory,
                5,
                item,
                messages.guiPermSelectMenuAllChunksName,
                splitLineLore(messages.guiPermSelectMenuAllChunksDesc),
                (clickType, stack) -> openGui(new PermModifyMenu(claimChunk, getPlayer(), true)));
    }
}

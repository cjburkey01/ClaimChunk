package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.ClaimChunkConfig;
import com.cjburkey.claimchunk.gui.GuiMenuScreen;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PermModifyMenu extends GuiMenuScreen {

    public final boolean isAllChunks;

    public PermModifyMenu(
            @NotNull ClaimChunk claimChunk, @NotNull Player player, boolean isAllChunks) {
        // Offset to make it 6 tall so that we can have the top row be a menu bar.
        super(claimChunk, player, 4, claimChunk.getMessages().guiPermSelectMenuTitle);

        this.isAllChunks = isAllChunks;
    }

    @Override
    public void onOpen(@NotNull Inventory inventory) {
        V2JsonMessages messages = claimChunk.getMessages();
        ClaimChunkConfig config = claimChunk.getConfigHandler();
        Material allowItem = materialFromStr(config.getGuiPermSelectMenuItem());
        Material denyItem = materialFromStr(config.getGuiPermSelectMenuItem());

        // Back button
        addInteractiveButton(
                inventory,
                0,
                materialFromStr(config.getGuiMenuBackButtonItem()),
                messages.guiMenuBackButtonName,
                splitLineLore(messages.guiMenuBackButtonDesc),
                (clickType, stack) -> openGui(new PermSelectMenu(claimChunk, getPlayer())));

        // Add permission buttons
        // TODO: WE HAVE TO ADD DEFAULT PERMISSIONS TO CHUNK DATA!
        Map<String, Boolean> permissions =
                isAllChunks
                        ? claimChunk
                                .getPlayerHandler()
                                .getDefaultPermissions(getPlayer().getUniqueId())
                                .toPermissionsMap()
                        : null;
        if (permissions == null) {
            return;
        }
        int slot = 0;
        for (Map.Entry<String, Boolean> permission : permissions.entrySet()) {
            addInteractiveButton(
                    inventory,
                    9 + slot,
                    permission.getValue() ? allowItem : denyItem,
                    "&r&f" + permission.getKey(),
                    splitLineLore(messages.guiPermModifyPermDesc),
                    (clickType, stack) -> {});
            slot++;
        }
    }

    @Override
    public @NotNull String getName() {
        V2JsonMessages messages = claimChunk.getMessages();
        return isAllChunks
                ? messages.guiPermModifyAllMenuTitle
                : messages.guiPermModifyThisMenuTitle;
    }
}

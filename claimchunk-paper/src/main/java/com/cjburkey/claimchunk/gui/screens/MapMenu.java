package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.ClaimChunkConfig;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.gui.GuiMenuScreen;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class MapMenu extends GuiMenuScreen {

    private static final int MAP_HEIGHT = 5;

    public MapMenu(@NotNull ClaimChunk claimChunk, @NotNull Player player) {
        // Offset to make it 6 tall so that we can have the top row be a menu bar.
        super(claimChunk, player, MAP_HEIGHT + 1, claimChunk.getMessages().guiMapMenuTitle);
    }

    @Override
    public void onOpen(@NotNull Inventory inventory) {
        int halfHeight = Math.floorDiv(MAP_HEIGHT, 2);

        ChunkHandler chunkHandler = claimChunk.getChunkHandler();
        V2JsonMessages messages = claimChunk.getMessages();
        ClaimChunkConfig config = claimChunk.getConfigHandler();
        ChunkPos centerChunk = new ChunkPos(getPlayer().getLocation().getChunk());
        boolean enableClaimingFromMap = config.getGuiMapMenuAllowClaimOtherChunks();

        // Add the back button
        addInteractiveButton(
                inventory,
                0,
                materialFromStr(config.getGuiMenuBackButtonItem()),
                messages.guiMenuBackButtonName,
                Collections.singletonList(messages.guiMenuBackButtonDesc),
                (clickType, stack) -> openGui(new MainMenu(claimChunk, getPlayer())));

        // Add the map items
        // Inventory width is 9, so go 4 eastward and westward
        for (int offsetX = -4; offsetX <= 4; offsetX++) {
            // Inventory height is `MAP_HEIGHT`, so go `halfHeight` northward and southward
            for (int offsetZ = -halfHeight; offsetZ <= halfHeight; offsetZ++) {
                // The `+1` pushes down by one row, for the top row menu bar idea I have.
                int slot = (offsetX + 4) + 9 * (offsetZ + halfHeight + 1);
                ChunkPos offsetChunk =
                        new ChunkPos(
                                centerChunk.world(),
                                centerChunk.x() + offsetX,
                                centerChunk.z() + offsetZ);
                boolean isCenter = offsetChunk.equals(centerChunk);

                ArrayList<String> lore = new ArrayList<>();
                UUID chunkOwner = chunkHandler.getOwner(offsetChunk);
                boolean isOwner = getPlayer().getUniqueId().equals(chunkOwner);

                if (isCenter) lore.add(messages.guiMapMenuInsideThisChunk);

                if (chunkOwner != null) {
                    lore.add(guiChunkOwnerNameText(chunkNameOrUnknown(chunkOwner)));
                    if (enableClaimingFromMap && isOwner) {
                        lore.add("");
                        lore.add(messages.guiClickToUnclaim);
                    }
                } else {
                    lore.add(messages.guiNotClaimed);
                    if (enableClaimingFromMap) {
                        lore.add("");
                        lore.add(messages.guiClickToClaim);
                    }
                }

                String mapItemMaterialStr;
                if (isCenter) {
                    mapItemMaterialStr =
                            isOwner
                                    ? config.getGuiMapMenuCenterSelfClaimedItem()
                                    : chunkOwner == null
                                            ? config.getGuiMapMenuCenterUnclaimedItem()
                                            : config.getGuiMapMenuCenterOtherClaimedItem();
                } else {
                    mapItemMaterialStr =
                            isOwner
                                    ? config.getGuiMapMenuSelfClaimedItem()
                                    : chunkOwner == null
                                            ? config.getGuiMapMenuUnclaimedItem()
                                            : config.getGuiMapMenuOtherClaimedItem();
                }

                addInteractiveButton(
                        inventory,
                        slot,
                        materialFromStr(mapItemMaterialStr),
                        guiChunkPosText(offsetChunk),
                        lore,
                        (clickType, stack) -> {
                            if (clickType.isLeftClick()) {
                                // claimChunk.getMainHandler().claimChunk(getPlayer(), offsetChunk);
                                refresh();
                            } else if (clickType.isRightClick()) {
                                claimChunk
                                        .getMainHandler()
                                        .unclaimChunk(
                                                false,
                                                false,
                                                getPlayer(),
                                                offsetChunk.world(),
                                                offsetChunk.x(),
                                                offsetChunk.z());
                                refresh();
                            }
                        });
            }
        }
    }
}

package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.gui.GuiMenuScreen;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class MainMenu extends GuiMenuScreen {

    // +---------+
    // |XX2X4X6XX|
    // +---------+
    // 2: Current chunk info
    // 4: Chunk map
    // 6: Chunk permissions

    public MainMenu(ClaimChunk claimChunk) {
        super(claimChunk, 1, claimChunk.getMessages().guiMainMenuTitle);
    }

    @Override
    protected void onBuildGui() {
        if (player == null) return;

        addCurrentChunkItem(player);
        addMapItem();
        addPermsItem();
    }

    private void addCurrentChunkItem(@NotNull Player player) {
        ChunkPos chunkPos = new ChunkPos(player.getLocation().getChunk());
        UUID chunkOwner = claimChunk.getChunkHandler().getOwner(chunkPos);

        ArrayList<String> lore = new ArrayList<>();

        lore.add(guiChunkPosText(chunkPos));
        if (chunkOwner != null) {
            lore.add(guiChunkOwnerNameText(chunkNameOrUnknown(chunkOwner)));
            if (chunkOwner.equals(player.getUniqueId())) {
                lore.add("");
                lore.add(claimChunk.getMessages().guiClickToUnclaim);
            }
        } else {
            lore.add(claimChunk.getMessages().guiNotClaimed);
            lore.add("");
            lore.add(claimChunk.getMessages().guiClickToClaim);
        }

        addInteractiveButton(
                2,
                materialFromStr(claimChunk.getConfigHandler().getGuiMainMenuCurrentChunkItem()),
                claimChunk.getMessages().guiMainMenuCurrentChunkItemName,
                lore,
                (clickType, stack) -> {
                    if (clickType.isLeftClick()) {
                        claimChunk.getMainHandler().claimChunk(player, chunkPos);
                        refresh();
                    } else if (clickType.isRightClick()) {
                        claimChunk
                                .getMainHandler()
                                .unclaimChunk(
                                        false,
                                        false,
                                        player,
                                        chunkPos.world(),
                                        chunkPos.x(),
                                        chunkPos.z());
                        refresh();
                    }
                });
    }

    private void addMapItem() {
        addInteractiveButton(
                4,
                materialFromStr(claimChunk.getConfigHandler().getGuiMainMenuChunkMapItem()),
                claimChunk.getMessages().guiMainMenuMapItemName,
                Collections.singletonList(claimChunk.getMessages().guiMapDescription),
                (clickType, stack) ->
                        claimChunk
                                .getGuiHandler()
                                .openGui(Objects.requireNonNull(player), new MapMenu(claimChunk)));
    }

    private void addPermsItem() {
        addInteractiveButton(
                6,
                materialFromStr(claimChunk.getConfigHandler().getGuiMainMenuPermFlagsItem()),
                claimChunk.getMessages().guiMainMenuPermFlagsItemName,
                Collections.singletonList(claimChunk.getMessages().guiMainMenuPermFlagsDescription),
                (clickType, stack) -> {});
    }
}

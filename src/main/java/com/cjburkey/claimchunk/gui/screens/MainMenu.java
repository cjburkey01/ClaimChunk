package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.gui.GuiMenuScreen;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class MainMenu extends GuiMenuScreen {

    // +---------+
    // |XX2X4X6XX|
    // +---------+
    // 2: Current chunk info
    // 4: Chunk map
    // 6: Chunk permissions

    public MainMenu(ClaimChunk claimChunk, Player player) {
        super(claimChunk, player, 1, claimChunk.getMessages().guiMainMenuTitle);
    }

    @Override
    public void onOpen(@NotNull Inventory inventory) {
        addCurrentChunkItem(inventory);
        addMapItem(inventory);
        addPermsItem(inventory);
    }

    private void addCurrentChunkItem(@NotNull Inventory inventory) {
        ChunkPos chunkPos = new ChunkPos(getPlayer().getLocation().getChunk());
        UUID chunkOwner = claimChunk.getChunkHandler().getOwner(chunkPos);

        ArrayList<String> lore = new ArrayList<>();

        lore.add(guiChunkPosText(chunkPos));
        if (chunkOwner != null) {
            lore.add(guiChunkOwnerNameText(chunkNameOrUnknown(chunkOwner)));
            if (chunkOwner.equals(getPlayer().getUniqueId())) {
                lore.add("");
                lore.add(claimChunk.getMessages().guiClickToUnclaim);
            }
        } else {
            lore.add(claimChunk.getMessages().guiNotClaimed);
            lore.add("");
            lore.add(claimChunk.getMessages().guiClickToClaim);
        }

        addInteractiveButton(
                inventory,
                2,
                materialFromStr(claimChunk.getConfigHandler().getGuiMainMenuCurrentChunkItem()),
                claimChunk.getMessages().guiMainMenuCurrentChunkItemName,
                lore,
                (clickType, stack) -> {
                    if (clickType.isLeftClick()) {
                        claimChunk.getMainHandler().claimChunk(getPlayer(), chunkPos);
                        refresh();
                    } else if (clickType.isRightClick()) {
                        claimChunk
                                .getMainHandler()
                                .unclaimChunk(
                                        false,
                                        false,
                                        getPlayer(),
                                        chunkPos.world(),
                                        chunkPos.x(),
                                        chunkPos.z());
                        refresh();
                    }
                });
    }

    private void addMapItem(@NotNull Inventory inventory) {
        addInteractiveButton(
                inventory,
                4,
                materialFromStr(claimChunk.getConfigHandler().getGuiMainMenuChunkMapItem()),
                claimChunk.getMessages().guiMainMenuMapItemName,
                splitLineLore(claimChunk.getMessages().guiMainMenuMapItemDesc),
                (clickType, stack) -> openGui(new MapMenu(claimChunk, getPlayer())));
    }

    private void addPermsItem(@NotNull Inventory inventory) {
        addInteractiveButton(
                inventory,
                6,
                materialFromStr(claimChunk.getConfigHandler().getGuiMainMenuPermFlagsItem()),
                claimChunk.getMessages().guiMainMenuPermFlagsItemName,
                splitLineLore(claimChunk.getMessages().guiMainMenuPermFlagsDescription),
                (clickType, stack) -> openGui(new PermSelectMenu(claimChunk, getPlayer())));
    }
}

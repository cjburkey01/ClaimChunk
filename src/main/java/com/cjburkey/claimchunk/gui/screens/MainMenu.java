package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.gui.GuiMenuScreen;
import com.cjburkey.claimchunk.player.PlayerHandler;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainMenu extends GuiMenuScreen {

    // +---------+
    // |XX2X4X6XX|
    // +---------+
    // 2: Current chunk info
    // 4: Chunk map
    // 6: Chunk permissions

    public MainMenu(ClaimChunk claimChunk) {
        super(claimChunk, 1, "ClaimChunk Menu");
    }

    @Override
    protected void onBuildGui() {
        if (player == null) return;

        addCurrentChunkItem(player);
        addMapItem();
        addPermsItem();
    }

    private void addCurrentChunkItem(@NotNull Player player) {
        ChunkHandler chunkHandler = claimChunk.getChunkHandler();
        PlayerHandler playerHandler = claimChunk.getPlayerHandler();

        ChunkPos chunkPos = new ChunkPos(player.getLocation().getChunk());
        UUID chunkOwner = chunkHandler.getOwner(chunkPos);
        String chunkName = chunkOwner == null ? null : playerHandler.getChunkName(chunkOwner);
        if (chunkName == null && chunkOwner != null)
            chunkName = playerHandler.getUsername(chunkOwner);

        ArrayList<String> lore = new ArrayList<>();

        lore.add(
                claimChunk
                        .getMessages()
                        .guiChunkPos
                        .replaceAll(Pattern.quote("%%WORLD%%"), chunkPos.world())
                        .replaceAll(Pattern.quote("%%X%%"), chunkPos.x() + "")
                        .replaceAll(Pattern.quote("%%Z%%"), chunkPos.z() + ""));
        if (chunkOwner != null) {
            lore.add(
                    claimChunk
                            .getMessages()
                            .guiChunkOwner
                            .replaceAll(Pattern.quote("%%NAME%%"), chunkName));
            if (chunkOwner.equals(player.getUniqueId())) {
                lore.add("");
                lore.add(claimChunk.getMessages().guiUnclaim);
            }
        } else {
            lore.add(claimChunk.getMessages().guiNotClaimed);
            lore.add("");
            lore.add(claimChunk.getMessages().guiClaim);
        }

        addInteractiveButton(
                2,
                material(claimChunk.getConfigHandler().getGuiCurrentChunkItem()),
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
                material(claimChunk.getConfigHandler().getGuiChunkMapItem()),
                claimChunk.getMessages().guiMapItemName,
                Collections.singletonList(claimChunk.getMessages().guiMapDescription),
                (clickType, stack) -> {});
    }

    private void addPermsItem() {}

    private @NotNull Material material(String val) {
        Material item = Material.matchMaterial(val);
        if (item == null) {
            item = Material.BARRIER;
        }
        return item;
    }
}

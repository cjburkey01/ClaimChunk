package com.cjburkey.claimchunk.gui.screens;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.gui.GuiMenuScreen;

import org.bukkit.Material;

import java.util.ArrayList;

public class MapMenu extends GuiMenuScreen {

    private static final int MAP_HEIGHT = 5;

    public MapMenu(ClaimChunk claimChunk) {
        super(claimChunk, MAP_HEIGHT, claimChunk.getMessages().guiMapMenuTitle);
    }

    @Override
    protected void onBuildGui() {
        if (player == null) return;

        ChunkPos centerChunk = new ChunkPos(player.getLocation().getChunk());

        int halfHeight = Math.floorDiv(MAP_HEIGHT, 2);

        // Inventory width is 9, so go 4 eastward and westward
        for (int offsetX = -4; offsetX <= 4; offsetX++) {
            // Inventory height is `MAP_HEIGHT`, so go `halfHeight` northward and southward
            for (int offsetZ = -halfHeight; offsetZ <= halfHeight; offsetZ++) {
                int slot = (offsetX + 4) + 9 * (offsetZ + halfHeight);
                ChunkPos offsetChunk =
                        new ChunkPos(
                                centerChunk.world(),
                                centerChunk.x() + offsetX,
                                centerChunk.z() + offsetZ);

                // TODO: CHANGE MATERIAL

                addInteractiveButton(
                        slot,
                        Material.DIRT,
                        "&r&fChunk at " + offsetChunk,
                        new ArrayList<>(),
                        (clickType, stack) -> {});
            }
        }
    }
}

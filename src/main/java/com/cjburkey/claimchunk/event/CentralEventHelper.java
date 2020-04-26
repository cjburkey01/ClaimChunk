package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

// TODO: ADD PROTECTIONS
@SuppressWarnings("unused")
public final class CentralEventHelper {

    public static boolean getCanEdit(ClaimChunk claimChunk, @Nonnull Chunk chunk, @Nonnull UUID plyEditor) {
        // If the user is an admin, they have permission to override chunk claims.
        if (Utils.hasAdmin(Bukkit.getPlayer(plyEditor))) {
            return true;
        }

        // Glboal chunk handler
        final ChunkHandler CHUNK = claimChunk.getChunkHandler();

        // This chunk's owner
        final UUID PLY_OWNER = CHUNK.getOwner(chunk);

        // If the chunk isn't claimed, users can't edit if the server has
        // protections in unclaiemd chunks.
        if (PLY_OWNER == null) {
            return !claimChunk.chConfig().getBool("protection", "blockUnclaimedChunks");
        }

        // If the player is the owner, they can edit it. Obviously.
        if (PLY_OWNER.equals(plyEditor)) {
            return true;
        }

        // Check if the chunk is owned by an offline player and if players
        // should be allowed to edit in chunks with offline owners.
        boolean isOfflineAndUnprotected = claimChunk.chConfig().getBool("protection", "disableOfflineProtect")
                && Bukkit.getPlayer(PLY_OWNER) == null;

        // If the player has access or if the server allows editing offline
        // players' chunks, this player can edit.
        return claimChunk
                .getPlayerHandler()
                .hasAccess(PLY_OWNER, plyEditor)
                || isOfflineAndUnprotected;
    }

}

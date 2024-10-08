package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @since 0.0.26
 */
public class CmdSetPermFlag extends CCPlyAccessCmd {

    public CmdSetPermFlag(@NotNull ClaimChunk claimChunk, boolean forPlayer, boolean forChunk) {
        super(claimChunk, forPlayer, forChunk);
    }

    @Override
    protected boolean handleAccess(
            @NotNull Player caller,
            @Nullable UUID otherPlayer,
            @Nullable ChunkPos chunkPos,
            @NotNull String[] arguments) {
        return true;
    }

    @Override
    public @NotNull String getDescription() {
        V2JsonMessages msg = claimChunk.getMessages();
        return describe(
                msg.cmdPermFlagPlyChunkSet,
                msg.cmdPermFlagPlySet,
                msg.cmdPermFlagChunkSet,
                msg.cmdPermFlagGlobalSet);
    }
}

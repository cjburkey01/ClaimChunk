package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @since 1.0.0
 */
public class CmdClearPermFlag extends CCPlyAccessCmd {

    public CmdClearPermFlag(@NotNull ClaimChunk claimChunk, boolean forPlayer, boolean forChunk) {
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
                msg.cmdPermFlagPlyChunkClear,
                msg.cmdPermFlagPlyClear,
                msg.cmdPermFlagChunkClear,
                msg.cmdPermFlagGlobalClear);
    }
}

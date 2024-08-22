package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.0.26
 */
public class CmdViewPermFlag extends CCPlyAccessCmd {

    public CmdViewPermFlag(@NotNull ClaimChunk claimChunk, boolean isForPly, boolean isForChunk) {
        super(claimChunk, isForPly, isForChunk);
    }

    @Override
    public @NotNull String getDescription() {
        V2JsonMessages msg = claimChunk.getMessages();
        if (isForPly) {
            if (isForChunk) {
                return msg.cmdPermFlagPlyChunkList;
            } else {
                return msg.cmdPermFlagPlyList;
            }
        } else {
            if (isForChunk) {
                return msg.cmdPermFlagChunkList;
            } else {
                return msg.cmdPermFlagGlobalList;
            }
        }
    }

    @Override
    public boolean onCall(
            @NotNull String cmdUsed, @NotNull CommandSender executor, @NotNull String[] args) {
        // TODO: DO THIS

        return false;
    }
}

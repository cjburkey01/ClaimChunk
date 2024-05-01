package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @since 0.0.24
 */
public class RevokeAccessCmd extends CCSubCommand {

    public RevokeAccessCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, "access", true);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdRevokeAccess);
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg(claimChunk.getMessages().argPlayer, CCAutoComplete.OFFLINE_PLAYER),
            new CCArg(claimChunk.getMessages().argAllChunks, CCAutoComplete.BOOLEAN)
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        String localizedBooleanTrue = claimChunk.getMessages().argTypeBoolTrue;
        String localizedBooleanFalse = claimChunk.getMessages().argTypeBoolFalse;

        if (1 <= args.length) {
            String[] playersToRevoke = args[0].split(",");
            boolean allChunks = false;
            if (2 <= args.length
                    && (args[1].equalsIgnoreCase(localizedBooleanTrue)
                            || args[1].equalsIgnoreCase(localizedBooleanFalse))) {
                allChunks = args[1].equalsIgnoreCase(localizedBooleanTrue);
            }

            claimChunk.getMainHandler().revokeAccess((Player) executor, playersToRevoke, allChunks);
            return true;
        }
        return false;
    }
}

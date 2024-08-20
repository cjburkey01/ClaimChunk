package com.cjburkey.claimchunk.smartcommand.sub.ply;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.0.24
 */
@Deprecated
public class AccessCmd extends CCSubCommand {

    private static final String[] nonPlayerArguments =
            new String[] {
                "break",
                "place",
                "doors",
                "redstone",
                "interactVehicles",
                "interactEntities",
                "interactBlocks",
                "useContainers",
                "allChunks"
            };

    public AccessCmd(ClaimChunk claimChunk) {
        // TODO: CREATE `/chunk admin access <PLY>` to allow listing from
        //       console as well
        super(claimChunk, Executor.PLAYER, true, "player", "access");
    }

    @Override
    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdAccess;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg(claimChunk.getMessages().argPlayer, CCAutoComplete.OFFLINE_PLAYER),
            new CCArg(claimChunk.getMessages().argBreak, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argPlace, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argDoors, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argRedstone, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argInteractVehicles, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argInteractEntities, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argInteractBlocks, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argUseContainers, CCAutoComplete.PERMISSION),
            new CCArg(claimChunk.getMessages().argAllChunks, CCAutoComplete.PERMISSION)
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        if (1 <= args.length) {
            String[] newAccessors = args[0].split(",");
            Map<String, Boolean> nonPlayerArgs =
                    parseNonPlayerArgs(Arrays.copyOfRange(args, 1, args.length));
            if (nonPlayerArgs != null) {
                claimChunk.getMainHandler().accessChunk(player, newAccessors, nonPlayerArgs);
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    private Map<String, Boolean> parseNonPlayerArgs(String[] args) {
        HashMap<String, Boolean> arguments = new HashMap<>();

        String localizedBooleanTrue = claimChunk.getMessages().argTypeBoolTrue;
        String localizedBooleanFalse = claimChunk.getMessages().argTypeBoolFalse;

        for (String arg : args) {
            String[] argParts = arg.split(":");

            if (argParts.length == 2
                    && argParts[0] != null
                    && argParts[1] != null
                    && Arrays.stream(nonPlayerArguments)
                            .anyMatch(argParts[0]::equalsIgnoreCase) // argParts[0] is one of the
                    // expected arguments
                    && (argParts[1].equalsIgnoreCase(localizedBooleanTrue)
                            || argParts[1].equalsIgnoreCase(
                                    localizedBooleanFalse)) // argParts[1] is a valid boolean
            ) {
                arguments.put(argParts[0], argParts[1].equalsIgnoreCase(localizedBooleanTrue));
            } else {
                return null;
            }
        }
        return arguments;
    }
}

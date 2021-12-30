package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;

/** @since 0.0.23 */
public class ShowClaimedCmd extends CCSubCommand {

    public int maxSeconds = 60;
    public int maxRadius = 6;

    public ShowClaimedCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, "show-claimed", true);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdShow);
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg("radius", CCAutoComplete.NONE), new CCArg("seconds", CCAutoComplete.NONE)
        };
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var chunkPos = new ChunkPos(player.getLocation().getChunk());
        var showForSeconds = 5;
        var radius = 3;

        // Optional argument to show chunks within a given radius (up to the max defined)
        if (args.length >= 1) {
            try {
                radius = Integer.min(Integer.parseInt(args[0]), maxRadius);
            } catch (Exception e) {
                return false;
            }
        }
        // Optional argument to show for a given time (up to the max defined)
        if (args.length >= 2) {
            try {
                showForSeconds = Integer.min(Integer.parseInt(args[1]), maxSeconds);
            } catch (Exception e) {
                return false;
            }
        }

        // Create a set of this player's claimed chunks within the given radius
        HashSet<ChunkPos> claimedChunks = new HashSet<>();
        for (var x = chunkPos.getX() - radius; x <= chunkPos.getX() + radius; x++) {
            for (var z = chunkPos.getZ() - radius; z <= chunkPos.getZ() + radius; z++) {
                if (claimChunk
                        .getChunkHandler()
                        .isOwner(player.getWorld(), x, z, player.getUniqueId())) {
                    claimedChunks.add(new ChunkPos(player.getWorld().getName(), x, z));
                }
            }
        }

        // Use the new particle system!
        claimChunk.getChunkOutlineHandler().showChunksFor(claimedChunks, player, showForSeconds);
        return true;
    }
}

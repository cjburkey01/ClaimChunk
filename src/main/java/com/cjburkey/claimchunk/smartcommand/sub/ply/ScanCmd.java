package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;
import de.goldmensch.commanddispatcher.Executor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScanCmd extends CCSubCommand {

    public ScanCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, "scan");
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdClaim);
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
                new CCArg("scanDistance", CCAutoComplete.NONE),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        final Chunk playerChunk = player.getWorld().getChunkAt(player.getLocation());

        List<Chunk> nearbyChunks = new ArrayList<>();
        int near = claimChunk.chConfig().getNearChunkSearch();

        if(args.length > 0) {
            near = Integer.parseInt(args[1]);
        }

        if(near < 1) return true;

        int min = (near - 1) / 2;
        int max = (near - 1) / 2 + 1;

        for(int x1 = -min; x1 < max; x1++) {
            for(int z1 = -min; z1 < max; z1++) {

                Chunk chunk = player.getWorld().getChunkAt(x1 + playerChunk.getX(), z1 + playerChunk.getZ());

                if(claimChunk.getChunkHandler().isClaimed(chunk) && !claimChunk.getChunkHandler().isOwner(chunk, player))
                    nearbyChunks.add(chunk);
            }
        }

        messagePly(player, nearbyChunks.size() + " chunks found within a " + near + " chunk radius");
        return true;
    }
}
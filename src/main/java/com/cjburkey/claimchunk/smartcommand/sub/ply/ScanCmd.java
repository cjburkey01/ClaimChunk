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
        super(claimChunk, Executor.PLAYER, "scan", true);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdScan);
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
        final Player player = (Player) executor;
        final Chunk playerChunk = player.getWorld().getChunkAt(player.getLocation());

        List<Chunk> nearbyChunks = new ArrayList<>();
        int near = claimChunk.chConfig().getNearChunkSearch();

        if (args.length > 0 && isInteger(args[0], 10)) near = Integer.parseInt(args[0]);

        if (near > claimChunk.chConfig().getMaxScanRange()) {
            messagePly(
                    player,
                    claimChunk
                            .getMessages()
                            .scanInputTooBig
                            .replace(
                                    "%%MAXAREA%%",
                                    String.valueOf(claimChunk.chConfig().getMaxScanRange())));
            return true;
        }

        if (near < 1) return true;

        int min = (near - 1) / 2;
        int max = (near - 1) / 2 + 1;

        for (int x1 = -min; x1 < max; x1++) {
            for (int z1 = -min; z1 < max; z1++) {

                final Chunk chunk =
                        player.getWorld()
                                .getChunkAt(x1 + playerChunk.getX(), z1 + playerChunk.getZ());

                if (claimChunk.getChunkHandler().isClaimed(chunk)
                        && !claimChunk.getChunkHandler().isOwner(chunk, player))
                    nearbyChunks.add(chunk);
            }
        }

        messagePly(
                player,
                claimChunk
                        .getMessages()
                        .claimsFound
                        .replace("%%NEARBY%%", String.valueOf(nearbyChunks.size()))
                        .replace("%%RADIUS%%", String.valueOf(near)));
        return true;
    }

    public boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }
}

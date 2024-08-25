package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public abstract class CCPlyAccessCmd extends CCSubCommand {

    public final boolean forPlayer;
    public final boolean forChunk;

    public CCPlyAccessCmd(@NotNull ClaimChunk claimChunk, boolean forPlayer, boolean forChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "access");
        this.forPlayer = forPlayer;
        this.forChunk = forChunk;
    }

    protected abstract boolean handleAccess(
            @NotNull Player caller,
            @Nullable UUID otherPlayer,
            @Nullable ChunkPos chunkPos,
            @NotNull String[] arguments);

    @Override
    public abstract @NotNull String getDescription();

    @Override
    public CCArg[] getPermittedArguments() {
        var perm = new CCArg("permissions", CCAutoComplete.PERMISSION);
        if (forPlayer) {
            return new CCArg[] {new CCArg("otherPlayer", CCAutoComplete.OFFLINE_PLAYER), perm};
        }
        return new CCArg[] {perm};
    }

    @Override
    public int getRequiredArguments() {
        return forPlayer ? 2 : 1;
    }

    @Override
    public boolean onCall(
            @NotNull String cmdUsed, @NotNull CommandSender executor, @NotNull String[] args) {
        Player caller = (Player) executor;

        UUID otherPlayer = null;
        if (forPlayer) {
            String plyName = args[0];
            args = Arrays.copyOfRange(args, 1, args.length);
            otherPlayer = claimChunk.getPlayerHandler().getUUID(plyName);
            if (otherPlayer == null) {
                messagePly(caller, claimChunk.getMessages().noPlayer);
                return true;
            }
        }

        ChunkPos chunkPos = forChunk ? new ChunkPos(caller.getLocation().getChunk()) : null;
        return handleAccess(caller, otherPlayer, chunkPos, args);
    }

    protected final @NotNull String describe(
            @NotNull String plyChunk,
            @NotNull String ply,
            @NotNull String chunk,
            @NotNull String global) {
        if (forPlayer) {
            if (forChunk) {
                return plyChunk;
            } else {
                return ply;
            }
        } else {
            if (forChunk) {
                return chunk;
            } else {
                return global;
            }
        }
    }
}

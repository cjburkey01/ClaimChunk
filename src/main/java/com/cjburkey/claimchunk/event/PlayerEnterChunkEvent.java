package com.cjburkey.claimchunk.event;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/** An event broadcast to the server when a player enters a new chunk. */
public class PlayerEnterChunkEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private boolean cancelled;

    public final Player player;
    public final Chunk previousChunk;
    public final Chunk nextChunk;
    public final UUID previousOwner;
    public final UUID nextOwner;

    // Handy
    public final boolean chunksHaveSameOwner;
    public final boolean isPlayerPreviousOwner;
    public final boolean isPlayerNextOwner;

    /**
     * {@code previousChunk} and {@code nextChunk} should NEVER be the same, I WILL THROW A RUNTIME
     * EXCEPTION.
     *
     * @param player The player.
     * @param previousChunk The player's previous chunk.
     * @param nextChunk The player's next chunk.
     */
    public PlayerEnterChunkEvent(
            @NotNull Player player,
            @NotNull Chunk previousChunk,
            @NotNull Chunk nextChunk,
            @Nullable UUID previousOwner,
            @Nullable UUID nextOwner) {
        // I'm sorry, but I have to be sure :|
        if (previousChunk.getX() == nextChunk.getX() && previousChunk.getZ() == nextChunk.getZ()) {
            throw new RuntimeException("previousChunk and nextChunk must be different!!!!");
        }

        // The needies
        this.player = Objects.requireNonNull(player);
        this.previousChunk = Objects.requireNonNull(previousChunk);
        this.nextChunk = Objects.requireNonNull(nextChunk);
        this.previousOwner = previousOwner;
        this.nextOwner = nextOwner;

        // Calculate the handies
        this.chunksHaveSameOwner = Objects.equals(previousOwner, nextOwner);
        this.isPlayerPreviousOwner = player.getUniqueId().equals(previousOwner);
        this.isPlayerNextOwner = player.getUniqueId().equals(nextOwner);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

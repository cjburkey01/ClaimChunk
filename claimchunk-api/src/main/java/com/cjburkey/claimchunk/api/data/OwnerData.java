package com.cjburkey.claimchunk.api.data;

import com.cjburkey.claimchunk.api.owner.IChunkOwner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Represents a chunk owner that has data with the server. */
public class OwnerData {

    /** The owner that this class represents. */
    public final @NotNull IChunkOwner owner;

    /** The last-seen ingame-name for this player. */
    public final @NotNull String lastIgn;

    /** The display name for this player's chunks, or {@code null} if unset. */
    public @Nullable String chunkName;

    /** The last tick that this player was seen online. */
    public long lastOnlineTime;

    /** Whether this player should receive alerts when players enter/leave their chunks. */
    public boolean alert;

    /** The extra number of claims this player may have above the amount set for their role. */
    public int extraMaxClaims;

    public OwnerData(@NotNull IChunkOwner owner, @NotNull String lastIgn) {
        this.owner = owner;
        this.lastIgn = lastIgn;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OwnerData that)) return false;
        return Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(owner);
    }
}

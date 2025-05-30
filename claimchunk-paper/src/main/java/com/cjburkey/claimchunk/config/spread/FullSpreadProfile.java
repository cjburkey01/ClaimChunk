package com.cjburkey.claimchunk.config.spread;

import com.cjburkey.claimchunk.config.ccconfig.CCConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class FullSpreadProfile extends SpreadProfile {

    public boolean inClaimed = true;
    public boolean inUnclaimed = true;

    public FullSpreadProfile() {}

    // Clone
    @SuppressWarnings("unused")
    public FullSpreadProfile(FullSpreadProfile other) {
        super(other);

        this.inClaimed = other.inClaimed;
        this.inUnclaimed = other.inUnclaimed;
    }

    @Override
    public boolean getShouldCancel(@Nullable UUID sourceOwner, @Nullable UUID newOwner) {
        if (Objects.equals(sourceOwner, newOwner)) {
            // Disable block spread from unclaimed chunks to unclaimed chunks
            if (!inUnclaimed && sourceOwner == null) return true;

            // Disable block spread from claimed chunks into the same owner's chunks
            if (!inClaimed && sourceOwner != null) return true;
        }

        // Defer to general spread protection
        return super.getShouldCancel(sourceOwner, newOwner);
    }

    @Override
    public void toCCConfig(@NotNull CCConfig config, @NotNull String key) {
        super.toCCConfig(config, key);

        config.set(key + ".from_claimed.into_same_claimed", inClaimed);
        config.set(key + ".from_unclaimed.into_unclaimed", inUnclaimed);
    }

    @Override
    public void fromCCConfig(@NotNull CCConfig config, @NotNull String key) {
        super.fromCCConfig(config, key);

        inClaimed = config.getBool(key + ".from_claimed.into_same_claimed", inClaimed);
        inUnclaimed = config.getBool(key + ".from_unclaimed.into_unclaimed", inUnclaimed);
    }
}

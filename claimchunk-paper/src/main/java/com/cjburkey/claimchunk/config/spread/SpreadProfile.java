package com.cjburkey.claimchunk.config.spread;

import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.ICCConfigSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SpreadProfile implements ICCConfigSerializable {

    public boolean fromClaimedIntoDiffClaimed = true;
    public boolean fromClaimedIntoUnclaimed = true;
    public boolean fromUnclaimedIntoClaimed = true;

    public SpreadProfile() {}

    // Clone
    public SpreadProfile(SpreadProfile other) {
        this.fromClaimedIntoDiffClaimed = other.fromClaimedIntoDiffClaimed;
        this.fromClaimedIntoUnclaimed = other.fromClaimedIntoUnclaimed;
        this.fromUnclaimedIntoClaimed = other.fromUnclaimedIntoClaimed;
    }

    public boolean getShouldCancel(@Nullable UUID sourceOwner, @Nullable UUID newOwner) {
        // Full spread profile handles same-owner spreads
        if (!Objects.equals(sourceOwner, newOwner)) {
            // Disable block spread from unclaimed chunks into claimed chunks
            if (!fromUnclaimedIntoClaimed && sourceOwner == null) return true;

            // Disable block spread from claimed chunks into different claimed chunks
            if (!fromClaimedIntoDiffClaimed && newOwner != null && sourceOwner != null) return true;

            // Disable block spread from claimed chunks into unclaimed chunks
            //noinspection RedundantIfStatement
            if (!fromClaimedIntoUnclaimed && sourceOwner != null) return true;
        }

        // No need to cancel
        return false;
    }

    public void toCCConfig(@NotNull CCConfig config, @NotNull String key) {
        config.set(key + ".from_claimed.into_diff_claimed", fromClaimedIntoDiffClaimed);
        config.set(key + ".from_claimed.into_unclaimed", fromClaimedIntoUnclaimed);
        config.set(key + ".from_unclaimed.into_claimed", fromUnclaimedIntoClaimed);
    }

    public void fromCCConfig(@NotNull CCConfig config, @NotNull String key) {
        fromClaimedIntoDiffClaimed =
                config.getBool(key + ".from_claimed.into_diff_claimed", fromClaimedIntoDiffClaimed);
        fromClaimedIntoUnclaimed =
                config.getBool(key + ".from_claimed.into_unclaimed", fromClaimedIntoUnclaimed);
        fromUnclaimedIntoClaimed =
                config.getBool(key + ".from_unclaimed.into_claimed", fromUnclaimedIntoClaimed);
    }
}

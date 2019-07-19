package com.cjburkey.claimchunk.player;

import java.util.Objects;
import java.util.UUID;

public final class SimplePlayerData {

    public final UUID player;
    public final String lastIgn;
    public final long lastOnlineTime;

    public SimplePlayerData(UUID player, String lastIgn, long lastOnlineTime) {
        this.player = player;
        this.lastIgn = lastIgn;
        this.lastOnlineTime = lastOnlineTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplePlayerData that = (SimplePlayerData) o;
        return lastOnlineTime == that.lastOnlineTime &&
                player.equals(that.player) &&
                lastIgn.equals(that.lastIgn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, lastIgn, lastOnlineTime);
    }

}

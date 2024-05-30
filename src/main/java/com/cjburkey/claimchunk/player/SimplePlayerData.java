package com.cjburkey.claimchunk.player;

import java.util.UUID;

public record SimplePlayerData(UUID player, String lastIgn, long lastOnlineTime) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplePlayerData that = (SimplePlayerData) o;
        return lastOnlineTime == that.lastOnlineTime
                && player.equals(that.player)
                && lastIgn.equals(that.lastIgn);
    }
}

package com.cjburkey.claimchunk.player;

import java.util.UUID;

public class FullPlayerData implements Cloneable {

    public final UUID player;
    public final String lastIgn;
    public String chunkName;
    public long lastOnlineTime;
    public boolean alert;
    public int extraMaxClaims;

    public FullPlayerData(
            UUID player,
            String lastIgn,
            String chunkName,
            long lastOnlineTime,
            boolean alert,
            int extraMaxClaims) {
        this.player = player;
        this.lastIgn = lastIgn;
        this.chunkName = chunkName;
        this.lastOnlineTime = lastOnlineTime;
        this.alert = alert;
        this.extraMaxClaims = extraMaxClaims;
    }

    private FullPlayerData(FullPlayerData clone) {
        this(
                clone.player,
                clone.lastIgn,
                clone.chunkName,
                clone.lastOnlineTime,
                clone.alert,
                clone.extraMaxClaims);
    }

    public SimplePlayerData toSimplePlayer() {
        return new SimplePlayerData(player, lastIgn, lastOnlineTime);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public FullPlayerData clone() {
        return new FullPlayerData(this);
    }
}

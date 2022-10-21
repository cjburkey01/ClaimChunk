package com.cjburkey.claimchunk.player;

import java.util.Set;
import java.util.UUID;

public class Pre0024FullPlayerData {

    public final UUID player;
    public final String lastIgn;
    public final Set<UUID> permitted;
    public String chunkName;
    public long lastOnlineTime;
    public boolean alert;

    public Pre0024FullPlayerData(
            UUID player,
            String lastIgn,
            Set<UUID> permitted,
            String chunkName,
            long lastOnlineTime,
            boolean alert) {
        this.player = player;
        this.lastIgn = lastIgn;
        this.permitted = permitted;
        this.chunkName = chunkName;
        this.lastOnlineTime = lastOnlineTime;
        this.alert = alert;
    }
}

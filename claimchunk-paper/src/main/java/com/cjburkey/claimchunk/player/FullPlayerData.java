package com.cjburkey.claimchunk.player;

import com.cjburkey.claimchunk.data.sqlite.SqlDataPlayer;

import java.util.HashMap;
import java.util.UUID;

public class FullPlayerData {

    public final UUID player;
    public final String lastIgn;
    public String chunkName;
    public long lastOnlineTime;
    public boolean alert;
    public int extraMaxClaims;
    public final HashMap<String, Boolean> globalFlags;
    public final HashMap<UUID, HashMap<String, Boolean>> playerFlags;

    public FullPlayerData(
            UUID player,
            String lastIgn,
            String chunkName,
            long lastOnlineTime,
            boolean alert,
            int extraMaxClaims,
            HashMap<String, Boolean> globalFlags,
            HashMap<UUID, HashMap<String, Boolean>> playerFlags) {
        this.player = player;
        this.lastIgn = lastIgn;
        this.chunkName = chunkName;
        this.lastOnlineTime = lastOnlineTime;
        this.alert = alert;
        this.extraMaxClaims = extraMaxClaims;
        this.globalFlags = globalFlags;
        this.playerFlags = playerFlags;
    }

    public FullPlayerData(
            UUID player,
            String lastIgn,
            String chunkName,
            long lastOnlineTime,
            boolean alert,
            int extraMaxClaims) {
        this(
                player,
                lastIgn,
                chunkName,
                lastOnlineTime,
                alert,
                extraMaxClaims,
                new HashMap<>(),
                new HashMap<>());
    }

    public FullPlayerData(SqlDataPlayer player) {
        this(
                UUID.fromString(player.uuid),
                player.lastIgn,
                player.chunkName,
                player.lastOnlineTime,
                player.alert,
                player.extraMaxClaims);
    }

    public SimplePlayerData toSimplePlayer() {
        return new SimplePlayerData(player, lastIgn, lastOnlineTime);
    }
}

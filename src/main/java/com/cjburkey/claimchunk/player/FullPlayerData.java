package com.cjburkey.claimchunk.player;

import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.data.sqlite.SqlDataPlayer;

import java.util.UUID;

public class FullPlayerData implements Cloneable {

    public final UUID player;
    public final String lastIgn;
    public String chunkName;
    public long lastOnlineTime;
    public boolean alert;
    public int extraMaxClaims;
    public ChunkPlayerPermissions defaultChunkPermissions;

    public FullPlayerData(
            UUID player,
            String lastIgn,
            String chunkName,
            long lastOnlineTime,
            boolean alert,
            int extraMaxClaims, ChunkPlayerPermissions defaultChunkPermissions) {
        this.player = player;
        this.lastIgn = lastIgn;
        this.chunkName = chunkName;
        this.lastOnlineTime = lastOnlineTime;
        this.alert = alert;
        this.extraMaxClaims = extraMaxClaims;
        this.defaultChunkPermissions = defaultChunkPermissions;
    }

    public FullPlayerData(SqlDataPlayer player) {
        this(
                UUID.fromString(player.uuid),
                player.lastIgn,
                player.chunkName,
                player.lastOnlineTime,
                player.alert,
                player.extraMaxClaims, new ChunkPlayerPermissions(player.defaultChunkPermissions));
    }

    private FullPlayerData(FullPlayerData clone) {
        this(
                clone.player,
                clone.lastIgn,
                clone.chunkName,
                clone.lastOnlineTime,
                clone.alert,
                clone.extraMaxClaims, clone.defaultChunkPermissions);
    }

    public SimplePlayerData toSimplePlayer() {
        return new SimplePlayerData(player, lastIgn, lastOnlineTime);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public FullPlayerData clone() {
        return new FullPlayerData(this);
    }
}

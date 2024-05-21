package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.player.FullPlayerData;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "player_data")
public class SqlDataPlayer {

    @Id
    @Column(name = "player_uuid")
    public String uuid;

    @Column(name = "last_ign")
    public String lastIgn;

    @Column(name = "chunk_name")
    public String chunkName;

    @Column(name = "last_online_time")
    public long lastOnlineTime;

    @Column(name = "alerts_enabled")
    public boolean alert;

    @Column(name = "extra_max_claims")
    public int extraMaxClaims;

    @SuppressWarnings("unused")
    public SqlDataPlayer() {}

    public SqlDataPlayer(FullPlayerData player) {
        this.uuid = player.player.toString();
        this.lastIgn = player.lastIgn;
        this.chunkName = player.chunkName;
        this.lastOnlineTime = player.lastOnlineTime;
        this.alert = player.alert;
        this.extraMaxClaims = player.extraMaxClaims;
    }
}

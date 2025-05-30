package com.cjburkey.claimchunk.data.sqlite;

import org.sormula.annotation.Row;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "player_data")
@Row(tableName = "player_data")
public class SqlDataPlayer {

    @Id
    @Column(name = "player_uuid")
    @org.sormula.annotation.Column(primaryKey = true, name = "player_uuid")
    public String uuid;

    @Column(name = "last_ign")
    @org.sormula.annotation.Column(name = "lastIgn")
    public String lastIgn;

    @Column(name = "chunk_name")
    @org.sormula.annotation.Column(name = "chunk_name")
    public String chunkName;

    @Column(name = "last_online_time")
    @org.sormula.annotation.Column(name = "last_online_time")
    public long lastOnlineTime;

    @Column(name = "alerts_enabled")
    @org.sormula.annotation.Column(name = "alerts_enabled")
    public boolean alert;

    @Column(name = "extra_max_claims")
    @org.sormula.annotation.Column(name = "extra_max_claims")
    public int extraMaxClaims;
}

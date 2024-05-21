package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.chunk.DataChunk;

import javax.persistence.*;

@Table(name = "chunk_data")
public class SqlDataChunk {

    @Id
    @Column(name = "chunk_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int chunkId;

    @Column(name = "chunk_world")
    public String world;

    @Column(name = "chunk_x")
    public int x;

    @Column(name = "chunk_z")
    public int z;

    @Column(name = "owner_uuid")
    public String uuid;

    @SuppressWarnings("unused")
    public SqlDataChunk() {}

    public SqlDataChunk(DataChunk chunk) {
        this.world = chunk.chunk.world();
        this.x = chunk.chunk.x();
        this.z = chunk.chunk.z();
        this.uuid = chunk.player.toString();
    }
}

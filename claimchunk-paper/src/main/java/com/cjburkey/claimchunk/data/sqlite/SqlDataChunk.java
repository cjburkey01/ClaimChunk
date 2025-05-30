package com.cjburkey.claimchunk.data.sqlite;

@Table(name = "chunk_data")
public class SqlDataChunk {

    @Column(name = "chunk_world")
    String world;

    @Column(name = "chunk_x")
    int x;

    @Column(name = "chunk_z")
    int z;

    @Column(name = "owner_uuid")
    String uuid;
}

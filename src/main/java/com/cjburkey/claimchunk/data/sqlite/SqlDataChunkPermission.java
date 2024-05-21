package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;

import java.util.UUID;

import javax.persistence.*;

@Table(name = "chunk_permissions")
public class SqlDataChunkPermission {

    @Id
    @Column(name = "perm_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int permissionId;

    @Column(name = "chunk_id")
    public int chunkId;

    @Column(name = "other_player_uuid")
    public String otherPlayerUuid;

    @Column(name = "permission_bits")
    public int permissionBits;

    @SuppressWarnings("unused")
    public SqlDataChunkPermission() {}

    public SqlDataChunkPermission(UUID otherPlayer, ChunkPlayerPermissions permissions) {
        this.chunkId = -1;
        this.otherPlayerUuid = otherPlayer.toString();
        this.permissionBits = permissions.permissionFlags;
    }
}

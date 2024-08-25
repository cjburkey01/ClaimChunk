package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.chunk.ChunkPos;

import org.jetbrains.annotations.Nullable;
import org.sormula.annotation.Column;
import org.sormula.annotation.ImplicitType;
import org.sormula.annotation.Row;

import java.util.UUID;

@Row(tableName = "flag_permissions")
public class SqlFlagEntry {

    @Column(name = "player_uuid")
    @ImplicitType(translator = Translators.UUIDTranslator.class)
    public UUID playerUUID;

    @Column(name = "other_player_uuid")
    @ImplicitType(translator = Translators.UUIDTranslator.class)
    public @Nullable UUID otherPlayerUUID;

    @Column(name = "chunk_pos")
    @ImplicitType(translator = Translators.ChunkPosTranslator.class)
    public @Nullable ChunkPos chunkPos;

    @Column(name = "flag_name")
    public String flagName;

    @Column(name = "allow_deny")
    public boolean allowDeny;
}

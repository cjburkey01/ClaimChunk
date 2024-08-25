package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.chunk.ChunkPos;

import org.sormula.annotation.Column;
import org.sormula.annotation.ImplicitType;
import org.sormula.annotation.Row;

import java.util.UUID;

@Row(tableName = "claimed_chunks")
public class SqlClaimedChunk {

    @Column(primaryKey = true, name = "chunk_pos")
    @ImplicitType(translator = Translators.ChunkPosTranslator.class)
    public ChunkPos chunkPos;

    @Column(name = "owner_uuid")
    @ImplicitType(translator = Translators.UUIDTranslator.class)
    public UUID chunkOwner;
}

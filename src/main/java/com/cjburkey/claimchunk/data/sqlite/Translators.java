package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.chunk.ChunkPos;

import org.sormula.translator.TypeTranslator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public final class Translators {

    public record UUIDTranslator() implements TypeTranslator<UUID> {
        @Override
        public UUID read(ResultSet resultSet, int columnIndex) throws Exception {
            String str = resultSet.getString(columnIndex);
            if (str.isEmpty()) {
                return null;
            }

            return UUID.fromString(str);
        }

        @Override
        public void write(PreparedStatement preparedStatement, int parameterIndex, UUID parameter)
                throws Exception {
            preparedStatement.setString(
                    parameterIndex, parameter != null ? parameter.toString() : "");
        }
    }

    public record ChunkPosTranslator() implements TypeTranslator<ChunkPos> {
        @Override
        public ChunkPos read(ResultSet resultSet, int columnIndex) throws Exception {
            String val = resultSet.getString(columnIndex);
            if (val.isEmpty()) {
                return null;
            }

            String[] str = val.split(",");
            String world = str[0];
            int x = Integer.parseInt(str[1]);
            int z = Integer.parseInt(str[2]);
            return new ChunkPos(world, x, z);
        }

        @Override
        public void write(
                PreparedStatement preparedStatement, int parameterIndex, ChunkPos parameter)
                throws Exception {
            preparedStatement.setString(
                    parameterIndex,
                    parameter != null
                            ? String.format(
                                    "%s,%s,%s", parameter.world(), parameter.x(), parameter.z())
                            : "");
        }
    }
}

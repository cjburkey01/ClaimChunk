package com.cjburkey.claimchunk.data.sqlite;

import com.zaxxer.q2o.Q2Sql;
import com.zaxxer.q2o.SqlClosure;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** This class is responsible for creating, loading, and upgrading the database file. */
public class SqLiteTableMigrationManager {

    public static void go() {
        // Make tables if they don't exist
        tryCreateTables();
    }

    private static void tryCreateTables() {
        // Create player data table
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS player_data (
                    player_uuid TEXT PRIMARY KEY NOT NULL,
                    last_ign TEXT NOT NULL,
                    chunk_name TEXT,
                    last_online_time INTEGER NOT NULL,
                    alerts_enabled INTEGER NOT NULL,
                    extra_max_claims INTEGER NOT NULL
                ) STRICT
                """);

        // Chunk data table
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS chunk_data (
                    chunk_id INTEGER PRIMARY KEY,
                    chunk_world TEXT NOT NULL,
                    chunk_x INTEGER NOT NULL,
                    chunk_z INTEGER NOT NULL,
                    owner_uuid TEXT NOT NULL,

                    FOREIGN KEY(owner_uuid) REFERENCES player_data(player_uuid)
                ) STRICT
                """);

        // TODO: MIGRATE?
        // Granular chunk player permission table
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS chunk_permissions (
                    chunk_id INTEGER NOT NULL,
                    other_player_uuid TEXT NOT NULL,
                    permission_bits INTEGER NOT NULL,

                    PRIMARY KEY(chunk_id, other_player_uuid)
                    FOREIGN KEY(chunk_id) REFERENCES chunk_data(chunk_id),
                    FOREIGN KEY(other_player_uuid) REFERENCES player_data(player_uuid)
                ) STRICT
                """);

        // Create table for flags that players have enabled by default in their claims.
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS flags_player_default_enabled (
                    player_uuid TEXT NOT NULL,
                    flag_name TEXT NOT NULL,

                    PRIMARY KEY(player_uuid, flag_name)

                    FOREIGN KEY(player_uuid) REFERENCES player_data(player_uuid)
                ) STRICT
                """);

        // Create table for flags that players have enabled for specific players across their
        // chunks by default.
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS flags_player_other_player_enabled (
                    player_uuid TEXT NOT NULL,
                    other_player_uuid TEXT NOT NULL,
                    flag_name TEXT NOT NULL,

                    PRIMARY KEY(player_uuid, other_player_uuid, flag_name)

                    FOREIGN KEY(player_uuid) REFERENCES player_data(player_uuid),
                    FOREIGN KEY(other_player_uuid) REFERENCES player_data(player_uuid)
                ) STRICT
                """);

        // Create table for flags that owners have enabled for specific chunks
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS flags_player_chunk_enabled (
                    player_uuid TEXT NOT NULL,
                    chunk_id INTEGER NOT NULL,
                    flag_name TEXT NOT NULL,

                    PRIMARY KEY(player_uuid, chunk_id, flag_name)

                    FOREIGN KEY(player_uuid) REFERENCES player_data(player_uuid),
                    FOREIGN KEY(chunk_id) REFERENCES chunk_data(chunk_id)
                ) STRICT
                """);

        // Create table for flags that players have enabled for specific players in specific
        // chunks
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS flags_player_chunk_player_enabled (
                    player_uuid TEXT NOT NULL,
                    other_player_uuid TEXT NOT NULL,
                    chunk_id INTEGER NOT NULL,
                    flag_name TEXT NOT NULL,

                    PRIMARY KEY(player_uuid, other_player_uuid, chunk_id, flag_name)

                    FOREIGN KEY(player_uuid) REFERENCES player_data(player_uuid),
                    FOREIGN KEY(other_player_uuid) REFERENCES player_data(player_uuid),
                    FOREIGN KEY(chunk_id) REFERENCES chunk_data(chunk_id)
                ) STRICT
                """);
    }

    @SuppressWarnings("unused")
    public static boolean tableExists(String tableName) {
        return SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    SELECT COUNT(*) FROM sqlite_master
                                    WHERE type='table'
                                    AND name=?
                                    """)) {
                        statement.setString(1, tableName);
                        ResultSet resultSet = statement.executeQuery();
                        int count = resultSet.next() ? resultSet.getInt(1) : 0;
                        return count > 0;
                    }
                });
    }

    // Use this method to determine if a column exists in a table to perform migrations
    public static boolean columnExists(String tableName, String columnName) {
        return SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    SELECT COUNT(*) FROM pragma_table_info(?) WHERE name=?
                                    """)) {
                        statement.setString(1, tableName);
                        statement.setString(2, columnName);
                        ResultSet resultSet = statement.executeQuery();
                        int count = resultSet.next() ? resultSet.getInt(1) : 0;
                        return count > 0;
                    }
                });
    }

    // Whenever a column is added or moved or transformed or whatever, add a
    // method here to perform that transformation and call it in initialize_tables.

}

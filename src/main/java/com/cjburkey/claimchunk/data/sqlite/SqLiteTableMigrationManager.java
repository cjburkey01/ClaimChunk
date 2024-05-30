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

        // Call migration check methods here.
        // Migration method naming scheme:
        //   migrate_{{MAJOR}}_{{MINOR}}_{{PATCH}}_{{DISCRIMINATOR}}
        migrate_0_0_25_1();
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
                    extra_max_claims INTEGER NOT NULL,
                    default_chunk_permissions INTEGER NOT NULL
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
    }

    private static void migrate_0_0_25_1() {
        if (!columnExists("player_data", "default_chunk_permissions")) {
            Q2Sql.executeUpdate("""
                    ALTER TABLE player_data
                    ADD default_chunk_permissions INTEGER NOT NULL DEFAULT 0
                    """);
        }
    }

    // Use this method to determine if a column exists in a table to perform migrations
    @SuppressWarnings("unused")
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

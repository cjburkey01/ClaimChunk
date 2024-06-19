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
        //   migrate_{{MAJOR}}_{{MINOR}}_{{PATCH}}
        migrate_0_0_25();
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
                    default_local_permissions INTEGER,

                    FOREIGN KEY(owner_uuid)
                        REFERENCES player_data(player_uuid)
                        ON DELETE CASCADE
                ) STRICT
                """);

        // Granular chunk player permission table
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS chunk_permissions (
                    chunk_id INTEGER NOT NULL,
                    other_player_uuid TEXT NOT NULL,
                    permission_bits INTEGER NOT NULL,

                    PRIMARY KEY(chunk_id, other_player_uuid),

                    FOREIGN KEY(chunk_id)
                        REFERENCES chunk_data(chunk_id)
                        ON DELETE CASCADE,
                    FOREIGN KEY(other_player_uuid)
                        REFERENCES player_data(player_uuid)
                        ON DELETE CASCADE
                ) STRICT
                """);

        // Global data storage in case we need it later.
        // For now, stores an integer representing which version of the table schema we currently use.
        // This is in case we need to perform some change that can't be checked easily, such as foreign key constraints.
        if (!tableExists("claimchunk_global_info")) {
            Q2Sql.executeUpdate(
                    """
                    CREATE TABLE claimchunk_global_info (
                        key TEXT PRIMARY KEY NOT NULL,
                        value TEXT NOT NULL
                    ) STRICT
                    """);
            Q2Sql.executeUpdate(
                    """
                    INSERT INTO claimchunk_global_info
                        (key, value)
                        VALUES ("claimchunk_table_schema", "1")
                    """);
        }
    }

    // Whenever a column is added or moved or transformed or whatever, add a
    // method here to perform that transformation and call it in initialize_tables.
    // I've heard it is really difficult to perform column modification operations, so our best bet
    // in that scenario would be to create a temporary table, copy the data to it, delete and
    // recreate the table, then copy the data back whilst manually transforming the row data for the
    // changed column(s)

    private static void migrate_0_0_25() {
        if (!columnExists("player_data", "default_chunk_permissions")) {
            Q2Sql.executeUpdate(
                    """
                    ALTER TABLE player_data
                    ADD default_chunk_permissions INTEGER NOT NULL DEFAULT 0
                    """);
        }

        if (!columnExists("chunk_data", "default_local_permissions")) {
            Q2Sql.executeUpdate(
                    """
                    ALTER TABLE chunk_data
                    ADD default_local_permissions INTEGER DEFAULT NULL
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
                                    SELECT COUNT(*)
                                    FROM pragma_table_info(?)
                                    WHERE name=?
                                    """)) {
                        statement.setString(1, tableName);
                        statement.setString(2, columnName);
                        ResultSet resultSet = statement.executeQuery();
                        return resultSet.next() && resultSet.getInt(1) > 0;
                    }
                });
    }

    public static boolean tableExists(String tableName) {
        return SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                        SELECT COUNT(*)
                                        FROM sqlite_master
                                        WHERE type='table' AND name=?
                                        """)) {
                        statement.setString(1, tableName);
                        ResultSet resultSet = statement.executeQuery();
                        return resultSet.next() && resultSet.getInt(1) > 0;
                    }
                });
    }

    /**
     * @return The reported schema version in the ClaimChunk database, or {@code -1} if the value
     *     doesn't exist or isn't an integer.
     */
    public static int getSchemaVersion() {
        return SqlClosure.sqlExecute(
                connection -> {
                    try (ResultSet resultSet =
                            Q2Sql.executeQuery(
                                    connection,
                                    """
                                    SELECT value
                                    FROM claimchunk_global_info
                                    WHERE key="claimchunk_table_schema"
                                    """)) {
                        if (resultSet.next()) {
                            String strResult = resultSet.getString(1);
                            try {
                                // Parse int will fail on null, so don't gotta null check
                                return Integer.parseInt(strResult);
                            } catch (Exception ignored) {
                            }
                        }
                        return -1;
                    }
                });
    }
}

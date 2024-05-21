package com.cjburkey.claimchunk.data.sqlite;

import com.zaxxer.q2o.Q2Sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** This class is responsible for creating, loading, and upgrading the database file. */
public class SqLiteTableMigrationManager {

    public static void go() {
        // Make tables if they don't exist
        tryCreateTables();

        // Call migration check methods here.
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

        // Granular chunk player permission table
        Q2Sql.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS chunk_permissions (
                    perm_id INTEGER PRIMARY KEY,
                    chunk_id INTEGER NOT NULL,
                    other_player_uuid TEXT NOT NULL,
                    permission_bits INTEGER NOT NULL,

                    FOREIGN KEY(chunk_id) REFERENCES chunk_data(chunk_id),
                    FOREIGN KEY(other_player_uuid) REFERENCES player_data(player_uuid)
                ) STRICT
                """);
    }

    // Use this method to determine if a column exists in a table to perform migrations
    // TODO: MAYBE CHECK IF THIS WORKS
    @SuppressWarnings("unused")
    private static boolean columnExists(Connection connection, String tableName, String columnName)
            throws SQLException {
        PreparedStatement statement =
                connection.prepareCall(
                        """
                        SELECT COUNT(*) FROM pragma_table_info(?) WHERE name=?
                        """);
        statement.setString(1, tableName);
        statement.setString(2, columnName);
        ResultSet resultSet = statement.executeQuery();
        int count = resultSet.next() ? resultSet.getInt(1) : 0;
        return count > 0;
    }

    // Whenever a column is added or moved or transformed or whatever, add a
    // method here to perform that transformation and call it in initialize_tables.

}

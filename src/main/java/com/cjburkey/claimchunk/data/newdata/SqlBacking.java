package com.cjburkey.claimchunk.data.newdata;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class SqlBacking {

    private static final boolean SQL_DEBUG = Config.getBool("database", "printDebug");

    static MysqlConnection connect(String hostname,
                                   int port,
                                   String databaseName,
                                   String username,
                                   String password) throws ClassNotFoundException {
        // Make sure JDBC is loaded
        Class.forName("com.mysql.jdbc.Driver");

        // Create a connection with JDBC
        return new MysqlConnection(() -> {
            try {
                return DriverManager.getConnection(
                        String.format("jdbc:mysql://%s:%s/%s?useSSL=false", hostname, port, databaseName),
                        username,
                        password);
            } catch (SQLException e) {
                Utils.err("Failed to create MySQL connection");
                e.printStackTrace();
            }
            return null;
        });
    }

    static boolean getTableDoesntExist(MysqlConnection connection,
                                       String databaseName,
                                       String tableName) throws SQLException {
        String sql = "SELECT count(*) FROM information_schema.TABLES WHERE (`TABLE_SCHEMA` = ?) AND (`TABLE_NAME` = ?)";
        try (PreparedStatement statement = prep(connection, sql)) {
            statement.setString(1, databaseName);
            statement.setString(2, tableName);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1) <= 0;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    static boolean getColumnIsNullable(MysqlConnection connection,
                                       String tableName,
                                       String columnName) throws SQLException {
        String sql = "SELECT `IS_NULLABLE` FROM information_schema.COLUMNS WHERE (`TABLE_NAME` = ?) AND (`COLUMN_NAME` = ?)";
        try (PreparedStatement statement = prep(connection, sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() && results.getBoolean(1);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    static boolean getColumnExists(MysqlConnection connection,
                                   String dbName,
                                   String tableName,
                                   String columnName) throws SQLException {
        String sql = "SELECT count(*) FROM information_schema.COLUMNS " +
                "WHERE (`TABLE_SCHEMA` = ?) AND (`TABLE_NAME` = ?) AND (`COLUMN_NAME` = ?)";
        try (PreparedStatement statement = prep(connection, sql)) {
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            statement.setString(3, columnName);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() && results.getInt(1) > 0;
            }
        }
    }

    static PreparedStatement prep(MysqlConnection connection, String sql) throws SQLException {
        if (SQL_DEBUG) Utils.debug("Execute SQL: \"%s\"", sql);
        try {
            return connection.get().prepareStatement(sql);
        } catch (Exception e) {
            Utils.debug("Refreshing SQL connection");
            return connection.refreshGet().prepareStatement(sql);
        }
    }

}

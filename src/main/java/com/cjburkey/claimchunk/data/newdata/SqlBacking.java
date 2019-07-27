package com.cjburkey.claimchunk.data.newdata;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class SqlBacking {

    private static final boolean SQL_DEBUG = Config.getBool("database", "printDebug");

    static ConnectionSingleton connect(String hostname,
                                       int port,
                                       String databaseName,
                                       String username,
                                       String password) throws ClassNotFoundException {
        // Make sure JDBC is loaded
        Class.forName("com.mysql.jdbc.Driver");

        // Create a connection with JDBC
        return new ConnectionSingleton(() -> {
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

    static boolean getTableDoesntExist(ConnectionSingleton connection,
                                       String databaseName,
                                       String tableName) throws SQLException {
        String sql = "SELECT count(*) FROM information_schema.TABLES WHERE (TABLE_SCHEMA = ?) AND (TABLE_NAME = ?)";
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

    static PreparedStatement prep(ConnectionSingleton connection, String sql) throws SQLException {
        if (SQL_DEBUG) Utils.debug("Execute SQL: \"%s\"", sql);
        try {
            return connection.get().prepareStatement(sql);
        } catch (Exception e) {
            Utils.debug("Refreshing SQL connection");
            return connection.refreshGet().prepareStatement(sql);
        }
    }

}

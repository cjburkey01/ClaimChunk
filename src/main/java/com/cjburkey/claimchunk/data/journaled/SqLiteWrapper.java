package com.cjburkey.claimchunk.data.journaled;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqLiteWrapper {

    @Getter private final File dbFile;
    private Connection connection;

    public SqLiteWrapper(@NotNull File dbFile) {
        this.dbFile = dbFile;

        try {
            TableMigrationManager.go(this::connectionOrDie);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize tables!", e);
        }
    }

    public @NotNull Connection ensureConnection() throws RuntimeException, SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        try {
            if (!dbFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dbFile.createNewFile();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            return connection;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new file " + dbFile, e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Cannot find SQLite JDBC class? Not sure how this can happen. Please submit an"
                            + " issue on GitHub",
                    e);
        }
    }

    public @NotNull Connection connectionOrDie() {
        try {
            return ensureConnection();
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception", e);
        }
    }

    @SuppressWarnings("unused")
    public @Nullable Connection getOpenConnection() {
        return connection;
    }
}

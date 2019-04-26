package com.cjburkey.claimchunk.database;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * @since 0.0.6
 */
public class DatabaseConnect {

    private final String hostName;
    private final String database;
    private final String username;
    private final String password;
    private final int port;
    private Connection connection;

    public DatabaseConnect(String hostName, String database, String user, String pass, int port) {
        this.hostName = hostName;
        this.database = database;
        username = user;
        password = pass;
        this.port = port;
    }

    public boolean openConnection() throws SQLException {
        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return false;
            }
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Utils.err("MySQL driver not found.");
                return false;
            }
            String connect = "jdbc:mysql://" + hostName + ':' + port + '/' + database;
            connection = DriverManager.getConnection(connect, username, password);
            return true;
        }
    }

    public void putChunk(UUID uuid, ChunkPos pos) throws DatabaseException {
        if (connection == null) throw new DatabaseException("Not connected to a database!");
        try {
            Statement st = connection.createStatement();
            st.execute("INSERT INTO 'claimchunk' ('world', 'posX', 'posZ', 'ownerID') VALUES ('" + pos.getWorld()
                    + "', '" + pos.getX() + "', '" + pos.getZ() + "', '" + uuid.toString() + "');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ChunkPos> getChunksByOwner(UUID uuid) {
        ArrayList<ChunkPos> chunks = new ArrayList<>();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM 'claimchunk' WHERE ownerID = '" + uuid.toString() + "'");
            while (rs.next()) {
                ChunkPos chunkPos = new ChunkPos(rs.getString("world"), rs.getInt("posX"), rs.getInt("posZ"));
                chunks.add(chunkPos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    public void createTable() throws SQLException, DatabaseException {
        if (connection == null) throw new DatabaseException("Not connected to a database!");
        importSQL(connection, getClass().getResourceAsStream("/init.sql"));
    }

    private void importSQL(Connection conn, InputStream in) throws SQLException {
        Scanner s = new Scanner(in);
        s.useDelimiter("(;(\r)?\n)|((\r)?\n)?(--)?.*(--(\r)?\n)");
        Statement st = null;
        try {
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }
                if (line.trim().length() > 0) st.execute(line);
            }
        } finally {
            if (st != null) st.close();
            s.close();
        }
    }

}

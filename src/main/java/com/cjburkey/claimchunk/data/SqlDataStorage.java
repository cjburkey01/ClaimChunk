package com.cjburkey.claimchunk.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.database.DatabaseConnect;

public class SqlDataStorage<T> implements IDataStorage<T> {

    private final List<T> storage = new ArrayList<>();
    private final DatabaseConnect connect;

    public SqlDataStorage() {
        String host = Config.getString("database", "hostname");
        int port = Config.getInt("database", "port");
        String database = Config.getString("database", "database");
        String user = Config.getString("database", "username");
        String pass = Config.getString("database", "password");
        connect = new DatabaseConnect(host, database, user, pass, port);
        try {
            boolean worked = connect.openConnection();
            if (!worked) {
                Utils.log("&4Couldn't create SQL connection. Connection could not be made or JDBC could not be found.");
                Bukkit.getServer().getPluginManager().disablePlugin(ClaimChunk.getInstance());
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {

    }

    public void reloadData() {

    }

    public void addData(T data) {
        storage.add(data);
    }

    public void clearData() {
        storage.clear();
    }

    public List<T> getData() {
        return new ArrayList<>(storage);
    }

}
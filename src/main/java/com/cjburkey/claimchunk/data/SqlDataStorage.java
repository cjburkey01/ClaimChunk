package com.cjburkey.claimchunk.data;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.database.DatabaseConnect;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.bukkit.Bukkit;

// TODO: DOES NOTHING YET
public class SqlDataStorage<T> implements IDataStorage<T> {

    private final HashSet<T> storage = new HashSet<>();
    private final DatabaseConnect connection;

    public SqlDataStorage() {
        String host = Config.getString("database", "hostname");
        int port = Config.getInt("database", "port");
        String database = Config.getString("database", "database");
        String user = Config.getString("database", "username");
        String pass = Config.getString("database", "password");
        connection = new DatabaseConnect(host, database, user, pass, port);
        try {
            boolean worked = connection.openConnection();
            if (!worked) {
                Utils.err("Couldn't create SQL connection. Connection could not be made or JDBC could not be found.");
                Bukkit.getServer().getPluginManager().disablePlugin(ClaimChunk.getInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<T> getData() {
        return Collections.unmodifiableCollection(storage);
    }

    @Override
    public void addData(T data) {
        storage.add(data);
    }

    // TODO: IMPLEMENT DATA SAVING
    @Override
    public void saveData() {
    }

    // TODO: IMPLEMENT DATA LOADING
    @Override
    public void reloadData() {
    }

    @Override
    public void clearData() {
        storage.clear();
    }

}

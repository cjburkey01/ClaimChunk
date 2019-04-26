package com.cjburkey.claimchunk.dynmap;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import java.util.UUID;
import org.bukkit.Bukkit;

import static com.cjburkey.claimchunk.dynmap.DynmapApi.*;

/**
 * Created by CJ Burkey on 2019/04/17
 */
public class DynmapHandler {

    private static boolean loaded = false;

    public static boolean init() {
        try {
            if (Bukkit.getPluginManager().getPlugin("dynmap") == null) return false;
            return (loaded = _init());
        } catch (Throwable e) {
            Utils.err("An error occurred while initializing Dynmap integration");
            e.printStackTrace();
        }
        return false;
    }

    // TODO: ADD A METHOD TO ADD OR REMOVE A SINGLE CHUNK

    public static boolean updateChunks(UUID plyUuid, String name, ChunkPos[] chunks, int color) {
        try {
            return loaded && _updateChunks(plyUuid, name, chunks, color);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

}

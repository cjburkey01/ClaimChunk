package com.cjburkey.claimchunk.dynmap;

import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import com.cjburkey.claimchunk.ClaimChunk;

public class ClaimChunkDynmap {

    private DynmapAPI dynmap;

    public boolean registerAndSuch() {
        Plugin plugin = ClaimChunk.getInstance().getServer().getPluginManager().getPlugin("dynmap");
        if (plugin != null && plugin instanceof DynmapAPI) {
            dynmap = (DynmapAPI) plugin;
            return true;
        }
        return false;
    }

    public DynmapAPI getApi() {
        return dynmap;
    }

}
package com.cjburkey.claimchunk.dynmap;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

class DynmapApi {

    private static final HashMap<UUID, HashMap<String, AreaMarker>> markers = new HashMap<>();
    private static MarkerSet markerSet;

    static boolean _init() {
        DynmapCommonAPI dynmap = (DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmap == null) return false;
        if (!dynmap.markerAPIInitialized()) {
            throw new IllegalStateException("Dynmap marker API has not been initialized yet");
        }
        markerSet = dynmap.getMarkerAPI().createMarkerSet("claimchunk_dynamp_integration", "ClaimChunk", null, false);
        return true;
    }

    static boolean _updateChunks(UUID plyUuid, String name, ChunkPos[] chunks, int color) {
        return false;
    }

    private static AreaMarker createMarker(String world, UUID plyUuid, String name, int color) {
        AreaMarker marker = markerSet.createAreaMarker(String.format("ply_claimchunk_%s_%s", world, plyUuid), name, false, world, new double[0], new double[0], false);
        marker.setFillStyle(0.5d, color);
        marker.setRangeY(64.0d, 64.0d);

        HashMap<String, AreaMarker> plyMarkers = markers.computeIfAbsent(plyUuid, k -> new HashMap<>());
        plyMarkers.put(world, marker);
        return marker;
    }

}

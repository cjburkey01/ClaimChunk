package com.cjburkey.claimchunk.api.layer;

import com.cjburkey.claimchunk.api.IClaimChunkPlugin;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ClaimChunkLayerHandler {

    private final PriorityQueue<LayerEntry> layerQueue = new PriorityQueue<>(new LayerComparator());
    private final IClaimChunkPlugin claimChunk;

    public ClaimChunkLayerHandler(@NotNull IClaimChunkPlugin claimChunk) {
        this.claimChunk = claimChunk;
    }

    /**
     * Inserts a layer into the queue based on its order ID.
     *
     * @param layer The layer to insert.
     */
    public void insertLayer(@NotNull IClaimChunkLayer layer) {
        layerQueue.add(new LayerEntry(layer));
    }

    /**
     * Removes a layer from the queue.
     *
     * @param layer The layer to remove from the queue.
     */
    public void removeLayer(@NotNull IClaimChunkLayer layer) {
        layerQueue.removeIf(entry -> entry.layer.equals(layer));
    }

    /** Enables each layer. */
    public void onEnable() {
        layerQueue.forEach(entry -> entry.enabled = entry.layer.onEnable(claimChunk));
    }

    /** Disables each layer. */
    public void onDisable() {
        layerQueue.forEach(entry -> entry.layer.onDisable(claimChunk));
    }

    // Keeps track of whether the given layer is enabled and its sorting ID
    private static class LayerEntry {
        boolean enabled;
        final int orderId;
        final IClaimChunkLayer layer;

        LayerEntry(@NotNull IClaimChunkLayer layer) {
            orderId = layer.getOrderId();
            this.layer = layer;
        }
    }

    // Determines which layers should come first; this could probably be a lot simpler :/
    private record LayerComparator() implements Comparator<LayerEntry> {
        @Override
        public int compare(LayerEntry o1, LayerEntry o2) {
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return Integer.compare(o1.orderId, o2.orderId);
        }
    }
}

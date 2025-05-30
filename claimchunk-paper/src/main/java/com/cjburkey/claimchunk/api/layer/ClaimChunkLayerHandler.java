package com.cjburkey.claimchunk.api.layer;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.api.IClaimChunkPlugin;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;
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
     * @param <T> The type representing the layer.
     * @return {@code true} if the layer was inserted, or {@code false} if there is already a layer
     *     with that given class type.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public <T extends IClaimChunkLayer> boolean insertLayer(@NotNull T layer) {
        // Only register this layer if there isn't already a layer of this type present in the
        // queue.
        if (getLayer(layer.getClass()).isEmpty()) {
            layerQueue.add(new LayerEntry(layer));
            return true;
        }

        // Return false if a layer of this type is already in the queue.
        return false;
    }

    /**
     * Retrieves the layer for a given type. There should only ever be one layer for a given class.
     *
     * @param classType The type of class describing this layer.
     * @param <T> The type of layer
     * @return An optional containing the layer or empty if no layer of that type is registered.
     */
    public <T extends IClaimChunkLayer> @NotNull Optional<T> getLayer(@NotNull Class<T> classType) {
        return layerQueue.stream()
                .filter(entry -> entry.layer.getClass().equals(classType))
                .findFirst()
                .map(entry -> classType.cast(entry.layer));
    }

    /**
     * Removes a layer from the queue.
     *
     * @param classType The class for the type of layer to remove.
     * @param <T> The type representing the layer to remove.
     * @return Whether the layer was successfully removed.
     */
    @SuppressWarnings("unused")
    public <T extends IClaimChunkLayer> boolean removeLayer(@NotNull Class<T> classType) {
        return layerQueue.removeIf(entry -> entry.layer.getClass().equals(classType));
    }

    /** Enables each layer. */
    public void onEnable() {
        Utils.debug("Enabling ClaimChunk modular layer handler");
        layerQueue.forEach(entry -> entry.onEnable(claimChunk));
    }

    /** Disables each layer. */
    public void onDisable() {
        Utils.debug("Disabling ClaimChunk modular layer handler");
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

        void onEnable(@NotNull IClaimChunkPlugin claimChunk) {
            enabled = layer.onEnable(claimChunk);
            String name = layer.getClass().getSimpleName();
            Utils.debug(enabled ? "Enabled layer %s" : "Layer %s not enabled", name);
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

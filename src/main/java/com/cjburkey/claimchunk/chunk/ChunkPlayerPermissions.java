package com.cjburkey.claimchunk.chunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkPlayerPermissions {

    /**
     * The flags for each permission. These are combined into a single integer to save space in the
     * file. Format (from Rightmost (i.e. least significant) bit to leftmost): Break, Place, Doors,
     * Redstone, Ride Boats/Minecarts, interact with entities, interact with blocks, open containers
     */
    private int permissionFlags;

    public ChunkPlayerPermissions() {
        permissionFlags = 0;
    }

    public ChunkPlayerPermissions(final int permissionFlags) {
        this.permissionFlags = permissionFlags;
    }

    public boolean canBreak() {
        return (permissionFlags & 1) == 1;
    }

    public void allowBreak(final boolean allow) {
        if (allow) {
            permissionFlags |= 1;
        } else {
            permissionFlags &= ~1;
        }
    }

    public boolean canPlace() {
        return (permissionFlags & 2) == 2;
    }

    public void allowPlace(final boolean allow) {
        if (allow) {
            permissionFlags |= 2;
        } else {
            permissionFlags &= ~2;
        }
    }

    public boolean canUseDoors() {
        return (permissionFlags & 4) == 4;
    }

    public void allowUseDoors(final boolean allow) {
        if (allow) {
            permissionFlags |= 4;
        } else {
            permissionFlags &= ~4;
        }
    }

    public boolean canUseRedstone() {
        return (permissionFlags & 8) == 8;
    }

    public void allowUseRedstone(final boolean allow) {
        if (allow) {
            permissionFlags |= 8;
        } else {
            permissionFlags &= ~8;
        }
    }

    public boolean canUseVehicles() {
        return (permissionFlags & 16) == 16;
    }

    public void allowUseVehicles(final boolean allow) {
        if (allow) {
            permissionFlags |= 16;
        } else {
            permissionFlags &= ~16;
        }
    }

    public boolean canInteractEntities() {
        return (permissionFlags & 32) == 32;
    }

    public void allowInteractEntities(final boolean allow) {
        if (allow) {
            permissionFlags |= 32;
        } else {
            permissionFlags &= ~32;
        }
    }

    public boolean canInteractBlocks() {
        return (permissionFlags & 64) == 64;
    }

    public void allowInteractBlocks(final boolean allow) {
        if (allow) {
            permissionFlags |= 64;
        } else {
            permissionFlags &= ~64;
        }
    }

    public boolean canUseContainers() {
        return (permissionFlags & 128) == 128;
    }

    public void allowUseContainers(final boolean allow) {
        if (allow) {
            permissionFlags |= 128;
        } else {
            permissionFlags &= ~128;
        }
    }

    public int getPermissionFlags() {
        return permissionFlags;
    }

    public Map<String, Boolean> toPermissionsMap() {
        HashMap<String, Boolean> permissionsMap = new HashMap<>();

        permissionsMap.put("break", this.canBreak());
        permissionsMap.put("place", this.canPlace());
        permissionsMap.put("doors", this.canUseDoors());
        permissionsMap.put("redstone", this.canUseRedstone());
        permissionsMap.put("interactVehicles", this.canUseVehicles());
        permissionsMap.put("interactEntities", this.canInteractEntities());
        permissionsMap.put("interactBlocks", this.canInteractBlocks());
        permissionsMap.put("useContainers", this.canUseContainers());

        return permissionsMap;
    }

    public static ChunkPlayerPermissions fromPermissionsMap(Map<String, Boolean> permissions) {
        ChunkPlayerPermissions chunkPlayerPermissions = new ChunkPlayerPermissions();

        for (Map.Entry<String, Boolean> perm : permissions.entrySet()) {
            switch (perm.getKey()) {
                case "break" -> chunkPlayerPermissions.allowBreak(perm.getValue());
                case "place" -> chunkPlayerPermissions.allowPlace(perm.getValue());
                case "doors" -> chunkPlayerPermissions.allowUseDoors(perm.getValue());
                case "redstone" -> chunkPlayerPermissions.allowUseRedstone(perm.getValue());
                case "interactVehicles" -> chunkPlayerPermissions.allowUseVehicles(perm.getValue());
                case "interactEntities" ->
                        chunkPlayerPermissions.allowInteractEntities(perm.getValue());
                case "interactBlocks" ->
                        chunkPlayerPermissions.allowInteractBlocks(perm.getValue());
                case "useContainers" -> chunkPlayerPermissions.allowUseContainers(perm.getValue());
            }
        }

        return chunkPlayerPermissions;
    }
}

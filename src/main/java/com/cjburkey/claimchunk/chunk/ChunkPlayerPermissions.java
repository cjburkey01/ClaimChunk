package com.cjburkey.claimchunk.chunk;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ChunkPlayerPermissions {

    public static final class Masks {
        public static int BREAK = 1;
        public static int PLACE = 1 << 1;
        public static int DOOR = 1 << 2;
        public static int REDSTONE = 1 << 3;
        public static int VEHICLE = 1 << 4;
        public static int INTERACT_ENTITY = 1 << 5;
        public static int INTERACT_BLOCK = 1 << 6;
        public static int CONTAINERS = 1 << 7;
    }

    /**
     * The flags for each permission. These are combined into a single integer to save space in the
     * file. Format (from Rightmost (i.e. least significant) bit to leftmost): Break, Place, Doors,
     * Redstone, Ride Boats/Minecarts, interact with entities, interact with blocks, open containers
     */
    public int permissionFlags;

    public ChunkPlayerPermissions() {
        permissionFlags = 0;
    }

    public ChunkPlayerPermissions(final int permissionFlags) {
        this.permissionFlags = permissionFlags;
    }

    public boolean checkMask(int mask) {
        return (permissionFlags & mask) == mask;
    }

    private void setAllow(int mask, boolean allow) {
        if (allow) permissionFlags |= mask;
        else permissionFlags &= ~mask;
    }

    public boolean canBreak() {
        return checkMask(Masks.BREAK);
    }

    public void allowBreak(final boolean allow) {
        setAllow(Masks.BREAK, allow);
    }

    public boolean canPlace() {
        return checkMask(Masks.PLACE);
    }

    public void allowPlace(final boolean allow) {
        setAllow(Masks.PLACE, allow);
    }

    public boolean canUseDoors() {
        return checkMask(Masks.DOOR);
    }

    public void allowUseDoors(final boolean allow) {
        setAllow(Masks.DOOR, allow);
    }

    public boolean canUseRedstone() {
        return checkMask(Masks.REDSTONE);
    }

    public void allowUseRedstone(final boolean allow) {
        setAllow(Masks.REDSTONE, allow);
    }

    public boolean canUseVehicles() {
        return checkMask(Masks.VEHICLE);
    }

    public void allowUseVehicles(final boolean allow) {
        setAllow(Masks.VEHICLE, allow);
    }

    public boolean canInteractEntities() {
        return checkMask(Masks.INTERACT_ENTITY);
    }

    public void allowInteractEntities(final boolean allow) {
        setAllow(Masks.INTERACT_ENTITY, allow);
    }

    public boolean canInteractBlocks() {
        return checkMask(Masks.INTERACT_BLOCK);
    }

    public void allowInteractBlocks(final boolean allow) {
        setAllow(Masks.INTERACT_BLOCK, allow);
    }

    public boolean canUseContainers() {
        return checkMask(Masks.CONTAINERS);
    }

    public void allowUseContainers(final boolean allow) {
        setAllow(Masks.CONTAINERS, allow);
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

    public static @NotNull ChunkPlayerPermissions fromPermissionsMap(
            @NotNull Map<String, Boolean> permissions) {
        ChunkPlayerPermissions chunkPlayerPermissions = new ChunkPlayerPermissions();

        for (Map.Entry<String, Boolean> perm : permissions.entrySet()) {
            boolean permVal = perm.getValue();
            switch (perm.getKey()) {
                case "break" -> chunkPlayerPermissions.allowBreak(permVal);
                case "place" -> chunkPlayerPermissions.allowPlace(permVal);
                case "doors" -> chunkPlayerPermissions.allowUseDoors(permVal);
                case "redstone" -> chunkPlayerPermissions.allowUseRedstone(permVal);
                case "interactVehicles" -> chunkPlayerPermissions.allowUseVehicles(permVal);
                case "interactEntities" -> chunkPlayerPermissions.allowInteractEntities(permVal);
                case "interactBlocks" -> chunkPlayerPermissions.allowInteractBlocks(permVal);
                case "useContainers" -> chunkPlayerPermissions.allowUseContainers(permVal);
            }
        }

        return chunkPlayerPermissions;
    }
}

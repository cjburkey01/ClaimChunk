package com.cjburkey.claimchunk.config.access;

import com.cjburkey.claimchunk.Utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;

public class Accesses {

    // Entity access tracking
    public final HashMap<EntityType, EntityAccess> entityAccesses;
    public final HashMap<EntityType, EntityAccess> liveEntityAccesses;
    public final HashMap<String, EntityAccess> entityAccessClassMapping;

    // Block access tracking
    public final HashMap<Material, BlockAccess> blockAccesses;
    public final HashMap<Material, BlockAccess> liveBlockAccesses;
    public final HashMap<String, BlockAccess> blockAccessClassMapping;

    public Accesses(
            HashMap<EntityType, EntityAccess> entityAccesses,
            HashMap<String, EntityAccess> entityAccessClassMapping,
            HashMap<Material, BlockAccess> blockAccesses,
            HashMap<String, BlockAccess> blockAccessClassMapping) {
        this.entityAccesses = entityAccesses;
        this.entityAccessClassMapping = entityAccessClassMapping;
        this.liveEntityAccesses = new HashMap<>();

        this.blockAccesses = blockAccesses;
        this.blockAccessClassMapping = blockAccessClassMapping;
        this.liveBlockAccesses = new HashMap<>();
    }

    // Clone
    public Accesses(Accesses original) {
        this.entityAccesses = Utils.deepCloneMap(original.entityAccesses, EntityAccess::new);
        this.liveEntityAccesses =
                Utils.deepCloneMap(original.liveEntityAccesses, EntityAccess::new);
        this.entityAccessClassMapping =
                Utils.deepCloneMap(original.entityAccessClassMapping, EntityAccess::new);

        this.blockAccesses = Utils.deepCloneMap(original.blockAccesses, BlockAccess::new);
        this.liveBlockAccesses = Utils.deepCloneMap(original.liveBlockAccesses, BlockAccess::new);
        this.blockAccessClassMapping =
                Utils.deepCloneMap(original.blockAccessClassMapping, BlockAccess::new);
    }
}

package com.cjburkey.claimchunk.config.access;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;

public class Accesses {

    public final HashMap<EntityType, EntityAccess> entityAccesses;
    public final HashMap<Material, BlockAccess> blockAccesses;

    public Accesses(HashMap<EntityType, EntityAccess> entityAccesses, HashMap<Material, BlockAccess> blockAccesses) {
        this.entityAccesses = entityAccesses;
        this.blockAccesses = blockAccesses;
    }

}

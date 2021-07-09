package com.cjburkey.claimchunk.config.access;

import org.apache.commons.lang.SerializationUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;

public class Access implements Serializable {

    private static final long serialVersionUID = 2350992421225504418L;

    public final HashMap<EntityType, EntityAccess> entityAccesses;
    public final HashMap<Material, BlockAccess> blockAccesses;

    public Access(HashMap<EntityType, EntityAccess> entityAccesses, HashMap<Material, BlockAccess> blockAccesses) {
        this.entityAccesses = entityAccesses;
        this.blockAccesses = blockAccesses;
    }

    // Clone
    @SuppressWarnings("unchecked")
    public Access(Access original) {
        // Lazy deep copy
        this.entityAccesses = (HashMap<EntityType, EntityAccess>) SerializationUtils.clone(original.entityAccesses);
        this.blockAccesses = (HashMap<Material, BlockAccess>) SerializationUtils.clone(original.blockAccesses);
    }

}

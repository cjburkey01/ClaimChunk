package com.cjburkey.claimchunk.config.access;

import com.cjburkey.claimchunk.Utils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import java.util.HashMap;

public class Access {

    public final HashMap<EntityType, EntityAccess> entityAccesses;
    public final HashMap<Material, BlockAccess> blockAccesses;

    public Access(HashMap<EntityType, EntityAccess> entityAccesses, HashMap<Material, BlockAccess> blockAccesses) {
        this.entityAccesses = entityAccesses;
        this.blockAccesses = blockAccesses;
    }

    // Clone
    public Access(Access original) {
        this.entityAccesses = Utils.deepCloneMap(original.entityAccesses, EntityAccess::new);
        this.blockAccesses = Utils.deepCloneMap(original.blockAccesses, BlockAccess::new);
    }

}

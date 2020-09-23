package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A class that represents the permissions that players have in a given world.
 *
 * @since 0.0.23
 */
public class ClaimChunkWorldProfile {

    public final boolean enabled;

    public final Map<EntityType, Access<EntityAccess>> entityAccesses;
    public final Map<Material, Access<BlockAccess>> blockAccesses;

    // Private for builder-only access.
    // This constructor is a monster and it would be safer for everyone if
    // users were explicit about the values they set.
    public ClaimChunkWorldProfile(boolean enabled,
                                  @Nullable Map<EntityType, Access<EntityAccess>> entityAccesses,
                                  @Nullable Map<Material, Access<BlockAccess>> blockAccesses) {
        this.enabled = enabled;

        // Make sure the lists aren't null
        if (Objects.isNull(entityAccesses)) {
            entityAccesses = new HashMap<>(0);
        }
        if (Objects.isNull(blockAccesses)) {
            blockAccesses = new HashMap<>(0);
        }

        this.entityAccesses = entityAccesses;
        this.blockAccesses = blockAccesses;
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean canAccessEntity(boolean isOwned,
                                   boolean isOwnerOrAccess,
                                   @Nonnull Entity entity,
                                   @Nonnull EntityAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the entity access and determine if the player is allowed to
        // access it.
        return isOwnerOrAccess || checkEntityAccess(isOwned,
                                                    entity.getWorld().getName(),
                                                    entity.getType(),
                                                    accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkEntityAccess(boolean isClaimed,
                                      String worldName,
                                      @Nonnull EntityType entityType,
                                      @Nonnull EntityAccessType accessType) {
        // Get the entity access for this entity
        final Access<EntityAccess> entityAccess = getEntityAccess(worldName, entityType);

        // Select the correct branch
        final EntityAccess access = isClaimed
                                            ? entityAccess.claimedChunk
                                            : entityAccess.unclaimedChunks;

        // Check for the type of access
        return accessType.getShouldAllow.apply(access);
    }

    public Access<EntityAccess> getEntityAccess(String worldName, EntityType type) {
        // Try to get the access for this entity
        Access<EntityAccess> entityAccess = entityAccesses.get(type);
        if (entityAccess != null) {
            return entityAccess;
        }

        // If it fails, try to get the access for all entities
        entityAccess = entityAccesses.get(EntityType.UNKNOWN);
        if (entityAccess == null) {
            // Return an empty access and write an error in the console to alert
            // the server manager(s) that the default access is missing.
            Utils.err("ClaimChunk is missing the default access information for entities in the world \"%s\"!", worldName);
            Utils.err("Without this default access information, all entity events will be blocked for \"%s\"!", worldName);
            Utils.err("If you accidentally deleted it, you can re-add it to the world config for \"%s\" and set" +
                      "the \"entityType\" to \"UNKNOWN\" and set the desired permitted accesses.", worldName);
            return new Access<>(new EntityAccess(), new EntityAccess());
        }

        return  entityAccess;
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean canAccessBlock(boolean isOwned,
                                  boolean isOwnerOrAccess,
                                  String worldName,
                                  @Nonnull Material blockType,
                                  @Nonnull BlockAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own. Then check
        // for the entity access and determine if the player is allowed to
        // access it.
        return isOwnerOrAccess || checkBlockAccess(isOwned, worldName, blockType, accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkBlockAccess(boolean isClaimed,
                                     String worldName,
                                     @Nonnull Material blockType,
                                     @Nonnull BlockAccessType accessType) {
        // Get the block access for this block
        final Access<BlockAccess> blockAccess = getBlockAccess(worldName, blockType);

        // Select the correct branch
        final BlockAccess access = isClaimed
                                           ? blockAccess.claimedChunk
                                           : blockAccess.unclaimedChunks;

        // Check for the type of access
        return accessType.shouldAllow.apply(access);
    }

    private Access<BlockAccess> getBlockAccess(String worldName, Material blockType) {
        Access<BlockAccess> blockAccess = blockAccesses.get(blockType);
        if (blockAccess != null) {
            return blockAccess;
        }

        // Return an empty access and write an error in the console to alert
        // the server manager(s) that the default access is missing.
        Utils.err("ClaimChunk is missing the default access information for entities in the world \"%s\"!",
                  worldName);
        Utils.err("Without this default access information, all entity events will be blocked for \"%s\"!",
                  worldName);
        Utils.err("If you accidentally deleted it, you can re-add it to the world config for \"%s\" and set"
                  + "the \"entityType\" to \"UNKNOWN\" and set the desired permitted accesses.", worldName);
        return new Access<>(new BlockAccess(), new BlockAccess());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClaimChunkWorldProfile that = (ClaimChunkWorldProfile) o;
        return enabled == that.enabled
               && entityAccesses.equals(that.entityAccesses)
               && blockAccesses.equals(that.blockAccesses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, entityAccesses, blockAccesses);
    }

    public static class AccessBranch {
        public boolean allowInteract = false;
    }

    public static class Access<BranchType extends AccessBranch> {

        public final BranchType claimedChunk;
        public final BranchType unclaimedChunks;

        protected Access(@Nonnull BranchType claimedChunk, @Nonnull BranchType unclaimedChunks) {
            this.claimedChunk = claimedChunk;
            this.unclaimedChunks = unclaimedChunks;
        }

    }

    @SuppressWarnings("unused")
    public enum EntityAccessType {

        INTERACT(access -> access.allowInteract),
        DAMAGE(access -> access.allowDamage),
        EXPLODE(access -> access.allowExplosion),

        ;

        private final Function<EntityAccess, Boolean> getShouldAllow;

        EntityAccessType(Function<EntityAccess, Boolean> getShouldAllow) {
            this.getShouldAllow = getShouldAllow;
        }

    }

    public static class EntityAccess extends AccessBranch {

        public boolean allowDamage = false;
        public boolean allowExplosion = false;

        public EntityAccess() {}

        public EntityAccess(boolean allowInteract, boolean allowDamage, boolean allowExplosion) {
            this.allowInteract = allowInteract;
            this.allowDamage = allowDamage;
            this.allowExplosion = allowExplosion;
        }

    }

    @SuppressWarnings("unused")
    public enum BlockAccessType {

        INTERACT(access -> access.allowInteract),
        BREAK(access -> access.allowBreak),
        PLACE(access -> access.allowPlace),
        EXPLODE(access -> access.allowExplosion),

        ;

        private final Function<BlockAccess, Boolean> shouldAllow;

        BlockAccessType(Function<BlockAccess, Boolean> shouldAllow) {
            this.shouldAllow = shouldAllow;
        }

    }

    public static class BlockAccess extends AccessBranch {

        public boolean allowBreak = false;
        public boolean allowPlace = false;
        public boolean allowExplosion = false;

        public BlockAccess() {}

        public BlockAccess(boolean allowInteract, boolean allowBreak, boolean allowPlace, boolean  allowExplosion) {
            this.allowInteract = allowInteract;
            this.allowBreak = allowBreak;
            this.allowPlace = allowPlace;
            this.allowExplosion = allowExplosion;
        }

        public BlockAccess copy() {
            return new BlockAccess(allowInteract, allowBreak, allowPlace, allowExplosion);
        }

    }

}

package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * A class that represents the permissions that players have in a given world.
 *
 * @since 0.0.23
 */
public class ClaimChunkWorldProfile {

    /**
     * The world for which this profile manages ClaimChunk permissions.
     */
    public final String world;

    /**
     * Whether this world will be controlled at all by ClaimChunk
     */
    public final boolean enabled;

    /**
     * The entity interactions list.
     */
    public final Set<EntityAccess> entityAccesses;

    /**
     * The block interactions list.
     */
    public final Set<BlockAccess> blockAccesses;

    // Private for builder-only access.
    // This constructor is a monster and it would be safer for everyone if
    // users were explicit about the values they set.
    private ClaimChunkWorldProfile(@Nullable String world,
                                   boolean enabled,
                                   @Nullable Set<EntityAccess> entityAccesses,
                                   @Nullable Set<BlockAccess> blockAccesses) {
        this.world = world;
        this.enabled = enabled;

        // Make sure the lists aren't null
        if (Objects.isNull(entityAccesses)) {
            entityAccesses = new HashSet<>(0);
        }
        if (Objects.isNull(blockAccesses)) {
            blockAccesses = new HashSet<>(0);
        }

        this.entityAccesses = entityAccesses;
        this.blockAccesses = blockAccesses;
    }

    // Returns `true` if the player should be allowed to perform this action
    public boolean onEntityEvent(@Nullable UUID chunkOwner,
                                 @Nonnull Player accessor,
                                 boolean playerHasAccess,
                                 @Nonnull Entity entity,
                                 @Nonnull EntityAccessType accessType) {
        // If the chunk is claimed and the player has access, they can just
        // edit and interact with it as if it were their own.
        if (chunkOwner != null && playerHasAccess) {
            return true;
        }

        // Double check that this is in this same world
        // If not, just allow it
        if (!accessor.getWorld().getName().equals(world)
            || !entity.getWorld().getName().equals(world)) {
            return true;
        }

        // Check check for the entity access and determine if the player is
        // allowed to access it.
        return checkEntityAccess(chunkOwner != null,
                                 accessor,
                                 entity.getType(),
                                 accessType);
    }

    // Returns `true` if the player should be allowed to perform this action
    private boolean checkEntityAccess(boolean isClaimed,
                                      @Nonnull Player accessor,
                                      @Nonnull EntityType entityType,
                                      @Nonnull EntityAccessType accessType) {
        // Get the entity access for this entity
        final EntityAccess entityAccess = getEntityAccess(entityType);

        if (isClaimed) {
            if (accessType.equals(EntityAccessType.DAMAGE)) {
                return entityAccess.allowDamageClaimed;
            }
            if (accessType.equals(EntityAccessType.INTERACT)) {
                return entityAccess.allowInteractClaimed;
            }
        } else {
            if (accessType.equals(EntityAccessType.DAMAGE)) {
                return entityAccess.allowDamageUnclaimed;
            }
            if (accessType.equals(EntityAccessType.INTERACT)) {
                return entityAccess.allowInteractUnclaimed;
            }
        }

        // Something's wrong :O
        Utils.err("Player \"%s\" was denied access to \"%s\" the entity \"%s\" in world \"%s\" because it couldn't be determined whether they should be allowed to perform that action!",
                  accessor.getName(), accessType, entityType, world);
        Utils.err("This may or may not be a bug! If this behavior continues and is not desired, you may try deleting the world config for world \"%s\" to force it to regenerate", world);
        Utils.err("Note: obviously, deleting the config would erase all changes you (may) have made, so I would recommend you, instead, just rename it to something random like %s_backup.json or something.", world);
        Utils.err("If this behavior still continues after trying that, I would recommend you either create a GitHub issue at https://github.com/cjburkey01/Claimchunk/ or contact me on the Discord server for which you can find a link on that GitHub page.");
        return false;
    }

    private EntityAccess getEntityAccess(EntityType type) {
        for (EntityAccess entityAccess : entityAccesses) {
            if (entityAccess.entityType != null && entityAccess.entityType.equals(type)) {
                return entityAccess;
            }
        }

        // Return an empty access and write an error in the console to alert
        // the server manager(s) that the default access is missing.
        Utils.err("ClaimChunk is missing the default access information for entities in the world \"%s\"!",
                  world);
        Utils.err("Without this default access information, all entity events will be blocked for \"%s\"!",
                  world);
        Utils.err("If you accidentally deleted it, you can re-add it to the world config for \"%s\" and set"
                  + "the \"entityType\" to \"UNKNOWN\" and set the desired permitted accesses.", world);
        return new EntityAccess();
    }

    public ClaimChunkWorldProfile copyForWorld(String world) {
        return new ClaimChunkWorldProfile(world,
                                          this.enabled,
                                          this.entityAccesses,
                                          this.blockAccesses
        );
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
        return Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world);
    }

    /**
     * Creates a new {@link ClaimChunkWorldProfile.Builder}.
     *
     * @param world The world for which the build profile will act as a
     *              permissions lookup.
     * @return A new, non-{@code null} builder.
     */
    public static Builder newFromWorld(@Nonnull String world) {
        return new Builder(world);
    }

    protected static Builder newEmpty() {
        return new Builder(null);
    }

    // TODO: ADD MORE JAVADOCS
    /**
     * A builder for {@link ClaimChunkWorldProfile} to make creating instances
     * more user-friendly.
     *
     * @since 0.0.23
     */
    public static final class Builder {

        /**
         * The name of the world.
         */
        public final String world;

        // Data given to the profile
        private boolean enabled = true;
        private HashSet<EntityAccess> entitiesList = new HashSet<>();
        private HashSet<BlockAccess> blocksList = new HashSet<>();

        private Builder(@Nullable String world) {
            this.world = world;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder addEntityAccess(EntityAccess entityType) {
            entitiesList.add(entityType);
            return this;
        }

        public Builder addEntityAccesses(EntityAccess... entityType) {
            Collections.addAll(entitiesList, entityType);
            return this;
        }

        public Builder addBlockAccess(BlockAccess blockType) {
            blocksList.add(blockType);
            return this;
        }

        public Builder addBlockAccesses(BlockAccess... blockType) {
            Collections.addAll(blocksList, blockType);
            return this;
        }

        public @Nonnull ClaimChunkWorldProfile build() {
            return new ClaimChunkWorldProfile(world,
                                              enabled,
                                              entitiesList,
                                              blocksList);
        }

    }

    public enum EntityAccessType {

        INTERACT,
        DAMAGE,

    }

    public static class EntityAccess {

        public EntityType entityType = EntityType.UNKNOWN;
        public boolean allowInteractClaimed = false;
        public boolean allowDamageClaimed = false;
        public boolean allowInteractUnclaimed = false;
        public boolean allowDamageUnclaimed = false;

    }

    public enum BlockAccessType {

        INTERACT,
        BREAK,
        PLACE,

    }

    public static class BlockAccess {

        public Material blockType = Material.AIR;
        public boolean allowInteractClaimed = false;
        public boolean allowBreakClaimed = false;
        public boolean allowPlaceClaimed = false;
        public boolean allowInteractUnclaimed = false;
        public boolean allowBreakUnclaimed = false;
        public boolean allowPlaceUnclaimed = false;

    }

}

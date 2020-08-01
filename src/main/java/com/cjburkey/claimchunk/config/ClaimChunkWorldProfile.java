package com.cjburkey.claimchunk.config;

import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
     * Whether players interacting in unclaimed chunks should be treated like
     * players interacting with claimed chunks.
     */
    public final boolean treatUnclaimedLikeUnownedPlayers;

    /**
     * Whether the provided entity list is a list of entities for allowed
     * interactions or blocked interactions.
     *
     * When this is {@code true}, the entities in the list will be the
     * <b>only</b> entity interactions permitted within protected chunks.
     */
    public final boolean entitiesList;

    /**
     * Whether the provided block list is a list of blocks for allowed
     * interactions or blocked interactions.
     */
    public final boolean blocksList;

    /**
     * The allowed/denied entity interactions list.
     *
     * @see this#entitiesList
     */
    public final Set<EntityType> unowningPlayersEntities;

    /**
     * The allowed/denied block interactions list.
     *
     * @see this#blocksList
     */
    public final Set<BlockType> unowningPlayersBlocks;

    // Private for builder-only access.
    // This constructor is a monster and it would be safer for everyone if
    // users were explicit about the values they set.
    private ClaimChunkWorldProfile(@Nullable String world,
                                   boolean enabled,
                                   boolean treatUnclaimedLikeUnownedPlayers,
                                   boolean entitiesList,
                                   boolean blocksList,
                                   @Nullable Set<EntityType> unowningPlayersEntities,
                                   @Nullable Set<BlockType> unowningPlayersBlocks) {
        this.world = world;
        this.enabled = enabled;

        this.treatUnclaimedLikeUnownedPlayers = treatUnclaimedLikeUnownedPlayers;

        this.entitiesList = entitiesList;
        this.blocksList = blocksList;

        // Make sure the lists aren't null
        if (Objects.isNull(unowningPlayersEntities)) {
            unowningPlayersEntities = new HashSet<>(0);
        }
        if (Objects.isNull(unowningPlayersBlocks)) {
            unowningPlayersBlocks = new HashSet<>(0);
        }

        this.unowningPlayersEntities = unowningPlayersEntities;
        this.unowningPlayersBlocks = unowningPlayersBlocks;
    }

    public ClaimChunkWorldProfile copyForWorld(String world) {
        return new ClaimChunkWorldProfile(world,
                                          this.enabled,
                                          this.treatUnclaimedLikeUnownedPlayers,
                                          this.entitiesList,
                                          this.blocksList,
                                          this.unowningPlayersEntities,
                                          this.unowningPlayersBlocks);
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
        return treatUnclaimedLikeUnownedPlayers == that.treatUnclaimedLikeUnownedPlayers
               && entitiesList == that.entitiesList
               && blocksList == that.blocksList
               && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world,
                            treatUnclaimedLikeUnownedPlayers, entitiesList, blocksList
        );
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
        private boolean treatUnclaimedLikeUnownedPlayers = false;
        private boolean entitiesAllowedList = true;
        private boolean blocksAllowedList = true;
        private HashSet<EntityType> entitiesList = new HashSet<>();
        private HashSet<BlockType> blocksList = new HashSet<>();

        private Builder(@Nullable String world) {
            this.world = world;
        }

        /**
         * Whether players interacting in unclaimed chunks should be treated
         * like players interacting with claimed chunks.
         *
         * @param treatUnclaimedLikeUnownedPlayers {@code true} if unclaimed
         *                                         chunks should be protected
         * @return This builder for chaining.
         */
        public Builder setTreatUnclaimedLikeUnownedPlayers(boolean treatUnclaimedLikeUnownedPlayers) {
            this.treatUnclaimedLikeUnownedPlayers = treatUnclaimedLikeUnownedPlayers;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Whether the provided entities list represents a list of types of
         * entities with which players who don't have permission to edit within
         * the current chunk shouldn't be able to interact.
         *
         * @param entitiesAllowedList {@code true} if the entities list
         *                            represents entities with which un-owning
         *                            players should not be able to interact.
         * @return This builder for chaining.
         */
        public Builder setEntitiesListAllow(boolean entitiesAllowedList) {
            this.entitiesAllowedList = entitiesAllowedList;
            return this;
        }

        public Builder setBlocksListAllow(boolean blocksAllowedList) {
            this.blocksAllowedList = blocksAllowedList;
            return this;
        }

        public Builder addEntityType(EntityType entityType) {
            entitiesList.add(entityType);
            return this;
        }

        public Builder addEntityTypes(EntityType... entityType) {
            Collections.addAll(entitiesList, entityType);
            return this;
        }

        public Builder addBlockType(BlockType blockType) {
            blocksList.add(blockType);
            return this;
        }

        public Builder addBlockTypes(BlockType... blockType) {
            Collections.addAll(blocksList, blockType);
            return this;
        }

        public @Nonnull ClaimChunkWorldProfile build() {
            return new ClaimChunkWorldProfile(world,
                                              enabled,
                                              treatUnclaimedLikeUnownedPlayers,
                                              entitiesAllowedList,
                                              blocksAllowedList,
                                              entitiesList,
                                              blocksList);
        }

    }

}

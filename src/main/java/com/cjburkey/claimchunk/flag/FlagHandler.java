package com.cjburkey.claimchunk.flag;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlagHandler {

    private final CCPermFlags permFlags;
    private final IClaimChunkDataHandler dataHandler;

    public FlagHandler(
            @NotNull CCPermFlags permFlags, @NotNull IClaimChunkDataHandler dataHandler) {
        this.permFlags = permFlags;
        this.dataHandler = dataHandler;
    }

    public void setPermissionFlags(
            @NotNull UUID owner,
            @Nullable UUID accessor,
            @Nullable ChunkPos chunk,
            @NotNull HashMap<String, Boolean> newPerms) {
        dataHandler.setPermissionFlags(owner, accessor, chunk, newPerms);
    }

    public void clearPermissionFlags(
            @NotNull UUID owner,
            @Nullable UUID accessor,
            @Nullable ChunkPos chunk,
            @NotNull String... flagNames) {
        dataHandler.clearPermissionFlags(owner, accessor, chunk, flagNames);
    }

    public @NotNull Map<String, Boolean> getPlyFlags(
            @NotNull UUID owner, @Nullable UUID accessor, @Nullable ChunkPos chunk) {
        return dataHandler.getPlyFlags(owner, accessor, chunk);
    }

    public boolean queryProtectionSimple(
            @NotNull UUID chunkOwner,
            @Nullable UUID accessor,
            @NotNull ChunkPos chunkPos,
            @Nullable CCFlags.SimpleFlag simpleFlag) {
        if (simpleFlag != null) {
            ApplicableFlags applicableFlags = getApplicableFlags(chunkOwner, accessor, chunkPos);
            return doesProtect(applicableFlags, simpleFlag.name(), simpleFlag.protectWhen());
        }
        return false;
    }

    public boolean queryBlockProtection(
            @NotNull UUID chunkOwner,
            @Nullable UUID accessor,
            @NotNull ChunkPos chunkPos,
            @NotNull Material blockType,
            @NotNull CCFlags.BlockFlagType interactionType) {
        CCFlags.ProtectingFlag protectingFlag =
                permFlags.getProtectingFlag(blockType, interactionType);
        if (protectingFlag != null) {
            ApplicableFlags applicableFlags = getApplicableFlags(chunkOwner, accessor, chunkPos);
            return doesProtect(
                    applicableFlags,
                    protectingFlag.name(),
                    protectingFlag.flagData().protectWhen());
        }
        return false;
    }

    public boolean queryEntityProtection(
            @NotNull UUID chunkOwner,
            @Nullable UUID accessor,
            @NotNull ChunkPos chunkPos,
            @NotNull EntityType entityType,
            @NotNull CCFlags.EntityFlagType interactionType) {
        CCFlags.ProtectingFlag protectingFlag =
                permFlags.getProtectingFlag(entityType, interactionType);
        if (protectingFlag != null) {
            ApplicableFlags applicableFlags = getApplicableFlags(chunkOwner, accessor, chunkPos);
            return doesProtect(
                    applicableFlags,
                    protectingFlag.name(),
                    protectingFlag.flagData().protectWhen());
        }

        return false;
    }

    private @NotNull FlagHandler.FlagProtectResult checkFlag(
            @NotNull Map<String, Boolean> flagMap,
            @NotNull String flagName,
            @NotNull CCFlags.ProtectWhen protectWhen) {
        Boolean flagValue = flagMap.get(flagName);
        if (flagValue != null) {
            return protectWhen.doesProtect(flagValue)
                    ? FlagProtectResult.Protected
                    : FlagProtectResult.Unprotected;
        }
        return FlagProtectResult.Unspecified;
    }

    private ApplicableFlags getApplicableFlags(
            @NotNull UUID chunkOwner, @Nullable UUID accessor, @NotNull ChunkPos chunkPos) {
        Map<String, Boolean> chunkPlayerFlags =
                accessor == null ? null : getPlyFlags(chunkOwner, accessor, chunkPos);
        Map<String, Boolean> chunkFlags = getPlyFlags(chunkOwner, null, chunkPos);
        Map<String, Boolean> playerFlags =
                accessor == null ? null : getPlyFlags(chunkOwner, accessor, null);
        Map<String, Boolean> globalFlags = getPlyFlags(chunkOwner, null, null);

        return new ApplicableFlags(chunkPlayerFlags, chunkFlags, playerFlags, globalFlags);
    }

    private boolean doesProtect(
            ApplicableFlags applicableFlags, String flagName, CCFlags.ProtectWhen protectWhen) {
        FlagProtectResult result;

        if (applicableFlags.chunkPlayerFlags() != null) {
            result = checkFlag(applicableFlags.chunkPlayerFlags(), flagName, protectWhen);
            if (result.isSpecified()) {
                return result.doesProtect();
            }
        }

        result = checkFlag(applicableFlags.chunkFlags(), flagName, protectWhen);
        if (result.isSpecified()) {
            return result.doesProtect();
        }

        if (applicableFlags.playerFlags() != null) {
            result = checkFlag(applicableFlags.playerFlags(), flagName, protectWhen);
            if (result.isSpecified()) {
                return result.doesProtect();
            }
        }

        result = checkFlag(applicableFlags.globalFlags(), flagName, protectWhen);
        if (result.isSpecified()) {
            return result.doesProtect();
        }

        return false;
    }

    public enum FlagProtectResult {
        Protected,
        Unprotected,
        Unspecified;

        public boolean isSpecified() {
            return this != Unspecified;
        }

        public boolean doesProtect() {
            return doesProtect(true);
        }

        public boolean doesProtect(boolean ifUnspecified) {
            return switch (this) {
                case Protected -> true;
                case Unprotected -> false;
                case Unspecified -> ifUnspecified;
            };
        }
    }

    private record ApplicableFlags(
            @Nullable Map<String, Boolean> chunkPlayerFlags,
            @NotNull Map<String, Boolean> chunkFlags,
            @Nullable Map<String, Boolean> playerFlags,
            @NotNull Map<String, Boolean> globalFlags) {}
}

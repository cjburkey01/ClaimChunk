package com.cjburkey.claimchunk.flag;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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

    public @NotNull FlagProtectInfo queryProtectionSimple(
            @NotNull UUID chunkOwner,
            @Nullable UUID accessor,
            @NotNull ChunkPos chunkPos,
            @Nullable CCFlags.SimpleFlag simpleFlag) {
        if (simpleFlag != null) {
            ApplicableFlags applicableFlags = getApplicableFlags(chunkOwner, accessor, chunkPos);
            return doesProtect(applicableFlags, simpleFlag.name(), simpleFlag.protectWhen());
        }
        return FlagProtectInfo.unspecified();
    }

    public @NotNull FlagProtectInfo queryBlockProtection(
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
        return FlagProtectInfo.unspecified();
    }

    public @NotNull FlagProtectInfo queryEntityProtection(
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
        return FlagProtectInfo.unspecified();
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

    private @NotNull ApplicableFlags getApplicableFlags(
            @NotNull UUID chunkOwner, @Nullable UUID accessor, @NotNull ChunkPos chunkPos) {
        Map<String, Boolean> chunkPlayerFlags =
                accessor == null ? null : getPlyFlags(chunkOwner, accessor, chunkPos);
        Map<String, Boolean> chunkFlags = getPlyFlags(chunkOwner, null, chunkPos);
        Map<String, Boolean> playerFlags =
                accessor == null ? null : getPlyFlags(chunkOwner, accessor, null);
        Map<String, Boolean> globalFlags = getPlyFlags(chunkOwner, null, null);

        return new ApplicableFlags(chunkPlayerFlags, chunkFlags, playerFlags, globalFlags);
    }

    private @NotNull FlagProtectInfo doesProtect(
            ApplicableFlags applicableFlags, String flagName, CCFlags.ProtectWhen protectWhen) {
        FlagProtectResult result;

        final Optional<String> flagNameOptional = Optional.of(flagName);
        Function<FlagProtectResult, FlagProtectInfo> makeOutput =
                r -> new FlagProtectInfo(r, flagNameOptional);

        if (applicableFlags.chunkPlayerFlags() != null) {
            result = checkFlag(applicableFlags.chunkPlayerFlags(), flagName, protectWhen);
            if (result.isSpecified()) {
                return makeOutput.apply(result);
            }
        }

        result = checkFlag(applicableFlags.chunkFlags(), flagName, protectWhen);
        if (result.isSpecified()) {
            return makeOutput.apply(result);
        }

        if (applicableFlags.playerFlags() != null) {
            result = checkFlag(applicableFlags.playerFlags(), flagName, protectWhen);
            if (result.isSpecified()) {
                return makeOutput.apply(result);
            }
        }

        result = checkFlag(applicableFlags.globalFlags(), flagName, protectWhen);
        if (result.isSpecified()) {
            return makeOutput.apply(result);
        }

        return new FlagProtectInfo(FlagProtectResult.Unspecified, Optional.empty());
    }

    public record FlagProtectInfo(
            @NotNull FlagProtectResult result, @NotNull Optional<String> flagName) {
        public static FlagProtectInfo unspecified() {
            return new FlagProtectInfo(FlagProtectResult.Unspecified, Optional.empty());
        }
    }

    public enum FlagProtectResult {
        Protected,
        Unprotected,
        Unspecified;

        public boolean isSpecified() {
            return this != Unspecified;
        }

        // RETURNS `true` IF THE RESULT IS UNSPECIFIED
        //  DON'T EVER CHANGE THIS!
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

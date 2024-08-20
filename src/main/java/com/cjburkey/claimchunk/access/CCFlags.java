package com.cjburkey.claimchunk.access;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class CCFlags {

    // Methods named such that they may align with record getters :}
    public interface IFlagData<TypeEnum extends Enum<TypeEnum>> {
        @NotNull
        TypeEnum flagType();

        @NotNull
        FlagData flagData();
    }

    public enum BlockFlagType {
        BREAK,
        PLACE,
        INTERACT,
        EXPLODE,
    }

    public enum EntityFlagType {
        DAMAGE,
        INTERACT,
        EXPLODE,
    }

    public enum ProtectWhen {
        ENABLED,
        DISABLED;

        public boolean ifEnabled() {
            return this == ENABLED;
        }

        public boolean doesProtect(boolean isFlagEnabled) {
            if (ifEnabled()) {
                return isFlagEnabled;
            }
            return !isFlagEnabled;
        }
    }

    public record FlagData(
            ProtectWhen protectWhen, @NotNull Set<String> include, @NotNull Set<String> exclude) {}

    public record BlockFlagData(@NotNull BlockFlagType flagType, @NotNull FlagData flagData)
            implements IFlagData<BlockFlagType> {}

    public record EntityFlagData(@NotNull EntityFlagType flagType, @NotNull FlagData flagData)
            implements IFlagData<EntityFlagType> {}
}

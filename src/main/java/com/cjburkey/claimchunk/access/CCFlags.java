package com.cjburkey.claimchunk.access;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CCFlags {

    // Generics...gotta love 'em, but feel free to hate them too.
    // Methods named such that they may align with record getters :}
    public interface IFlagData<TypeEnum extends Enum<?>> {
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

    public record FlagData(@Nullable List<String> include, @Nullable List<String> exclude) {}

    public record BlockFlagData(@NotNull BlockFlagType flagType, @NotNull FlagData flagData)
            implements IFlagData<BlockFlagType> {}

    public record EntityFlagData(@NotNull EntityFlagType flagType, @NotNull FlagData flagData)
            implements IFlagData<EntityFlagType> {}
}

package com.cjburkey.claimchunk.flag;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record YmlTypeProtection(
        @NotNull YmlFlagFor protectFor,
        @NotNull YmlProtectType protectType,
        @NotNull CCFlags.ProtectWhen protectWhen,
        @Nullable String denyMessage,
        @Nullable List<String> include,
        @Nullable List<String> exclude)
        implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of();
    }

    public static YmlTypeProtection deserialize(@NotNull Map<String, Object> map) {

        return null;
    }
}

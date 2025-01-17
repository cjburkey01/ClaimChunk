package com.cjburkey.claimchunk.flag;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record YmlFlagProtection(
        @NotNull Map<YmlFlagFor, @NotNull List<YmlTypeProtection>> protections)
        implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of();
    }

    public static YmlFlagProtection deserialize(@NotNull Map<String, Object> map) {
        return null;
    }
}

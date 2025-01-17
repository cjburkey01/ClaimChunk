package com.cjburkey.claimchunk.flag;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record YmlPermissionFlag(@NotNull List<YmlFlagProtection> protections)
        implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of();
    }

    public static YmlPermissionFlag deserialize(@NotNull Map<String, Object> map) {
        return null;
    }
}

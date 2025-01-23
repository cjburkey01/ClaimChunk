package com.cjburkey.claimchunk.newflag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * @since 0.0.26
 */
public record YmlPermissionEffect(
        @NotNull YmlEffectTarget effectFor,
        @NotNull YmlEffectType effectType,
        @Nullable YmlProtectWhen protectWhen,
        @Nullable String denyMessage,
        @Nullable List<String> include,
        @Nullable List<String> exclude) {
}

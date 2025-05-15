package com.cjburkey.claimchunk.newflag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @since 1.0.0
 */
public record YmlPermissionFlag(
        @Nullable String denyMessage,
        boolean adminOnly,
        @NotNull List<YmlPermissionEffect> effects) {}

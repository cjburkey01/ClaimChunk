package com.cjburkey.claimchunk.newflag;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 1.0.0
 */
public class FlagEventDelegate {

    @NotNull
    EventResult checkEventProtected(
            @NotNull YmlEffectTarget target,
            @NotNull YmlEffectType type,
            @Nullable Player actor,
            @NotNull Material blockType) {
        throw new RuntimeException();
    }

    public enum FlagResultType {
        ALLOW,
        DENY,
        UNSPECIFIED,
    }

    public record EventResult(@NotNull FlagResultType result, @Nullable String denyMessage) {
        public static @NotNull EventResult allowed() {
            return new EventResult(FlagResultType.ALLOW, null);
        }

        public static @NotNull EventResult denied(@Nullable String denyMessage) {
            return new EventResult(FlagResultType.DENY, denyMessage);
        }

        public static @NotNull EventResult unspecified() {
            return new EventResult(FlagResultType.UNSPECIFIED, null);
        }
    }
}

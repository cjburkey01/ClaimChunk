package com.cjburkey.claimchunk.newflag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @since 0.0.26
 */
public final class YmlFlagParser {

    public static @NotNull YmlPermissionFlag parsePermissionFlag(@NotNull Map<String, Object> map) {
        @Nullable
        String denyMessage =
                switch (map.get("denyMessage")) {
                    case null -> null;
                    case String msg -> msg;
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for flag deny message: " + other);
                };

        boolean adminOnly =
                switch (map.get("adminOnly")) {
                    case null -> false;
                    case Boolean bool -> bool;
                    case String b -> Boolean.parseBoolean(b);
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for flag admin only bool: " + other);
                };

        @SuppressWarnings("unchecked")
        @NotNull
        List<Map<String, Object>> effectMapList =
                switch (map.get("effects")) {
                    case null -> throw new NullPointerException("Flag missing effects map");
                    case List<?> m -> (List<Map<String, Object>>) m;
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for flag effects list: " + other);
                };
        List<YmlPermissionEffect> effects =
                effectMapList.stream().map(YmlFlagParser::parsePermissionEffect).toList();

        return new YmlPermissionFlag(denyMessage, adminOnly, effects);
    }

    private static @NotNull YmlPermissionEffect parsePermissionEffect(
            @NotNull Map<String, Object> map) throws RuntimeException {
        @NotNull
        YmlEffectTarget effectFor =
                switch (map.get("for")) {
                    case null ->
                            throw new NullPointerException(
                                    "Permission effect missing \"for\" target");
                    case String target -> YmlEffectTarget.valueOf(target);
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for effect target: " + other);
                };

        @NotNull
        YmlEffectType effectType =
                switch (map.get("type")) {
                    case null ->
                            throw new NullPointerException("Permission effect missing \"type\"");
                    case String type -> YmlEffectType.valueOf(type);
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for effect type: " + other);
                };

        @Nullable
        YmlProtectWhen protectWhen =
                switch (map.get("protectWhen")) {
                    case null -> null;
                    case String t -> YmlProtectWhen.valueOf(t);
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for effect protectWhen: " + other);
                };

        @Nullable
        String denyMessage =
                switch (map.get("denyMessage")) {
                    case null -> null;
                    case String msg -> msg;
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for effect deny message: " + other);
                };

        @SuppressWarnings("unchecked")
        @Nullable
        List<String> include =
                switch (map.get("include")) {
                    case null -> null;
                    case List<?> list -> (List<String>) list;
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for effect include: " + other);
                };

        @SuppressWarnings("unchecked")
        @Nullable
        List<String> exclude =
                switch (map.get("exclude")) {
                    case null -> null;
                    case List<?> list -> (List<String>) list;
                    case Object other ->
                            throw new IllegalArgumentException(
                                    "Incorrect type for effect exclude: " + other);
                };

        return new YmlPermissionEffect(
                effectFor, effectType, protectWhen, denyMessage, include, exclude);
    }
}

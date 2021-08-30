package com.cjburkey.claimchunk.config.access;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.ICCConfigSerializable;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Pattern;

public class BlockAccess implements ICCConfigSerializable {

    public enum BlockAccessType {
        INTERACT(access -> access.allowInteract),
        BREAK(access -> access.allowBreak),
        PLACE(access -> access.allowPlace),
        EXPLODE(access -> access.allowExplosion),
        ;

        private final Function<BlockAccess, Boolean> shouldAllow;

        BlockAccessType(Function<BlockAccess, Boolean> shouldAllow) {
            this.shouldAllow = shouldAllow;
        }

        public boolean getShouldAllow(BlockAccess access) {
            return shouldAllow.apply(access);
        }
    }

    public boolean allowBreak;
    public boolean allowPlace;
    public boolean allowInteract;
    public boolean allowExplosion;

    public BlockAccess(
            boolean allowInteract, boolean allowBreak, boolean allowPlace, boolean allowExplosion) {
        update(allowInteract, allowBreak, allowPlace, allowExplosion);
    }

    // Clone
    public BlockAccess(BlockAccess other) {
        this(other.allowInteract, other.allowBreak, other.allowPlace, other.allowExplosion);
    }

    public BlockAccess() {
        this(false, false, false, false);
    }

    public void update(
            boolean allowInteract, boolean allowBreak, boolean allowPlace, boolean allowExplosion) {
        this.allowBreak = allowBreak;
        this.allowPlace = allowPlace;
        this.allowInteract = allowInteract;
        this.allowExplosion = allowExplosion;
    }

    @Override
    public void toCCConfig(@NotNull CCConfig config, @NotNull String key) {
        config.set(
                key,
                String.format(
                        "B:%s P:%s I:%s E:%s",
                        allowBreak, allowPlace, allowInteract, allowExplosion));
    }

    @Override
    public void fromCCConfig(@NotNull CCConfig config, @NotNull String key) {
        // Get the value
        String value = config.getStr(key);
        if (value == null) return;
        value = value.trim();
        if (value.isEmpty()) return;

        // Check if we need to parse the old way
        if (value.length() == 4 && Pattern.matches("[#.]{4}", value)) {
            char[] vals = value.toCharArray();
            allowBreak = vals[0] == '#';
            allowExplosion = vals[1] == '#';
            allowInteract = vals[2] == '#';
            allowPlace = vals[3] == '#';

            // Update to the new format!
            toCCConfig(config, key);

            Utils.log("Updated config from \"%s\" to \"%s\"", value, config.getStr(key));
        } else {
            // Parse the new way by looping through each property in this value
            for (String prop : value.split("\\s+")) {
                // Skip emptiness
                if (prop == null) continue;
                prop = prop.trim();
                if (prop.isEmpty()) continue;

                // Split by the colon
                String[] split = prop.split(":");

                // Make sure there is a name and a value
                if (split.length != 2) {
                    Utils.err(
                            "Failed to parse property \"%s\" from config file for key: \"%s\" from"
                                + " string \"%s\"",
                            prop, key, value);
                    return;
                }

                // Trim for consistency
                split[0] = split[0].trim();
                split[1] = split[1].trim();

                // Determine if players will be allowed to override this value
                @SuppressWarnings("unused")
                boolean isStatic =
                        split[0].contains("+")
                                && split[0].substring(split[0].indexOf('+'))
                                        .equalsIgnoreCase("static");

                // Assign values
                if (split[0].equalsIgnoreCase("B")) allowBreak = Boolean.parseBoolean(split[1]);
                if (split[0].equalsIgnoreCase("E")) allowExplosion = Boolean.parseBoolean(split[1]);
                if (split[0].equalsIgnoreCase("I")) allowInteract = Boolean.parseBoolean(split[1]);
                if (split[0].equalsIgnoreCase("P")) allowPlace = Boolean.parseBoolean(split[1]);
            }
        }
    }

    @Override
    public String toString() {
        return "BlockAccess{"
                + "allowInteract="
                + allowInteract
                + ", allowBreak="
                + allowBreak
                + ", allowPlace="
                + allowPlace
                + ", allowExplosion="
                + allowExplosion
                + '}';
    }
}

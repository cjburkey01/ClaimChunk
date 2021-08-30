package com.cjburkey.claimchunk.config.access;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.ICCConfigSerializable;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Pattern;

public class EntityAccess implements ICCConfigSerializable {

    public enum EntityAccessType {
        INTERACT(access -> access.allowInteract),
        DAMAGE(access -> access.allowDamage),
        EXPLODE(access -> access.allowExplosion),
        ;

        private final Function<EntityAccess, Boolean> shouldAllow;

        EntityAccessType(Function<EntityAccess, Boolean> shouldAllow) {
            this.shouldAllow = shouldAllow;
        }

        public boolean getShouldAllow(EntityAccess access) {
            return shouldAllow.apply(access);
        }
    }

    public boolean allowInteract;
    public boolean allowDamage;
    public boolean allowExplosion;

    public EntityAccess(boolean allowInteract, boolean allowDamage, boolean allowExplosion) {
        update(allowInteract, allowDamage, allowExplosion);
    }

    // Clone
    public EntityAccess(EntityAccess other) {
        this(other.allowInteract, other.allowDamage, other.allowExplosion);
    }

    public EntityAccess() {
        this(false, false, false);
    }

    public void update(boolean allowInteract, boolean allowDamage, boolean allowExplosion) {
        this.allowInteract = allowInteract;
        this.allowDamage = allowDamage;
        this.allowExplosion = allowExplosion;
    }

    @Override
    public void toCCConfig(@NotNull CCConfig config, @NotNull String key) {
        config.set(
                key, String.format("D:%s E:%s I:%s", allowDamage, allowExplosion, allowInteract));
    }

    @Override
    public void fromCCConfig(@NotNull CCConfig config, @NotNull String key) {
        // Get the value
        String value = config.getStr(key);
        if (value == null) return;
        value = value.trim();
        if (value.isEmpty()) return;

        // Check if we need to parse the old way
        if (value.length() == 3 && Pattern.matches("[#.]{3}", value)) {
            char[] vals = value.toCharArray();
            allowDamage = vals[0] == '#';
            allowExplosion = vals[1] == '#';
            allowInteract = vals[2] == '#';

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
                if (split[0].equalsIgnoreCase("D")) allowDamage = Boolean.parseBoolean(split[1]);
                if (split[0].equalsIgnoreCase("E")) allowExplosion = Boolean.parseBoolean(split[1]);
                if (split[0].equalsIgnoreCase("I")) allowInteract = Boolean.parseBoolean(split[1]);
            }
        }
    }

    @Override
    public String toString() {
        return "EntityAccess{"
                + "allowInteract="
                + allowInteract
                + ", allowDamage="
                + allowDamage
                + ", allowExplosion="
                + allowExplosion
                + '}';
    }
}

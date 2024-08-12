package com.cjburkey.claimchunk.flags;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Keeps track of loading the permission flags specified in the flags.yml
 * configuration file.
 *
 * @since 0.0.26
 */
public class PermFlags {

    private final JavaPlugin plugin;
    private final File defaultFlagsFile;
    private final String defaultFlagsResource;
    private final File flagsFile;
    private final HashMap<String, BlockFlagData> blockControls = new HashMap<>();
    private final HashMap<String, EntityFlagData> entityControls = new HashMap<>();

    public PermFlags(JavaPlugin plugin, File defaultFlagsFile, String defaultFlagsResource, File flagsFile) {
        this.plugin= plugin;
        this.defaultFlagsFile = defaultFlagsFile;
        this.defaultFlagsResource = defaultFlagsResource;
        this.flagsFile = flagsFile;
    }

    public void load() {
        // TODO:
    }

    // -- CLASSES -- //

    public static final class FlagData {
        public boolean isListInclude;
        public List<String> list;

        public FlagData(boolean isListInclude, List<String> list) {
            this.isListInclude = isListInclude;
            this.list = list;
        }
    }

    public enum BlockFlagType {
        BREAK,
        PLACE,
        INTERACT,
        EXPLODE,
    }

    public static class BlockFlagData {
        public BlockFlagType flagType;
        public FlagData flagData;

        public BlockFlagData(BlockFlagType flagType, boolean isListInclude, List<String> list) {
            this.flagType = flagType;
            this.flagData = new FlagData(isListInclude, list);
        }
    }

    public enum EntityFlagType {
        DAMAGE,
        INTERACT,
        EXPLODE,
    }

    public static class EntityFlagData {
        public EntityFlagType flagType;
        public FlagData flagData;

        public EntityFlagData(EntityFlagType flagType, boolean isListInclude, List<String> list) {
            this.flagType = flagType;
            this.flagData = new FlagData(isListInclude, list);
        }
    }
}

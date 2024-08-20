package com.cjburkey.claimchunk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cjburkey.claimchunk.access.CCFlags;
import com.cjburkey.claimchunk.access.CCInteractClasses;
import com.cjburkey.claimchunk.access.CCPermFlags;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Objects;

public class PermFlagTests {

    @Test
    void testLoadFlags() {
        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(
                        new StringReader(
                                """
                                permissionFlags:
                                  breakBlocks:
                                    - for: BLOCKS
                                      type: BREAK
                                  damageEntities:
                                    - for: ENTITIES
                                      type: DAMAGE
                                  redstone:
                                    - for: BLOCKS
                                      type: INTERACT
                                      include: ['@REDSTONE']
                                """));
        CCPermFlags permFlags = new CCPermFlags(new CCInteractClasses(true));
        permFlags.loadFromConfig(config);

        CCFlags.BlockFlagData breakBlocks = permFlags.blockControls.get("breakBlocks");
        assertEquals(breakBlocks.flagType(), CCFlags.BlockFlagType.BREAK);

        CCFlags.EntityFlagData damageEntities = permFlags.entityControls.get("damageEntities");
        assertEquals(damageEntities.flagType(), CCFlags.EntityFlagType.DAMAGE);

        assert Objects.requireNonNull(
                        permFlags.blockControls.get("redstone").flagData().include(),
                        "Missing include list")
                .contains("@REDSTONE");
    }

    @Test
    void testTwo() {
        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(
                        new StringReader(
                                """
                                permissionFlags:
                                  ruinStuff:
                                    - for: ENTITIES
                                      type: DAMAGE
                                    - for: BLOCKS
                                      type: BREAK
                                """));
        CCPermFlags permFlags = new CCPermFlags(new CCInteractClasses(true));
        permFlags.loadFromConfig(config);

        assert permFlags.blockControls.containsKey("ruinStuff");
        assert permFlags.entityControls.containsKey("ruinStuff");
    }
}

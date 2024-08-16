package com.cjburkey.claimchunk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cjburkey.claimchunk.access.CCInteractClasses;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class InteractClassTests {

    @Test
    void testToYml() {
        CCInteractClasses classes = new CCInteractClasses(false);
        classes.addEntityClass("VEHICLES", EntityType.MINECART, EntityType.FURNACE_MINECART);

        YamlConfiguration yaml = classes.toYaml();
        List<String> entities =
                (List<String>) Objects.requireNonNull(yaml.getList("entityClasses.VEHICLES"));
        assertEquals(2, entities.size());
        assert entities.contains("MINECART") && entities.contains("FURNACE_MINECART");
    }

    @Test
    void testDefaults() {
        CCInteractClasses classes = new CCInteractClasses(true);
        YamlConfiguration yaml = classes.toYaml();
        List<String> entities =
                (List<String>)
                        Objects.requireNonNull(yaml.getList("entityClasses.CONTAINER_ENTITIES"));
        assert entities.contains("CHEST_MINECART");
    }
}

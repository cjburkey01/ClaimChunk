package com.cjburkey.claimchunk.flag;

import com.cjburkey.claimchunk.config.ClaimChunkWorldProfileHandler;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class CCInteractClasses {

    private final HashMap<String, HashSet<Material>> classBlocks = new HashMap<>();
    private final HashMap<String, HashSet<EntityType>> classEntities = new HashMap<>();

    private final HashMap<Material, HashSet<String>> blockClasses = new HashMap<>();
    private final HashMap<EntityType, HashSet<String>> entityClasses = new HashMap<>();

    public CCInteractClasses(boolean includeDefault) {
        if (includeDefault) {
            for (Map.Entry<String, HashSet<Material>> classes :
                    ClaimChunkWorldProfileHandler.getDefaultBlockAccessClasses().entrySet()) {
                addBlockClass(classes.getKey(), classes.getValue());
            }
            for (Map.Entry<String, HashSet<EntityType>> classes :
                    ClaimChunkWorldProfileHandler.getDefaultEntityAccessClasses().entrySet()) {
                addEntityClass(classes.getKey(), classes.getValue());
            }
        }
    }

    public void addBlockClass(String className, Collection<Material> materials) {
        classBlocks(className).addAll(materials);
        materials.forEach(block -> blockClasses(block).add(className));
    }

    public void addBlockClass(String className, Material... materials) {
        Collections.addAll(classBlocks(className), materials);
        Arrays.stream(materials).forEach(block -> blockClasses(block).add(className));
    }

    public void addEntityClass(String className, Collection<EntityType> entities) {
        classEntities(className).addAll(entities);
        entities.forEach(entity -> entityClasses(entity).add(className));
    }

    public void addEntityClass(String className, EntityType... entities) {
        Collections.addAll(classEntities(className), entities);
        Arrays.stream(entities).forEach(entity -> entityClasses(entity).add(className));
    }

    // RETURNED SET IS UNMODIFIABLE!
    public @NotNull Set<Material> getClassBlocks(String className) {
        return classBlocks.containsKey(className)
                ? Collections.unmodifiableSet(classBlocks(className))
                : Collections.emptySet();
    }

    // RETURNED SET IS UNMODIFIABLE!
    public @NotNull Set<EntityType> getClassEntities(String className) {
        return classEntities.containsKey(className)
                ? Collections.unmodifiableSet(classEntities(className))
                : Collections.emptySet();
    }

    // RETURNED SET IS UNMODIFIABLE!
    public @NotNull Set<String> getBlockClasses(Material block) {
        return blockClasses.containsKey(block)
                ? Collections.unmodifiableSet(blockClasses(block))
                : Collections.emptySet();
    }

    // RETURNED SET IS UNMODIFIABLE!
    public @NotNull Set<String> getEntityClasses(EntityType entity) {
        return entityClasses.containsKey(entity)
                ? Collections.unmodifiableSet(entityClasses(entity))
                : Collections.emptySet();
    }

    public YamlConfiguration toYaml() {
        YamlConfiguration config = new YamlConfiguration();
        config.options()
                .setHeader(
                        Arrays.asList(
                                "Do not modify!",
                                "This file lists classes that may be used in the flag config file"
                                        + " and world profiles",
                                "This file is reset every server launch! Changes will not be"
                                        + " reflected in-game!"));

        // Write block classes
        for (Map.Entry<String, HashSet<Material>> entry : classBlocks.entrySet()) {
            config.set(
                    "blockClasses." + entry.getKey(),
                    entry.getValue().stream().map(Material::name).collect(Collectors.toList()));
        }

        // Write entity classes
        for (Map.Entry<String, HashSet<EntityType>> entry : classEntities.entrySet()) {
            config.set(
                    "entityClasses." + entry.getKey(),
                    entry.getValue().stream().map(EntityType::name).collect(Collectors.toList()));
        }

        return config;
    }

    private @NotNull HashSet<Material> classBlocks(String className) {
        return classBlocks.computeIfAbsent(className, ignoredKey -> new HashSet<>());
    }

    private @NotNull HashSet<EntityType> classEntities(String className) {
        return classEntities.computeIfAbsent(className, ignoredKey -> new HashSet<>());
    }

    private @NotNull HashSet<String> blockClasses(Material block) {
        return blockClasses.computeIfAbsent(block, ignoredKey -> new HashSet<>());
    }

    private @NotNull HashSet<String> entityClasses(EntityType entity) {
        return entityClasses.computeIfAbsent(entity, ignoredKey -> new HashSet<>());
    }
}

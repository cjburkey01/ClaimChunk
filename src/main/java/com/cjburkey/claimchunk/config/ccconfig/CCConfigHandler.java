package com.cjburkey.claimchunk.config.ccconfig;

import com.cjburkey.claimchunk.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CCConfigHandler<ConfigType> {

    private final File configFile;
    private final ConfigType config;

    public CCConfigHandler(File configFile, ConfigType config) {
        this.configFile = configFile;
        this.config = config;
    }

    public ConfigType config() {
        return config;
    }

    public File file() {
        return configFile;
    }

    public boolean save(Function<ConfigType, String> serializeConfig) {
        // Try to create the parent file if it doesn't exist
        if (configFile.getParentFile() != null
                && !configFile.getParentFile().exists()
                && !configFile.getParentFile().mkdirs()) {
            Utils.err("Failed to create parent directory \"%s\" for file \"%s\"",
                    configFile.getParent(),
                    configFile.getAbsolutePath());
        }

        // Check if the config can exist
        if (!configFile.exists()
                && (configFile.getParentFile() == null
                || !configFile.getParentFile().exists())) {
            Utils.err("Unable to save config to file \"%s\"", configFile.getAbsolutePath());
            return false;
        }

        // Try to write the config to the file
        try (FileWriter writer = new FileWriter(configFile, false)) {
            writer.write(serializeConfig.apply(config));
            return true;
        } catch (IOException e) {
            Utils.err("Error saving config to file \"%s\":", configFile.getAbsolutePath());
            e.printStackTrace();
        }

        // The file must not have been saved successfully
        return false;
    }

    public boolean load(BiConsumer<String, ConfigType> deserializeConfig) {
        // Check if the config can exist
        if (!configFile.exists()) {
            Utils.err("Unable to load config from file \"%s\" because it didn't exist", configFile.getAbsolutePath());
            return false;
        }

        // Read the file
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            // Read the string, deserialize it into a CCConfig, then merge this
            // config with that config
            deserializeConfig.accept(reader.lines().collect(Collectors.joining("\n")), config);
            return true;
        } catch (IOException e) {
            Utils.err("Error loading config from file \"%s\":", configFile.getAbsolutePath());
            e.printStackTrace();
        }

        // The file must not have been loaded successfully
        return false;
    }

}

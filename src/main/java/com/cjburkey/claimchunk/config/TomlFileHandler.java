package com.cjburkey.claimchunk.config;

import com.cjburkey.claimchunk.Utils;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class TomlFileHandler<Data> {

    private final File file;
    private final Class<Data> dataClass;
    private Data readData;
    private final Supplier<Data> defaultData;
    private final TomlWriter tomlWriter = new TomlWriter.Builder().indentTablesBy(4)
                                                                  .indentValuesBy(4)
                                                                  .padArrayDelimitersBy(1)
                                                                  .showFractionalSeconds()
                                                                  .build();

    public TomlFileHandler(@Nonnull File file, @Nonnull Class<Data> dataClass, @Nonnull Supplier<Data> defaultData) {
        this.file = file;
        this.dataClass = dataClass;
        this.defaultData = defaultData;
    }

    public boolean save(@Nullable Data data) {
        // Try to create all the directories necessary for this file
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            Utils.err("Failed to create parent directory: \"%s\"", file.getParentFile().getAbsolutePath());
            return false;
        }

        // Try to write the provided data to the TOML file or the default if
        // none was provided
        try {
            tomlWriter.write((data != null) ? data : defaultData.get(), file);
            return true;
        } catch (IOException e) {
            Utils.err("Failed to write to TOML file: \"%s\"", file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Data> load() {
        try {
            // Check if the file doesn't exist and try to save the default
            if (!file.exists() && !save(null)) {
                Utils.err("Failed to write new default TOML to file: \"%s\"", file.getAbsolutePath());
                return Optional.empty();
            }

            // Keep track of the last read value
            readData = new Toml().read(file).to(dataClass);

            // Return a filled optional wrapper
            return Optional.of(readData);
        } catch (Exception e) {
            Utils.err("Failed to read TOML file: \"%s\"", file.getAbsolutePath());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Data readData() {
        return readData;
    }

}

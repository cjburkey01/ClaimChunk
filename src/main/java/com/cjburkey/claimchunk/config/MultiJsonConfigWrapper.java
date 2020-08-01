package com.cjburkey.claimchunk.config;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

// Is this more inefficient than just making another JSON handler that isn't
// based around an array? Yes. Does it matter? Probably! Will that stop me from
// just making this wrapper class? Nope! :)
public class MultiJsonConfigWrapper<T> {

    private final File parentConfigDir;
    private final Class<T[]>  referenceClass;
    private final HashMap<String, JsonConfig<T>> configs = new HashMap<>();

    public MultiJsonConfigWrapper(Class<T[]> referenceClass, File parentConfigDir) {
        this.parentConfigDir = parentConfigDir;
        this.referenceClass = referenceClass;
    }

    private JsonConfig<T> getConfig(String keyString) {
        // Create a new JSON config handler if one doesn't exist for this type
        // yet
        return configs.computeIfAbsent(keyString, key -> {
            // The specific config file for this type
            final File configFile = new File(parentConfigDir, key + ".json");

            // Create and insert the new config
            return new JsonConfig<>(referenceClass, configFile, true);
        });
    }

    @SuppressWarnings("unused")
    public void reload(String keyString) throws IOException {
        // Get a reference to the config
        JsonConfig<T> config = getConfig(keyString);

        // Reload the data for this config
        config.reloadData();
    }

    public void lazyReloadAll() {
        // World profiles are loaded when they are needed, so clearing allows
        // more efficient reloads.
        configs.clear();
    }

    public void save(String keyString, Function<String, T> getDefault) throws IOException {
        // Get a reference to the config
        JsonConfig<T> config = getConfig(keyString);

        // Get a copy of the data present to check if there is already an entry
        Collection<T> data = config.getData();
        if (data.size() < 1) {
            // If the config is empty, initialize the default value
            T def = getDefault.apply(keyString);

            // Just stop here if the default is null because saving the empty
            // config is pointless.
            if (def == null) {
                return;
            }
            // Otherwise, add the default data
            config.addData(def);
        }

        // Save the file
        // TODO: ADD INFORMATIVE COMMENT
        config.saveData(null);
    }

    public @Nullable T get(String keyString, Function<String, T> getDefault, boolean reload) throws IOException {
        // Get the JSON config handler
        JsonConfig<T> config = getConfig(keyString);

        // Reloading can be costly so there is a parameter to disable automatic
        // config loading every access
        if (reload) {
            // Load the data in the config
            config.reloadData();
        }

        // Get the data present
        Collection<T> data = config.getData();
        if (data.size() < 1) {
            // Save the config using this method which adds the default into
            // the config if the data is not already inside of the handler.
            save(keyString, getDefault);
        } else if (data.size() > 1) {
            // Inefficient string handling but this should happen rarely/never
            // so it won't matter at the times this would occur.
            throw new IOException("Error loading config file: " + config.file.getAbsolutePath()
                                  + ". The config file contains more data than should have been saved in it. "
                                  + "Try deleting the file to allow it to regenerate or fix the error manually "
                                  + "by ensuring there is only one main config entry.");
        }

        // Get an iterator for the data to get the first (and only) entry.
        // This calls `config.getData()` again to account for the fact that
        // the method returns an immutable copy of the data, and, as such, the
        // old collection reference will not contain the possibly-newly-added
        // default value.
        Iterator<T> dataIter = config.getData().iterator();

        // Double check that the config data exists
        if (dataIter.hasNext()) {
            return dataIter.next();
        }

        // Oops! Something went wrong and the config wasn't created.
        return null;
    }

}

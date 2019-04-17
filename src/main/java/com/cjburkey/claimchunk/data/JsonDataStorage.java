package com.cjburkey.claimchunk.data;

import com.cjburkey.claimchunk.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class JsonDataStorage<T> implements IDataStorage<T> {

    private static Gson gson;

    private final HashSet<T> data = new HashSet<>();
    private final Class<T[]> referenceClass;
    private File file;

    public JsonDataStorage(Class<T[]> referenceClass, File file) {
        this.file = file;
        this.referenceClass = referenceClass;
    }

    @Override
    public Collection<T> getData() {
        return Collections.unmodifiableCollection(data);
    }

    @Override
    public void addData(T toAdd) {
        data.add(toAdd);
    }

    @Override
    public void saveData() throws IOException {
        if (file == null) {
            return;
        }
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            Utils.err("Failed to create directory");
            return;
        }
        if (file.exists() && !file.delete()) {
            Utils.err("Failed to clear old offline JSON data");
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(getGson().toJson(data));
        }
    }

    @Override
    public void reloadData() throws IOException {
        if (file == null || !file.exists()) {
            return;
        }
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
                json.append('\n');
            }
        }
        T[] out = getGson().fromJson(json.toString(), referenceClass);
        if (out != null) {
            data.clear();
            Collections.addAll(data, out);
        }
    }

    @Override
    public void clearData() {
        data.clear();
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .serializeNulls()
                    .create();
        }
        return gson;
    }

}

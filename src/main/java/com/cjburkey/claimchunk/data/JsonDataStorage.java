package com.cjburkey.claimchunk.data;

import com.cjburkey.claimchunk.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonDataStorage<T> implements IDataStorage<T> {

    private final List<T> data = new ArrayList<>();
    private final Class<T[]> referenceClass;
    private File file;

    public JsonDataStorage(Class<T[]> referenceClass, File file) {
        this.file = file;
        this.referenceClass = referenceClass;
    }

    public void saveData() throws IOException {
        if (file == null) {
            return;
        }
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            Utils.err("Failed to create directory");
        }
        if (file.exists() && !file.delete()) {
            Utils.err("Failed to clear data");
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(getGson().toJson(data));
        }
    }

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
            data.addAll(Arrays.asList(out));
        }
    }

    public void addData(T toAdd) {
        data.add(toAdd);
    }

    public void clearData() {
        data.clear();
    }

    public List<T> getData() {
        return new ArrayList<>(data);
    }

    private Gson getGson() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    }

}

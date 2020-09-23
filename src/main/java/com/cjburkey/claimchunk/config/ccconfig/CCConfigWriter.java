package com.cjburkey.claimchunk.config.ccconfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;

public final class CCConfigWriter {

    // TODO: FINISH SERIALIZATION
    public static void serialize(CCConfig config, OutputStream outputStream) throws IOException {
        ArrayList<Map.Entry<String, String>> properties = new ArrayList<>(config.values());
        properties.sort((o0, o1) -> {
            if (o0 == o1) return 0;
            if (o0 == null) return -1;
            if (o1 == null) return 1;
            return o0.getKey().compareTo(o1.getKey());
        });

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String lastCategory = "";
            String currentCategory = "";
        }
    }

}

package com.cjburkey.claimchunk.config.ccconfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class CCConfigWriter {

    public void serialize(CCConfig config, OutputStream outputStream) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(serialize(config));
        }
    }
    
    public String serialize(CCConfig config) {
        StringBuilder output = new StringBuilder();
        
        // Sort all of the properties
        ArrayList<Map.Entry<String, String>> properties = new ArrayList<>(config.values());
        properties.sort((o0, o1) -> {
            if (o0 == o1) return 0;
            if (o0 == null) return -1;
            if (o1 == null) return 1;
            return o0.getKey().compareTo(o1.getKey());
        });
        
        Stream<Map.Entry<String, String>> propStream = properties.stream();
        
        do {
            Map.Entry<String, String> previousProp = propStream.iterator().hasNext() ? propStream.iterator().next() : null;
            Map.Entry<String, String> prop = propStream.iterator().hasNext() ? propStream.iterator().next() : null;
            
            // TODO
            
            break;
        } while (true);
        
        return output.toString();
    }

}

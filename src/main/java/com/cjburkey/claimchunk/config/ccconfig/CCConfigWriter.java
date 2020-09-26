package com.cjburkey.claimchunk.config.ccconfig;

import com.cjburkey.claimchunk.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

        // Iterate through the properties
        Iterator<Map.Entry<String, String>> props = properties.iterator();
        Map.Entry<String, String> previousProp = null;
        while (props.hasNext()) {
            // Keep track of the previous property
            Map.Entry<String, String> prop = props.next();
            try {
                // Skip null properties
                if (prop == null) continue;

                // Get the key of the property
                final NSKey propKey = new NSKey(prop.getKey());
                if (propKey.key == null) {
                    Utils.err("Invalid key: \"%s\"", prop.getKey());
                    continue;
                }

                // Get the key for the previous property if it exists
                final NSKey prevPropKey = (previousProp == null)
                                                  ? null
                                                  : new NSKey(previousProp.getKey());

                // Determine if a label needs to be added for this
                String label = null;
                if (previousProp != null && !propKey.key.equals(prevPropKey.key)) {
                    // Keep track of how many parents are the same
                    int same = 0;

                    // Count the same categories
                    List<String> propCat = propKey.category();
                    List<String> prevPropCat = propKey.category();
                    int max = Integer.max(propCat.size(), prevPropCat.size());
                    for (int i = 0; i < max; i ++) {
                        if (propCat.get(i).equals(prevPropCat.get(i))) {
                            same ++;
                        } else break;
                    }

                    label = String.join(".", propCat.subList(same, propCat.size()));
                    if (same == 1) label = "<." + label;
                    if (same > 1) label = "<<." + label;
                }
                if (label != null) {
                    // Add the label to the output
                    output.append('\n');
                    output.append(label);
                    output.append(':');
                    output.append('\n');
                }

                // Write key-value pair
                output.append(propKey.key);
                output.append(' ');
                output.append(prop.getValue());
                output.append(' ');
                output.append(';');
                output.append('\n');
            } finally {
                previousProp = prop;
            }
        }
        
        return output.toString();
    }

}

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
import java.util.stream.Collectors;

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
        // And keep track of the previous property
        Map.Entry<String, String> previousProp = null;
        // Keep track of how indented we should be
        int indent = 0;
        while (props.hasNext()) {
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
                StringBuilder label = new StringBuilder();
                if (previousProp == null || !propKey.category().equals(prevPropKey.category())) {
                    List<String> relative = propKey.getRelativeCat(prevPropKey == null
                                                                           ? new ArrayList<>()
                                                                           : prevPropKey.category());

                    label.append(String.join(".", relative));
                }
                if (label.length() > 0) {
                    // Write the label to the output
                    indent(output, 0, "  ");
                    output.append('\n');
                    output.append(label);
                    output.append(':');
                    output.append('\n');
                }

                // Write key-value pair
                indent(output, 1, "  ");
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

    private static void indent(StringBuilder output, int indents, String singleIndent) {
        for (int i = 0; i < indents; i ++) {
            output.append(singleIndent);
        }
    }

}

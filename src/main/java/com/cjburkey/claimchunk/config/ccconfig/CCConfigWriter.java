package com.cjburkey.claimchunk.config.ccconfig;

import com.cjburkey.claimchunk.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public final class CCConfigWriter {

    public String singleIndent;
    public int keyValueSeparation;
    public int valueSemicolonSeparation;
    public int labelColonSeparation;
    public int propertyIndent;
    public int labelIndent;

    public CCConfigWriter() {
        this("  ", 2, 1, 0, 1, 0);
    }

    public CCConfigWriter(String singleIndent, int keyValueSeparation, int valueSemicolonSeparation,
                          int labelColonSeparation, int propertyIndent, int labelIndent) {
        this.singleIndent = singleIndent;
        this.keyValueSeparation = keyValueSeparation;
        this.valueSemicolonSeparation = valueSemicolonSeparation;
        this.labelColonSeparation = labelColonSeparation;
        this.propertyIndent = propertyIndent;
        this.labelIndent = labelIndent;
    }

    @SuppressWarnings("unused")
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

        // Output the header comment if it's valid
        String headerComment = config.headerComment() == null
                                       ? null
                                       : config.headerComment().trim();
        if (headerComment != null && !headerComment.isEmpty()) {
            for (String headerCommentLine : headerComment.split("\n")) {
                output.append('#');
                output.append(' ');
                output.append(headerCommentLine);
                output.append('\n');
            }
        }

        // Iterate through the properties
        Iterator<Map.Entry<String, String>> props = properties.iterator();
        // And keep track of the previous property
        Map.Entry<String, String> previousProp = null;
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
                if (previousProp == null || !propKey.category().equals(prevPropKey.category())) {
                    // Write the label to the output
                    output.append('\n');
                    indent(output, labelIndent, singleIndent);
                    output.append(propKey.categories());
                    for (int i = 0; i < labelColonSeparation; i ++) output.append(' ');
                    output.append(':');
                    output.append('\n');
                }

                // Write key-value pair
                indent(output, propertyIndent, singleIndent);
                output.append(propKey.key);
                for (int i = 0; i < keyValueSeparation; i ++) output.append(' ');
                output.append(prop.getValue());
                for (int i = 0; i < valueSemicolonSeparation; i ++) output.append(' ');
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

package com.cjburkey.claimchunk;

import static org.junit.jupiter.api.Assertions.*;

import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigParseError;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigParser;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigWriter;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class CCConfigTests {

    @Test
    void testConfigValueStorage() {
        // Initialize a config
        final CCConfig config = new CCConfig("", "");

        // Set some test values
        config.set("an_int", "10");
        config.set("a_float", "20.0");
        config.set("a_bool", "true");
        config.set("a_string", "this is my value :)");

        // Assert the test values
        assertEquals(10, config.getInt("an_int", 0));
        assertEquals(20.0f, config.getFloat("a_float", 0.0f), Float.MIN_VALUE);
        assert config.getBool("a_bool", false);
        assertEquals("this is my value :)", config.getStr("a_string"));

        // Assert defaults when the getting nonexistent values
        assertEquals(1, config.getInt("a_fake_int", 1));
        assertEquals(2.0f, config.getFloat("a_fake_float", 2.0f), Float.MIN_VALUE);
        assert !config.getBool("a_fake_bool", false);
        assertEquals("", config.getStr("a_fake_string"));
    }

    @Test
    void testConfigToString() {
        // Initialize a config
        final CCConfig config = new CCConfig("Example comment :)\nA different comment", "");

        // Set some test values
        config.set("bob.an_int", "10");
        config.set("bob.a_float", "20.0");
        config.set("bob.says.a_bool", "true");
        config.set("jim.yells.says.a_bool", "true");
        config.set("jim.says.yells.a_bool", "false");
        config.set("bob.a_string", "this is my value :)");
        config.set("bob.a_different_float", "20.0");

        // Create the config writer
        final CCConfigWriter configWriter = new CCConfigWriter("  ", 4, 1, 0, 1, 0);
        final String serializedConfig = configWriter.serialize(config);

        // The expected output
        final String expected =
                "# Example comment :)\n"
                        + "# A different comment\n\n"
                        + "bob:\n"
                        + "  a_different_float    20.0 ;\n"
                        + "  a_float    20.0 ;\n"
                        + "  a_string    this is my value :) ;\n"
                        + "  an_int    10 ;\n\n"
                        + "bob.says:\n"
                        + "  a_bool    true ;\n\n"
                        + "jim.says.yells:\n"
                        + "  a_bool    false ;\n\n"
                        + "jim.yells.says:\n"
                        + "  a_bool    true ;\n";

        assertEquals(serializedConfig, expected);
    }

    @Test
    void testConfigFromString() {
        final String input =
                "\nbob:\n"
                        + "  a_different_float    30.0 ;\n"
                        + "  a_float    20.0 ;\n"
                        + "  a_string    this is my value :) ;\n"
                        + "  an_int    10 ;\n\n"
                        + "bob.says:\n"
                        + "  a_bool    true ;\n\n"
                        + "jim.says.yells:\n"
                        + "  a_bool    false ;\n\n"
                        + "jim.yells.says:\n"
                        + "  a_bool    true ;\n";

        // Initialize a config
        final CCConfig config = new CCConfig("", "");
        final CCConfigParser configParser = new CCConfigParser();

        // Parse the config and make sure there aren't any errors
        final List<CCConfigParseError> parseErrors = configParser.parse(config, input);
        System.out.println(
                parseErrors.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(", ", "[ ", "] ")));
        assertEquals(new ArrayList<>(), parseErrors);

        // Ensure all the values were set correctly
        assertEquals(10, config.getInt("bob.an_int", 0));
        assertEquals(20.0f, config.getFloat("bob.a_float", 0.0f));
        assertTrue(config.getBool("bob.says.a_bool", false));
        assertTrue(config.getBool("jim.yells.says.a_bool", false));
        assertFalse(config.getBool("jim.says.yells.a_bool", true));
        assertEquals("this is my value :)", config.getStr("bob.a_string"));
        assertEquals(30.0f, config.getFloat("bob.a_different_float", 0.0f));
    }
}

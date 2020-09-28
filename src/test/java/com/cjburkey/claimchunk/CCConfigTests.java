package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.config.ccconfig.CCConfig;
import com.cjburkey.claimchunk.config.ccconfig.CCConfigWriter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CCConfigTests {
    
    @Test
    void testConfigValueStorage() {
        // Initialize a config
        CCConfig config = new CCConfig("");

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
        CCConfig config = new CCConfig("");

        // Set some test values
        config.set("bob.an_int", "10");
        config.set("bob.a_float", "20.0");
        config.set("bob.says.a_bool", "true");
        config.set("jim.yells.says.a_bool", "true");
        config.set("jim.says.yells.a_bool", "false");
        config.set("bob.a_string", "this is my value :)");
        config.set("bob.a_different_float", "20.0");

        System.out.println();
        System.out.println(new CCConfigWriter().serialize(config));
    }
    
}

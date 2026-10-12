package org.example.telegrambot.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final Properties properties = new Properties();

    static {
        // Load application.properties
        try (InputStream is = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        // Load bot-token.properties (overrides application.properties)
        try (InputStream is = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream("bot-token.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            // Token file is optional, log warning but don't fail
            System.err.println("Warning: bot-token.properties not found, using token from application.properties");
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Property '" + key + "' not found in application.properties");
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}

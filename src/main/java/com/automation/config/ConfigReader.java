package com.automation.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    // Static block to load the file once when the class is initialized
    static {
        try {
            String path = "src/test/resources/config.properties";
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            System.out.println("Local config.properties not found. Relying entirely on environment variables.");
        }
    }

    public static String get(String key) {
        // 1. Check if running in CI (GitHub Actions sets CI=true automatically, but we also set it in our yml)
        if (System.getenv("CI") != null && System.getenv("CI").equalsIgnoreCase("true")) {
            return System.getenv(key);
        }

        // 2. If running on Jenkins or local terminal, env vars might be passed directly
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // 3. Fallback to local config.properties for local IDE runs
        if (properties != null && properties.getProperty(key) != null) {
            return properties.getProperty(key);
        }

        throw new RuntimeException("Property / Environment Variable '" + key + "' was not found!");
    }
}
package io.klerch.alexa.translator.client;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Encapsulates access to application-wide property values
 */
public class AppConfig {
    private static Properties properties = new Properties();
    private static final String defaultPropertiesFile = "app.properties";
    private static final String customPropertiesFile = "my.app.properties";

    /**
     * Static block does the bootstrapping of all configuration properties with
     * reading out values from different resource files
     */
    static {
        final String propertiesFile =
                AppConfig.class.getClassLoader().getResource(customPropertiesFile) != null ?
                        customPropertiesFile : defaultPropertiesFile;
        final InputStream propertiesStream = AppConfig.class.getClassLoader().getResourceAsStream(propertiesFile);
        try {
            properties.load(propertiesStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (propertiesStream != null) {
                try {
                    propertiesStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getAlexaAppId() {
        return properties.getProperty("AlexaAppId");
    }

    public static String getAlexaUserId() {
        return properties.getProperty("AlexaUserId");
    }

    public static String getAlexaSessionId() {
        return properties.getProperty("AlexaSessionId");
    }

    public static String getAlexaRequestId() {
        return properties.getProperty("AlexaRequestId");
    }

    public static String getLocale() {
        return properties.getProperty("Locale");
    }

    public static String getLambdaName() {
        return properties.getProperty("LambdaName");
    }

    public static String getTestPhrase() {
        return properties.getProperty("TestPhrase");
    }
}

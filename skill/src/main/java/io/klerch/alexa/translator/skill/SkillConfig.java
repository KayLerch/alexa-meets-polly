package io.klerch.alexa.translator.skill;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Encapsulates access to application-wide property values
 */
public class SkillConfig {
    private static Properties properties = new Properties();
    private static final String defaultPropertiesFile = "app.properties";
    private static final String customPropertiesFile = "my.app.properties";

    /**
     * Static block does the bootstrapping of all configuration properties with
     * reading out values from different resource files
     */
    static {
        final String propertiesFile =
                SkillConfig.class.getClassLoader().getResource(customPropertiesFile) != null ?
                        customPropertiesFile : defaultPropertiesFile;
        final InputStream propertiesStream = SkillConfig.class.getClassLoader().getResourceAsStream(propertiesFile);
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

    public static String getS3BucketUrl() {
        return properties.getProperty("S3BucketUrl");
    }

    public static String getS3BucketName() {
        return properties.getProperty("S3BucketName");
    }

    public static String getGoogleProjectName() {
        return properties.getProperty("GoogleProjectName");
    }

    public static String getGoogleApiKey() {
        return properties.getProperty("GoogleApiKey");
    }

    public static String getMicrosoftSubscriptionKey() {
        return properties.getProperty("MicrosoftSubscriptionKey");
    }

    public static String getTranslatorConvertServiceUrl() {
        return properties.getProperty("TranslatorConvertServiceUrl");
    }

    public static String getTranslatorConvertServiceUser() {
        return properties.getProperty("TranslatorConvertServiceUser");
    }

    public static String getTranslatorConvertServicePass() {
        return properties.getProperty("TranslatorConvertServicePass");
    }

    public static String getS3CardFolderUrl() {
        return getS3BucketUrl() + properties.getProperty("S3CardFolder");
    }

    public static Boolean shouldSkipMp3Conversion() {
        return StringUtils.equalsIgnoreCase("true", properties.getProperty("SkipMp3Conversion"));
    }

    public static String getTranslatorService() {
        return properties.getProperty("TranslatorService");
    }
}

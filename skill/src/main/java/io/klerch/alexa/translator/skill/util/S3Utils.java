package io.klerch.alexa.translator.skill.util;

import io.klerch.alexa.translator.skill.SkillConfig;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class S3Utils {
    private static final Logger log = Logger.getLogger(S3Utils.class);

    private String mp3Url;
    private String filePath;

    public S3Utils(final TTSPolly ttsPolly, final String text) {
        try {
            filePath = ttsPolly.getLocale() + "-" + URLEncoder.encode(text.replace(" ", "_").replace("ü", "ue").replace("ö", "oe").replace("ä", "ae").replace("ß", "ss"), "UTF-8") + "-" + ttsPolly.getVoice() + ".mp3";
            mp3Url = SkillConfig.getS3BucketUrl() + filePath;
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to generate file name for mp3. " + e.getMessage());
        }
    }

    public String getMp3Url() {
        return mp3Url;
    }

    public String getFilePath() {
        return filePath;
    }
}

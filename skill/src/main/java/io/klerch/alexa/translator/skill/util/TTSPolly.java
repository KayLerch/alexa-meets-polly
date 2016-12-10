package io.klerch.alexa.translator.skill.util;

import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public class TTSPolly {
    private static final Logger log = Logger.getLogger(TTSPolly.class);

    private final String locale;
    private final String language;
    private final YamlReader yamlReader;
    private final AmazonPolly awsPolly;
    private final AmazonS3Client awsS3;
    private final String voiceId;

    public TTSPolly(final String locale, final String language) {
        this.locale = locale;
        this.language = language;

        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/voices.yml");
        this.yamlReader = new YamlReader(reader, locale);
        this.awsPolly = new AmazonPollyClient();
        this.awsS3 = new AmazonS3Client();

        voiceId = language != null ? yamlReader.getRandomUtterance(language.toLowerCase().replace(" ", "_")).orElse("") : "";
        Validate.notBlank(voiceId, "No voiceId is associated with given language.");
    }

    private Optional<TextToSpeech> getMp3UrlOfPreviousTTS(final String text) {
        return awsS3.doesObjectExist(SkillConfig.getS3BucketName(), getMp3Path(text)) ?
                Optional.of(getTTS(text)) : Optional.empty();
    }

    public Optional<TextToSpeech> textToSpeech(final String text) {
        Optional<TextToSpeech> tts = getMp3UrlOfPreviousTTS(text);
        // if there was a previous tts for this text return immediately
        if (tts.isPresent()) return tts;

        // translate term
        final Optional<String> translated = new GoogleTranslation(locale).translate(text, language);

        if (translated.isPresent()) {
            final String ssml = String.format("<speak><prosody rate='x-slow' volume='x-loud'>%1$s</prosody></speak>", translated.get());
            final SynthesizeSpeechRequest synthRequest = new SynthesizeSpeechRequest()
                    .withText(ssml)
                    .withOutputFormat(OutputFormat.Mp3)
                    .withVoiceId(voiceId)
                    .withTextType(TextType.Ssml)
                    .withSampleRate("16000");
            final SynthesizeSpeechResult synthResult = awsPolly.synthesizeSpeech(synthRequest);

            try {
                final PutObjectRequest s3Put = new PutObjectRequest(SkillConfig.getS3BucketName(), getMp3Path(text), synthResult.getAudioStream(), new ObjectMetadata())
                        .withCannedAcl(CannedAccessControlList.PublicRead);
                awsS3.putObject(s3Put);

                // mp3 needs conversion to comply with MP3 format supported by Alexa service
                final String mp3ConvertedUrl = Mp3Converter.convertMp3(getMp3Url(text));

                if (mp3ConvertedUrl != null && !mp3ConvertedUrl.isEmpty()) {
                    return Optional.of(getTTS(text, translated.get()));
                }
            } catch (final IOException | URISyntaxException e) {
                log.error("Error while generating mp3. " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    private String getMp3Path(final String text) {
        return String.format("%1$s/%2$s/%3$s.mp3", locale, voiceId, text.replace(" ", "_"));
    }

    private String getMp3Url(final String text) {
        return SkillConfig.getS3BucketUrl() + getMp3Path(text);
    }

    private TextToSpeech getTTS(final String text) {
        return getTTS(text, null);
    }

    private TextToSpeech getTTS(final String text, final String translatedText) {
        return TextToSpeech.create()
                .withLanguage(language)
                .withText(text)
                .withMp3(getMp3Url(text))
                .withVoice(voiceId)
                .withTranslatedText(translatedText).build();
    }
}

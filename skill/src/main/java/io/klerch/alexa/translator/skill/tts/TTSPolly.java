package io.klerch.alexa.translator.skill.tts;

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
import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.handler.AlexaSessionStateHandler;
import io.klerch.alexa.state.handler.AlexaStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import io.klerch.alexa.translator.skill.translate.TranslatorFactory;
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
    private final AlexaStateHandler dynamoStateHandler;
    private final AlexaStateHandler sessionStateHandler;

    public TTSPolly(final AlexaInput input) {
        this.locale = input.getLocale();
        this.language = input.getSlotValue("language");

        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/voices.yml");
        this.yamlReader = new YamlReader(reader, locale);
        this.awsPolly = new AmazonPollyClient();
        this.awsS3 = new AmazonS3Client();
        this.sessionStateHandler = input.getSessionStateHandler();
        this.dynamoStateHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());

        voiceId = language != null ? yamlReader.getRandomUtterance(language.toLowerCase().replace(" ", "_")).orElse("") : "";
        Validate.notBlank(voiceId, "No voiceId is associated with given language.");
    }

    private String getDictionaryId(final String text) {
        return String.format("%1$s-%2$s_%3$s", locale, voiceId, text.replace(" ", "_"));
    }

    private String getMp3Path(final String text) {
        return String.format("%1$s/%2$s/%3$s.mp3", locale, voiceId, text.replace(" ", "_"));
    }

    private String getMp3Url(final String text) {
        return SkillConfig.getS3BucketUrl() + getMp3Path(text);
    }

    public Optional<TextToSpeech> textToSpeech(final String text) throws AlexaStateException {
        Optional<TextToSpeech> tts = dynamoStateHandler.readModel(TextToSpeech.class, getDictionaryId(text));
        // if there was a previous tts for this text return immediately
        if (tts.isPresent()) {
            // set handler to session to avoid writing back to dynamo
            tts.get().setHandler(sessionStateHandler);
            return tts;
        }

        // translate term
        final Optional<String> translated = TranslatorFactory.getTranslator(locale).translate(text, language);

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

                if (!SkillConfig.shouldSkipMp3Conversion()) {
                    // mp3 needs conversion to comply with MP3 format supported by Alexa service
                    final String mp3ConvertedUrl = Mp3Converter.convertMp3(getMp3Url(text));
                    Validate.notBlank(mp3ConvertedUrl, "Conversion service did not return proper return value");
                }
                return Optional.of(getTTS(text, translated.get()));
            } catch (final IOException | URISyntaxException e) {
                log.error("Error while generating mp3. " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    private TextToSpeech getTTS(final String text, final String translatedText) {
        final TextToSpeech tts = TextToSpeech.create()
                .withLanguage(language)
                .withText(text)
                .withMp3(getMp3Url(text))
                .withVoice(voiceId)
                .withTranslatedText(translatedText).build();
        // this object needs to be saved in dynamo dictionary
        tts.setHandler(dynamoStateHandler);
        // using an unique identifier
        tts.setId(getDictionaryId(text));
        return tts;
    }

    public String getLanguage() {
        return this.language;
    }
}

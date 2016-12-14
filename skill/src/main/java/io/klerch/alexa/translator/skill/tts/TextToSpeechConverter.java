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
import io.klerch.alexa.state.handler.AlexaStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import io.klerch.alexa.translator.skill.translate.TranslatorFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public class TextToSpeechConverter {
    private static final Logger log = Logger.getLogger(TextToSpeechConverter.class);

    private final String locale;
    private final String language;
    private final YamlReader yamlReader;
    private final AmazonPolly awsPolly;
    private final AmazonS3Client awsS3;
    private final String voiceId;
    private final AlexaStateHandler dynamoStateHandler;
    private final AlexaStateHandler sessionStateHandler;

    public TextToSpeechConverter(final AlexaInput input) {
        // the locale is coming with the speechlet request and indicates to source language to translate from
        this.locale = input.getLocale();
        // the language is taken from the user input (slot value) and indicates to language to translate to
        this.language = input.getSlotValue("language");
        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/voices.yml");
        // the yaml reader reads values from YAML file to get a Polly voiceId for a language
        this.yamlReader = new YamlReader(reader, locale);
        // Polly client to request speech of a translated text
        this.awsPolly = new AmazonPollyClient();
        // S3 client to store MP3 with speech of a translated text
        this.awsS3 = new AmazonS3Client();
        // session state handler to read/write skill state information to Alexa session
        this.sessionStateHandler = input.getSessionStateHandler();
        // dynamo state handler to read/write skill state information to DynamoDB
        this.dynamoStateHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
        // retrieve voiceId from YAML file that maps to the language given by the user
        voiceId = language != null ? yamlReader.getRandomUtterance(language.toLowerCase().replace(" ", "_")).orElse("") : "";
        // without a voiceId there's not chance to fulfill the translation request
        Validate.notBlank(voiceId, "No voiceId is associated with given language.");
    }

    /**
     * Generates the id unique per text, locale and voice. This id is used to store
     * translation information in DynamoDB
     * @param text The text (not the translation)
     * @return id of translation in dictionary
     */
    private String getDictionaryId(final String text) {
        final String escapedText = text.replace(" ", "_")
                .replaceAll("(?i)ö", "oe")
                .replaceAll("(?i)ä", "ae")
                .replaceAll("(?i)ü", "ue")
                .replaceAll("(?i)ß", "ss")
                .replaceAll("[^a-zA-Z0-9_\\-]", "");
        return String.format("%1$s-%2$s_%3$s", locale, voiceId, escapedText);
    }

    /**
     * Generates the path to an mp3 having the speech of the translation
     * @param text The text (not the translation)
     * @return path to an mp3 having the speech of the translation
     */
    private String getMp3Path(final String text) {
        return String.format("%1$s/%2$s/%3$s.mp3", locale, voiceId, text.replace(" ", "_"));
    }

    /**
     * Generates the url to an mp3 having the speech of the translation
     * @param text The text (not the translation)
     * @return path to an mp3 having the speech of the translation
     */
    private String getMp3Url(final String text) {
        return SkillConfig.getS3BucketUrl() + getMp3Path(text);
    }

    /**
     * Returns text-to-speech of a translation of a given text. Before translating the text,
     * requesting speech from AWS Polly and storing the resulting MP3 to S3 this method looks
     * up previous translation of the same text. Once found it will avoid doing the aforementioned
     * roundtrip but rather will use the data of the previous translation.
     * @param text text to translate and convert to speech
     * @return text to speech information
     * @throws AlexaStateException error reading or writing state to Dynamo dictionary
     */
    public Optional<TextToSpeech> textToSpeech(final String text) throws AlexaStateException {
        // look up previous translation in dictionary
        Optional<TextToSpeech> tts = dynamoStateHandler.readModel(TextToSpeech.class, getDictionaryId(text));
        // if there was a previous tts for this text return immediately (exception for the roundtrip-phrase used by the test-client)
        if (tts.isPresent() && !StringUtils.equalsIgnoreCase(text, SkillConfig.getAlwaysRoundTripPhrase())) {
            // set handler to session to avoid writing back to dynamo (nothing changed)
            tts.get().setHandler(sessionStateHandler);
            return tts;
        }

        // translate term by leveraging a Translator implementation provided by the factory
        final Optional<String> translated = TranslatorFactory.getTranslator(locale).translate(text, language);

        if (translated.isPresent()) {
            // form the SSML by embedding the translated text
            final String ssml = String.format("<speak><prosody rate='-40%%' volume='x-loud'>%1$s</prosody></speak>", translated.get());
            // build a Polly request to get speech with desired voice and SSML
            final SynthesizeSpeechRequest synthRequest = new SynthesizeSpeechRequest()
                    .withText(ssml)
                    .withOutputFormat(OutputFormat.Mp3)
                    .withVoiceId(voiceId)
                    .withTextType(TextType.Ssml)
                    .withSampleRate("16000");
            // fire request to Pollu
            final SynthesizeSpeechResult synthResult = awsPolly.synthesizeSpeech(synthRequest);

            try {
                // store audio stream of Polly to S3 as an MP3 file
                final PutObjectRequest s3Put = new PutObjectRequest(SkillConfig.getS3BucketName(), getMp3Path(text), synthResult.getAudioStream(), new ObjectMetadata())
                        .withCannedAcl(CannedAccessControlList.PublicRead);
                awsS3.putObject(s3Put);
                // as long as Polly output does not comply with Alexa MP3 format restriction we need to convert the MP3
                if (!SkillConfig.shouldSkipMp3Conversion()) {
                    // call the REST service that encapsualtes the FFMPEG conversion on a server
                    final String mp3ConvertedUrl = Mp3Converter.convertMp3(getMp3Url(text));
                    // validate this service returned a url (equal to success)
                    Validate.notBlank(mp3ConvertedUrl, "Conversion service did not return proper return value");
                }
                // build the TTS object with all the information needed to return output speech
                return Optional.of(getTTS(text, translated.get()));
            } catch (final IOException | URISyntaxException e) {
                log.error("Error while generating mp3. " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * Does the TTS object creation for you as all related information can be generated
     * from a given text and its translation.
     * @param text the original text
     * @param translatedText the translated text
     * @return TTS object holding all information
     */
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

    /**
     * @return the language a text is translated to. This is taken directly from the
     * user input so it contains the spoken version like 'russian' or 'russisch' in the
     * locale-specific format.
     */
    public String getLanguage() {
        return this.language;
    }
}

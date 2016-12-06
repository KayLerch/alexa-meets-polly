package io.klerch.alexa.translator.skill.util;

import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Optional;

public class TTSPolly {
    private static final Logger log = Logger.getLogger(TTSPolly.class);

    private final String locale;
    private final YamlReader yamlReader;
    final AmazonPolly awsPolly;
    final AmazonS3Client awsS3;

    public TTSPolly(final String locale) {
        this.locale = locale;
        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/voices.yml");
        this.yamlReader = new YamlReader(reader, locale);
        this.awsPolly = new AmazonPollyClient();
        this.awsS3 = new AmazonS3Client();
    }

    public Optional<TextToSpeech> textToSpeech(final String text, final String translated, final String language) {
        final Optional<String> voice = yamlReader.getRandomUtterance(language);

        if (voice.isPresent()) {
            final SynthesizeSpeechRequest synthRequest = new SynthesizeSpeechRequest()
                    .withText(translated)
                    .withOutputFormat(OutputFormat.Mp3)
                    .withVoiceId(voice.get())
                    .withTextType("text")
                    .withSampleRate("16000");
            final SynthesizeSpeechResult synthResult = awsPolly.synthesizeSpeech(synthRequest);

            // mp3 needs conversion to comply with MP3 format supported by Alexa service
            try {
                // now upload stream to S3
                final String filePath = locale + "-" + URLEncoder.encode(text.replace(" ", "_").replace("ü", "ue").replace("ö", "oe").replace("ä", "ae").replace("ß", "ss"), "UTF-8") + "-" + voice.get() + ".mp3";
                final String mp3Url = SkillConfig.getS3BucketUrl() + filePath;

                final PutObjectRequest s3Put = new PutObjectRequest(SkillConfig.getS3BucketName(), filePath, synthResult.getAudioStream(), new ObjectMetadata())
                        .withCannedAcl(CannedAccessControlList.PublicRead);
                awsS3.putObject(s3Put);

                Mp3Converter.convertMp3(mp3Url);
                return Optional.of(TextToSpeech.create()
                        .withText(text).withMp3(mp3Url).withVoice(synthRequest.getVoiceId())
                        .withTranslatedText(translated).build());
            } catch (final IOException | URISyntaxException e) {
                log.error("Error while converting mp3. " + e.getMessage());
            }
        }
        return Optional.empty();
    }
}

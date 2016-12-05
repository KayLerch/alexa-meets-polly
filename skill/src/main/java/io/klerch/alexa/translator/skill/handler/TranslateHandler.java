package io.klerch.alexa.translator.skill.handler;

import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.schema.type.AlexaOutputFormat;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.util.GoogleTranslation;
import io.klerch.alexa.translator.skill.util.Mp3Converter;
import io.klerch.alexa.translator.skill.util.TTSPolly;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

@AlexaIntentListener(customIntents = "Translate")
public class TranslateHandler extends AbstractIntentHandler {
    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final String lang = input.getSlotValue("language");
        final String term = input.getSlotValue("term");

        final String translated = new GoogleTranslation(input.getLocale()).translate(term, lang);
        final TTSPolly polly = new TTSPolly(input.getLocale());
        final InputStream tts = polly.textToSpeech(translated, lang);

        final AmazonS3Client s3Client = new AmazonS3Client();

        String filePath = "";
        try {
            filePath = input.getLocale() + "_" + URLEncoder.encode(term, "UTF-8") + "_" + polly.getVoice() + ".mp3";
            final PutObjectRequest s3Put = new PutObjectRequest(SkillConfig.getS3BucketName(), filePath, tts, new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
            s3Client.putObject(s3Put);
        } catch (final Exception e) {
            throw new AlexaRequestHandlerException("Error uploading mp3.", e, input, null);
        }

        final String mp3Url = SkillConfig.getS3BucketUrl() + filePath;

        try {
            Mp3Converter.convertMp3(mp3Url);
        } catch (URISyntaxException | IOException e) {
            throw new AlexaRequestHandlerException("Error converting mp3.", e, input, null);
        }

        return AlexaOutput.tell("SayTranslate")
                .putSlot("mp3", mp3Url, AlexaOutputFormat.AUDIO)
                .putSlot("language", lang)
                .putSlot("term", term)
                .build();
    }
}

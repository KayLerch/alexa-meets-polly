package io.klerch.alexa.translator.skill.util;

import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;
import java.io.InputStream;
import java.util.Optional;

public class TTSPolly {
    private final String locale;
    private String voice;
    private final YamlReader yamlReader;
    final AmazonPolly awsPolly;

    public TTSPolly(final String locale) {
        this.locale = locale;
        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/voices.yml");
        this.yamlReader = new YamlReader(reader, locale);
        this.awsPolly = new AmazonPollyClient();
    }

    public InputStream textToSpeech(final String text, final String language) {
        final Optional<String> voice = yamlReader.getRandomUtterance(language);

        if (voice.isPresent()) {
            this.voice = voice.get();
            final SynthesizeSpeechRequest synthRequest = new SynthesizeSpeechRequest()
                    .withText(text)
                    .withOutputFormat(OutputFormat.Mp3)
                    .withVoiceId(this.voice)
                    .withTextType("text")
                    .withSampleRate("16000");
            final SynthesizeSpeechResult synthResult = awsPolly.synthesizeSpeech(synthRequest);
            return synthResult.getAudioStream();
        }
        return null;
    }

    public String getVoice() {
        return voice;
    }
}

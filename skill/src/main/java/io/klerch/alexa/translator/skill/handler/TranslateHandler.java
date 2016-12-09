package io.klerch.alexa.translator.skill.handler;

import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import io.klerch.alexa.translator.skill.util.GoogleTranslation;
import io.klerch.alexa.translator.skill.util.TTSPolly;
import org.apache.log4j.Logger;

import java.util.Optional;

@AlexaIntentListener(customIntents = "Translate")
public class TranslateHandler extends AbstractIntentHandler {
    private static final Logger log = Logger.getLogger(TranslateHandler.class);

    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final StringBuilder sb = new StringBuilder();

        if (input.hasSlotNotBlank("termA")) {
            sb.append(input.getSlotValue("termA"));
        }
        if (input.hasSlotNotBlank("termB")) {
            sb.append(" ").append(input.getSlotValue("termB"));
        }

        final String lang = input.getSlotValue("language");
        final String text = sb.toString();

        final TTSPolly ttsPolly = new TTSPolly(input.getLocale(), lang);

        // translate term
        final Optional<String> translated = new GoogleTranslation(input.getLocale()).translate(text, lang);

        if (translated.isPresent()) {
            // translated term to speech
            final Optional<TextToSpeech> tts = ttsPolly.textToSpeech(text, translated.get());

            if (tts.isPresent()) {
                final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
                // set language and handler to keep in mind all information related to last translation
                tts.get().withLanguage(lang).withHandler(dynamoHandler);
                return sayTranslate(tts.get());
            } else {
                log.warn(String.format("Did not get result of text-to-speech for '$1%s'", translated.get()));
            }
        } else {
            log.warn(String.format("Did not get result of translation for '$1%s' from '$2%s' to '$3%s'.", text, input.getLocale(), lang));
        }
        return AlexaOutput.tell("SayNoTranslation")
                .putSlot("text", text)
                .putSlot("language", lang)
                .build();
    }
}

package io.klerch.alexa.translator.skill.handler;

import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.SimpleCard;
import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaIntentHandler;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import io.klerch.alexa.translator.skill.util.GoogleTranslation;
import io.klerch.alexa.translator.skill.util.TTSPolly;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

abstract class AbstractIntentHandler implements AlexaIntentHandler {
    private static final Logger log = Logger.getLogger(AbstractIntentHandler.class);

    @Override
    public boolean verify(final AlexaInput input) {
        return true;
    }

    @Override
    public abstract AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException;

    @Override
    public AlexaOutput handleError(final AlexaRequestHandlerException exception) {
        log.error("ERROR: " + exception.getMessage());
        if (exception.getCause() != null) {
            log.error("ERROR: " + exception.getCause().getMessage());
        }
        return AlexaOutput.tell("SaySorry").build();
    }

    AlexaOutput sayTranslate(final TextToSpeech tts) {
        final Card card = new SimpleCard();
        card.setTitle(tts.getText() + " -> " + tts.getTranslatedText());

        return AlexaOutput.tell("SayTranslate")
                .withCard(card)
                .putState(tts)
                .build();
    }

    AlexaOutput sayTranslate(final AlexaInput input, final String text, final String lang) {
        final TTSPolly ttsPolly = new TTSPolly(input.getLocale(), lang);

        // translate term
        final Optional<String> translated = new GoogleTranslation(input.getLocale()).translate(text, lang);

        if (translated.isPresent()) {
            final String translatedText = StringEscapeUtils.unescapeHtml4(translated.get());
            // translated term to speech
            final Optional<TextToSpeech> tts = ttsPolly.textToSpeech(text, translatedText);

            if (tts.isPresent()) {
                final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
                // set language and handler to keep in mind all information related to last translation
                tts.get().withLanguage(lang).withHandler(dynamoHandler);
                return sayTranslate(tts.get());
            } else {
                log.warn(String.format("Did not get result of text-to-speech for '$1%s'", translatedText));
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

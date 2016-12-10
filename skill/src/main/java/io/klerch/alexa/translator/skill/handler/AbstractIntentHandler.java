package io.klerch.alexa.translator.skill.handler;

import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.SimpleCard;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaIntentHandler;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import io.klerch.alexa.translator.skill.util.TTSPolly;
import org.apache.log4j.Logger;

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

        return ttsPolly.textToSpeech(text).map(this::sayTranslate).orElse(
            AlexaOutput.tell("SayNoTranslation")
                    .putSlot("text", text)
                    .putSlot("language", lang)
                    .build());
    }
}

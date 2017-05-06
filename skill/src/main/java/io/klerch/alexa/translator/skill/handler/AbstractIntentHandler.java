package io.klerch.alexa.translator.skill.handler;

import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.StandardCard;
import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.handler.AlexaStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaIntentHandler;
import io.klerch.alexa.tellask.schema.type.AlexaOutputFormat;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.model.LastTextToSpeech;
import io.klerch.alexa.translator.skill.model.SessionState;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import io.klerch.alexa.translator.skill.tts.TextToSpeechConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

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

        return isConversation(exception.getInput()) ?
                AlexaOutput.ask("SaySorry").withReprompt(true).build() :
                AlexaOutput.tell("SaySorry").build();
    }

    AlexaOutput sayTranslate(final AlexaInput input, final TextToSpeech tts) {
        final StandardCard card = new StandardCard();
        card.setTitle(StringUtils.capitalize(tts.getText()) + " : " + StringUtils.capitalize(tts.getTranslatedText()));
        card.setText("http://aka.ms/MicrosoftTranslatorAttribution");

        final String imgUrl = String.format("%1$s/%2$s-%3$s-%4$s.png", SkillConfig.getS3CardFolderUrl(), input.getLocale(), tts.getVoice().toLowerCase(), SkillConfig.getTranslatorService().toLowerCase());

        final Image img = new Image();
        img.setLargeImageUrl(imgUrl);
        img.setSmallImageUrl(imgUrl);
        card.setImage(img);

        // remember the current tts as last tts of user
        final AWSDynamoStateHandler dynamoStateHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
        final LastTextToSpeech lastTts = new LastTextToSpeech(tts);

        // if no one-shot this conversation keeps open and user can go on with other options
        if (isConversation(input))  {
            return AlexaOutput.ask("SayTranslateAndElse")
                    .withCard(card)
                    .withReprompt(true)
                    .putState(tts, lastTts.withHandler(dynamoStateHandler))
                    .build();
        } else {
            // a one-shot invocation returns the translation and ends the session
            return AlexaOutput.tell("SayTranslate")
                    .withCard(card)
                    .putState(tts, lastTts.withHandler(dynamoStateHandler))
                    .build();
        }
    }

    AlexaOutput sayTranslate(final AlexaInput input, final String text) throws AlexaStateException {
        if (!input.hasSlotNotBlank("language")) {
            return AlexaOutput.ask("SayNoLanguage")
                    .putSlot("text", text)
                    .withReprompt(true)
                    .build();
        }

        final TextToSpeechConverter ttsConverter = new TextToSpeechConverter(input);

        if (ttsConverter.hasSupportedLanguage()) {
            return ttsConverter.textToSpeech(text).map(tts -> sayTranslate(input, tts)).orElse(
                    isConversation(input) ?
                            AlexaOutput.ask("SayNoTranslationAndElse")
                                    .putSlot("text", text)
                                    .putSlot("language", ttsConverter.getLanguage())
                                    .withReprompt(true)
                                    .build() :
                            AlexaOutput.tell("SayNoTranslation")
                                    .putSlot("text", text)
                                    .putSlot("language", ttsConverter.getLanguage())
                                    .build());
        } else {
            return isConversation(input) ?
                    AlexaOutput.ask("SayUnsupportedLanguagesAndElse")
                            .putSlot("language", ttsConverter.getLanguage())
                            .withReprompt(true)
                            .build() :
                    AlexaOutput.tell("SayUnsupportedLanguages")
                            .putSlot("language", ttsConverter.getLanguage())
                            .build();
        }
    }

    boolean isConversation(final AlexaInput input) {
        // find out if this is no one-shot
        Optional<SessionState> sessionState = Optional.empty();
        try {
            sessionState = input.getSessionStateHandler().readModel(SessionState.class);
        } catch (final AlexaStateException e) {
            log.error("Unable to read session state.", e);
        }
        return sessionState.isPresent() && sessionState.get().getConversation();
    }
}

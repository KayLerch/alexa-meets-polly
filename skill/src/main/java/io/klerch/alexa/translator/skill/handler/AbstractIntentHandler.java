package io.klerch.alexa.translator.skill.handler;

import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.StandardCard;
import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaIntentHandler;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.model.LastTextToSpeech;
import io.klerch.alexa.translator.skill.model.TextToSpeech;
import io.klerch.alexa.translator.skill.tts.TextToSpeechConverter;
import org.apache.commons.lang3.StringUtils;
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

        return AlexaOutput.tell("SayTranslate")
                .withCard(card)
                .putState(tts, lastTts.withHandler(dynamoStateHandler))
                .build();
    }

    AlexaOutput sayTranslate(final AlexaInput input, final String text) throws AlexaStateException {
        final TextToSpeechConverter ttsConverter = new TextToSpeechConverter(input);

        return ttsConverter.textToSpeech(text).map(tts -> sayTranslate(input, tts)).orElse(
            AlexaOutput.tell("SayNoTranslation")
                    .putSlot("text", text)
                    .putSlot("language", ttsConverter.getLanguage())
                    .build());
    }
}

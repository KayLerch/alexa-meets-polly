package io.klerch.alexa.translator.skill.handler;

import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.schema.type.AlexaIntentType;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.model.LastTextToSpeech;
import io.klerch.alexa.translator.skill.model.TextToSpeech;

import java.util.Optional;

@AlexaIntentListener(builtInIntents = AlexaIntentType.INTENT_REPEAT)
public class RepeatHandler extends AbstractIntentHandler {
    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession(), SkillConfig.getDynamoTableName());
        // try get last result
        final Optional<LastTextToSpeech> lastTts = dynamoHandler.readModel(LastTextToSpeech.class);

        if (lastTts.isPresent()) {
            final Optional<TextToSpeech> tts = dynamoHandler.readModel(TextToSpeech.class, lastTts.get().getTtsId());
            if (tts.isPresent()) {
                // avoid to rewrite model to dynamo
                tts.get().setHandler(input.getSessionStateHandler());
                return sayTranslate(input, tts.get());
            }
        }
        return isConversation(input) ?
                AlexaOutput.ask("SayNothingToRepeatAndElse").withReprompt(true).build() :
                AlexaOutput.tell("SayNothingToRepeat").build();
    }
}

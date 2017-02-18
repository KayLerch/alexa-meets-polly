package io.klerch.alexa.translator.skill.handler;

import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import org.apache.log4j.Logger;

@AlexaIntentListener(customIntents = "Translate")
public class TranslateHandler extends AbstractIntentHandler {
    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final StringBuilder sb = new StringBuilder();

        if (input.hasSlotNotBlank("termA")) {
            sb.append(input.getSlotValue("termA"));
        }
        if (input.hasSlotNotBlank("termB")) {
            sb.append(" ").append(input.getSlotValue("termB"));
        }
        return sayTranslate(input, sb.toString().trim());
    }
}

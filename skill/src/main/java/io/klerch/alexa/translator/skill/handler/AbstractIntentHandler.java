package io.klerch.alexa.translator.skill.handler;

import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaIntentHandler;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
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
}

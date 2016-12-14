package io.klerch.alexa.translator.skill.handler;

import io.klerch.alexa.state.handler.AlexaStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaLaunchHandler;
import io.klerch.alexa.tellask.schema.annotation.AlexaLaunchListener;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.translator.skill.model.SessionState;

@AlexaLaunchListener
public class LaunchHandler implements AlexaLaunchHandler {

    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final AlexaStateHandler sessionStateHandler = input.getSessionStateHandler();
        final SessionState sessionState = input.getSessionStateHandler().createModel(SessionState.class);
        // remember this skill was started as a conversation (rather than in a oneshot)
        sessionState.setConversation(true);

        return AlexaOutput.ask("SayWelcome")
                .withReprompt(true)
                .putState(sessionState)
                .build();
    }

    public AlexaOutput handleError(final AlexaRequestHandlerException exception) {
        return AlexaOutput.tell("SaySorry").build();
    }
}

package io.klerch.alexa.translator.skill.model;

import io.klerch.alexa.state.model.AlexaScope;
import io.klerch.alexa.state.model.AlexaStateModel;
import io.klerch.alexa.state.model.AlexaStateSave;

@AlexaStateSave(Scope = AlexaScope.SESSION)
public class SessionState extends AlexaStateModel {
    private Boolean isConversation = false;

    public SessionState() {}

    public Boolean getConversation() {
        return isConversation;
    }

    public void setConversation(final Boolean conversation) {
        isConversation = conversation;
    }
}

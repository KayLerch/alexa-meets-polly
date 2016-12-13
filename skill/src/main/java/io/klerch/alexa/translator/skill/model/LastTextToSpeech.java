package io.klerch.alexa.translator.skill.model;

import io.klerch.alexa.state.model.AlexaScope;
import io.klerch.alexa.state.model.AlexaStateModel;
import io.klerch.alexa.state.model.AlexaStateSave;

@AlexaStateSave(Scope= AlexaScope.USER)
public class LastTextToSpeech extends AlexaStateModel {
    // refers to TextToSpeech object in the dictionary
    private String ttsId;

    public LastTextToSpeech() {
        // keep this empty constructor. it is important for the magic reflection of the state handlers
    }

    public LastTextToSpeech(final TextToSpeech tts) {
        ttsId = tts.getId();
    }

    public String getTtsId() {
        return ttsId;
    }
}

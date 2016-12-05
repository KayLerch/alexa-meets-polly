package io.klerch.alexa.translator.skill.model;

import io.klerch.alexa.state.model.AlexaScope;
import io.klerch.alexa.state.model.AlexaStateSave;
import io.klerch.alexa.tellask.schema.annotation.AlexaSlotSave;
import io.klerch.alexa.tellask.schema.type.AlexaOutputFormat;

@AlexaStateSave(Scope = AlexaScope.USER)
public class TextToSpeech {
    @AlexaSlotSave(slotName = "term")
    private final String text;
    @AlexaSlotSave(slotName = "mp3", formatAs = AlexaOutputFormat.AUDIO)
    private final String mp3;
    @AlexaSlotSave(slotName = "voice")
    private final String voice;

    public TextToSpeech(final String text, final String mp3Url, final String voice) {
        this.text = text;
        this.mp3 = mp3Url;
        this.voice = voice;
    }

    public String getText() {
        return text;
    }

    public String getMp3() {
        return mp3;
    }

    public String getVoice() {
        return voice;
    }
}

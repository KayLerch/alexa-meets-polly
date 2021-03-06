package io.klerch.alexa.translator.skill.model;

import io.klerch.alexa.state.model.AlexaScope;
import io.klerch.alexa.state.model.AlexaStateModel;
import io.klerch.alexa.state.model.AlexaStateSave;
import io.klerch.alexa.tellask.schema.annotation.AlexaSlotSave;
import io.klerch.alexa.tellask.schema.type.AlexaOutputFormat;
import org.apache.commons.lang3.Validate;

@AlexaStateSave(Scope = AlexaScope.APPLICATION)
public class TextToSpeech extends AlexaStateModel {
    @AlexaSlotSave(slotName = "translatedText")
    private String translatedText;
    @AlexaSlotSave(slotName = "mp3", formatAs = AlexaOutputFormat.AUDIO)
    private String mp3;
    @AlexaSlotSave(slotName = "voice")
    private String voice;
    @AlexaSlotSave(slotName = "language")
    private String language;
    @AlexaSlotSave(slotName = "text")
    private String text;

    public TextToSpeech() {
        // keep this empty constructor. it is important for the magic reflection of the state handlers
    }

    private TextToSpeech(final TextToSpeechBuilder builder) {
        this.translatedText = builder.translatedText;
        this.text = builder.text;
        this.voice = builder.voice;
        this.language = builder.language;
        this.mp3 = builder.mp3;
    }

    /**
     * @return the translated text
     */
    public String getTranslatedText() {
        return translatedText;
    }

    /**
     * @return the original text before it was translated
     */
    public String getText() {
        return text;
    }

    /**
     * @return the language coming from the user input (e.g. 'russian' or 'russisch')
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return Url to an MP3 file having the output speech of the translated text
     */
    public String getMp3() {
        return mp3;
    }

    /**
     * @return id of the Polly voice used to convert translated text to speech
     */
    public String getVoice() {
        return voice;
    }

    public static TextToSpeechBuilder create() {
        return new TextToSpeechBuilder();
    }

    public static class TextToSpeechBuilder {
        private String translatedText;
        private String mp3;
        private String voice;
        private String language;
        private String text;

        TextToSpeechBuilder() {
        }

        public TextToSpeechBuilder withTranslatedText(final String translatedText) {
            this.translatedText = translatedText;
            return this;
        }

        public TextToSpeechBuilder withVoice(final String voice) {
            this.voice = voice;
            return this;
        }

        public TextToSpeechBuilder withMp3(final String mp3Url) {
            this.mp3 = mp3Url;
            return this;
        }

        public TextToSpeechBuilder withText(final String text) {
            this.text = text;
            return this;
        }

        public TextToSpeechBuilder withLanguage(final String language) {
            this.language = language;
            return this;
        }

        public TextToSpeech build() {
            Validate.notBlank(text, "Text must not be blank.");
            Validate.notBlank(voice, "Voice must not be blank.");
            Validate.notBlank(mp3, "Mp3 must not be blank.");
            return new TextToSpeech(this);
        }
    }
}

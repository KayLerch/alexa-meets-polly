package io.klerch.alexa.translator.skill.translate;

import java.util.Optional;

public interface ITranslator {
    /**
     * Translate the given text into the given language.
     * @param text The text to translate
     * @param language The language to translate the text into
     * @return The translated text
     */
    Optional<String> translate(final String text, final String language);
}

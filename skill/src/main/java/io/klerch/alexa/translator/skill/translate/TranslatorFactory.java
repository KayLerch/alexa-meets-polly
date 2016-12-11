package io.klerch.alexa.translator.skill.translate;

public class TranslatorFactory {
    public static ITranslator getTranslator(final String locale) {
        // TODO: retrieve proper translator according to application configuration
        return new GoogleTranslator(locale);
    }
}

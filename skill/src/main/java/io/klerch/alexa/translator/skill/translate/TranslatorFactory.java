package io.klerch.alexa.translator.skill.translate;

import io.klerch.alexa.translator.skill.SkillConfig;
import org.apache.commons.lang3.Validate;

public class TranslatorFactory {
    /**
     * Gets the appropriate translator set up in the app configuration
     * @param locale the locale coming in with a speechlet request
     * @return appropriate translator set up in the app configuration
     */
    public static Translator getTranslator(final String locale) {
        final String translatorId = SkillConfig.getTranslatorService();
        final Translator translator =
                "Microsoft".equals(translatorId) ? new MicrosoftTranslator(locale) :
                "Google".equals(translatorId) ? new GoogleTranslator(locale) : null;
        Validate.notNull(translator, "Invalid TranslatorService set up in the configuration.");
        return translator;
    }
}

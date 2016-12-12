package io.klerch.alexa.translator.skill.translate;

import io.klerch.alexa.translator.skill.SkillConfig;
import org.apache.commons.lang3.Validate;

public class TranslatorFactory {
    public static ITranslator getTranslator(final String locale) {
        final String translatorId = SkillConfig.getTranslatorService();
        final ITranslator translator =
                "Microsoft".equals(translatorId) ? new MicrosoftTranslator(locale) :
                "Google".equals(translatorId) ? new GoogleTranslator(locale) : null;
        Validate.notNull(translator, "Invalid TranslatorService set up in the configuration.");
        return translator;
    }
}

package io.klerch.alexa.translator.skill.translate;

import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;

import java.util.Optional;

public abstract class AbstractTranslator implements ITranslator {
    final YamlReader yamlReader;
    final String locale;

    public AbstractTranslator(final String locale) {
        this.locale = locale;
        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/languages.yml");
        this.yamlReader = new YamlReader(reader, locale);
    }

    @Override
    public abstract Optional<String> translate(String term, String language);
}

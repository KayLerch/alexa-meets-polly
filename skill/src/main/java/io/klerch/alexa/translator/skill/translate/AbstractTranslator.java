package io.klerch.alexa.translator.skill.translate;

import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;

import java.util.Optional;

public abstract class AbstractTranslator implements ITranslator {
    final YamlReader yamlReader;
    final String locale;

    public AbstractTranslator(final String locale) {
        throw new RuntimeException("Translator must override and implement the default constructor.");
    }

    AbstractTranslator(final String locale, final String yamlFile) {
        // the locale is coming with the speechlet request and indicates to source language to translate from
        this.locale = locale;

        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", yamlFile);
        // the yaml reader reads values from YAML file to get a short code for a language
        this.yamlReader = new YamlReader(reader, locale);
    }

    @Override
    public abstract Optional<String> translate(final String term, final String language);
}

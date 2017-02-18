package io.klerch.alexa.translator.skill.translate;

import com.amazonaws.util.StringUtils;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;

import java.util.Optional;

public abstract class AbstractTranslator implements Translator {
    final YamlReader yamlReader;
    final String locale;
    final String sourceLanguageCode;

    public AbstractTranslator(final String locale) {
        throw new RuntimeException("Translator must override and implement the default constructor.");
    }

    AbstractTranslator(final String locale, final String yamlFile) {
        // the locale is coming with the speechlet request and indicates to source language to translate from
        this.locale = locale;
        this.sourceLanguageCode = this.locale.split("-")[0];

        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", yamlFile);
        // the yaml reader reads values from YAML file to get a short code for a language
        this.yamlReader = new YamlReader(reader, locale);
    }

    @Override
    public final Optional<String> getTargetLangCodeIfSupported(final String language) {
        return Optional.ofNullable(language)
                // map language to target-language-code
                .map(l -> this.yamlReader.getRandomUtterance(l.toLowerCase().replace(" ", "_")).orElse(""))
                // if source and target language are equal target language is not supported
                .filter(c -> !StringUtils.isNullOrEmpty(c) && !c.equalsIgnoreCase(sourceLanguageCode));
    }

    @Override
    public final Optional<String> translate(final String term, final String language) {
        return getTargetLangCodeIfSupported(language)
                // return text as is or delegate translation to child in case source and target language differ
                .map(targetLanguageCode -> targetLanguageCode.equalsIgnoreCase(sourceLanguageCode) ? term : doTranslate(term, targetLanguageCode))
                // translation must be not null or empty
                .filter(translation -> !StringUtils.isNullOrEmpty(translation));
    }

    /**
     * Delegates the actual translation to the child implementation.
     * @param term text to translate
     * @param targetLanguageCode target language code
     * @return translated text
     */
    abstract String doTranslate(final String term, final String targetLanguageCode);
}

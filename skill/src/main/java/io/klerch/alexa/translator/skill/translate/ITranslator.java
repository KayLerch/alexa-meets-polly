package io.klerch.alexa.translator.skill.translate;

import java.util.Optional;

public interface ITranslator {
    Optional<String> translate(final String term, final String language);
}

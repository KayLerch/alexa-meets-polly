package io.klerch.alexa.translator.skill.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import io.klerch.alexa.tellask.util.resource.ResourceUtteranceReader;
import io.klerch.alexa.tellask.util.resource.YamlReader;
import io.klerch.alexa.translator.skill.SkillConfig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleTranslation {
    private static Logger log = Logger.getLogger(GoogleTranslation.class.getName());
    private Translate translator;
    private final String locale;
    private String languageCode;
    private final YamlReader yamlReader;

    public GoogleTranslation(final String locale) {
        this.locale = locale;
        final ResourceUtteranceReader reader = new ResourceUtteranceReader("/out", "/languages.yml");
        this.yamlReader = new YamlReader(reader, locale);

        try {
            this.translator = new Translate.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), null)
                    .setApplicationName(SkillConfig.getGoogleProjectName())
                    .build();
        } catch (final GeneralSecurityException | IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    public Optional<String> translate(final String term, final String language) {
        final Optional<String> code = this.yamlReader.getRandomUtterance(language.replace(" ", "_"));
        final String sourceCode = locale.split("-")[0];

        if (code.isPresent()) {
            // if source and target language are the same return original term
            if (code.get().equalsIgnoreCase(sourceCode)) {
                return Optional.of(term);
            }

            this.languageCode = code.get();
            try {
                final Translate.Translations.List list = translator.new Translations().list(
                        Collections.singletonList(term), this.languageCode);
                list.setKey(SkillConfig.getGoogleApiKey());
                list.setSource(sourceCode);
                final TranslationsListResponse response = list.execute();

                if (!response.isEmpty() && !response.getTranslations().isEmpty()) {
                    return Optional.of(response.getTranslations().get(0).getTranslatedText());
                }
            } catch (IOException e) {
                log.severe(e.getMessage());
            }
        }
        return Optional.empty();
    }

    public String getLanguageCode() {
        return languageCode;
    }
}

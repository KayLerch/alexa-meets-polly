package io.klerch.alexa.translator.skill.translate;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import io.klerch.alexa.translator.skill.SkillConfig;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleTranslator extends AbstractTranslator {
    private static Logger log = Logger.getLogger(GoogleTranslator.class.getName());
    private Translate translator;

    public GoogleTranslator(final String locale) {
        super(locale, "/languages-google.yml");

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

    @Override
    public Optional<String> translate(final String term, final String language) {
        final Optional<String> code = language != null ? this.yamlReader.getRandomUtterance(language.toLowerCase().replace(" ", "_")) : Optional.empty();
        final String sourceCode = this.locale.split("-")[0];

        if (code.isPresent()) {
            // if source and target language are the same return original term
            if (code.get().equalsIgnoreCase(sourceCode)) {
                return Optional.of(term);
            }
            try {
                final Translate.Translations.List list = translator.new Translations().list(
                        Collections.singletonList(term), code.get());
                list.setKey(SkillConfig.getGoogleApiKey());
                list.setSource(sourceCode);
                final TranslationsListResponse response = list.execute();

                if (!response.isEmpty() && !response.getTranslations().isEmpty()) {
                    return Optional.of(StringEscapeUtils.unescapeHtml4(response.getTranslations().get(0).getTranslatedText()));
                }
            } catch (IOException e) {
                log.severe(e.getMessage());
            }
        }
        return Optional.empty();
    }
}

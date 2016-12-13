package io.klerch.alexa.translator.skill.translate;

import io.klerch.alexa.translator.skill.SkillConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Optional;

public class MicrosoftTranslator extends AbstractTranslator {
    private static final String ServiceEndpointIssueToken = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    private static final String ServiceEndpointTranslate = "https://api.microsofttranslator.com/v2/http.svc/Translate";

    public MicrosoftTranslator(final String locale) {
        super(locale, "/languages-azure.yml");
    }

    @Override
    public Optional<String> translate(final String text, final String language) {
        final Optional<String> code = language != null ? this.yamlReader.getRandomUtterance(language.toLowerCase().replace(" ", "_")) : Optional.empty();
        final String sourceCode = this.locale.split("-")[0];

        if (code.isPresent()) {
            try {
                // if source and target language are the same return original term
                if (code.get().equalsIgnoreCase(sourceCode)) {
                    return Optional.of(text);
                }
                final String accessToken = String.format("Bearer %1$s", getAccessToken());
                final URIBuilder uri = new URIBuilder(ServiceEndpointTranslate)
                        .addParameter("appid", accessToken)
                        .addParameter("from", sourceCode)
                        .addParameter("to", code.get())
                        .addParameter("contentType", "text/plain")
                        .addParameter("text", URLEncoder.encode(text, "UTF-8"));
                final HttpGet httpGet = new HttpGet(uri.build());
                final HttpResponse response = HttpClientBuilder.create().build().execute(httpGet);
                // work on response
                final HttpEntity entity = response.getEntity();
                return Optional.of(IOUtils.toString(entity.getContent(), "UTF-8").replaceAll("<[^>]*>", ""));
            } catch (final IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    private String getAccessToken() throws URISyntaxException, IOException {
        // build uri
        final URIBuilder uri = new URIBuilder(ServiceEndpointIssueToken);
        final HttpPost httpPost = new HttpPost(uri.build());
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Ocp-Apim-Subscription-Key", SkillConfig.getMicrosoftSubscriptionKey());
        final HttpResponse response = HttpClientBuilder.create().build().execute(httpPost);
        // work on response
        final HttpEntity entity = response.getEntity();
        return IOUtils.toString(entity.getContent(), "UTF-8");
    }
}

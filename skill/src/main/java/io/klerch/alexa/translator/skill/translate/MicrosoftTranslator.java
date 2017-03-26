package io.klerch.alexa.translator.skill.translate;

import io.klerch.alexa.translator.skill.SkillConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class MicrosoftTranslator extends AbstractTranslator {
    private static Logger log = Logger.getLogger(MicrosoftTranslator.class.getName());
    private static final String ServiceEndpointIssueToken = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    private static final String ServiceEndpointTranslate = "https://api.microsofttranslator.com/v2/http.svc/Translate";

    public MicrosoftTranslator(final String locale) {
        super(locale, "/languages-azure.yml");
    }

    @Override
    public String doTranslate(final String text, final String targetLanguageCode) {
        // if source and target language are the same return original term
        if (targetLanguageCode.equalsIgnoreCase(sourceLanguageCode)) {
            return text;
        }

        try {
            final String accessToken = String.format("Bearer %1$s", getAccessToken());
            final URIBuilder uri = new URIBuilder(ServiceEndpointTranslate)
                    .addParameter("appid", accessToken)
                    .addParameter("from", StringEscapeUtils.escapeHtml4(sourceLanguageCode))
                    .addParameter("to", StringEscapeUtils.escapeHtml4(targetLanguageCode))
                    .addParameter("contentType", "text/plain")
                    .addParameter("text", StringEscapeUtils.escapeHtml4(text));
            final HttpGet httpGet = new HttpGet(uri.build());
            final HttpResponse response = HttpClientBuilder.create().build().execute(httpGet);

            Validate.inclusiveBetween(200, 399, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

            // work on response
            final HttpEntity entity = response.getEntity();
            return IOUtils.toString(entity.getContent(), "UTF-8").replaceAll("<[^>]*>", "");
        } catch (final IOException | URISyntaxException e) {
            log.severe(e.getMessage());
        }
        return null;
    }

    private String getAccessToken() throws URISyntaxException, IOException {
        // build uri
        final URIBuilder uri = new URIBuilder(ServiceEndpointIssueToken);
        final HttpPost httpPost = new HttpPost(uri.build());
        httpPost.setHeader("Content-Type", "text/plain");
        httpPost.setHeader("Ocp-Apim-Subscription-Key", SkillConfig.getMicrosoftSubscriptionKey());
        final HttpResponse response = HttpClientBuilder.create().build().execute(httpPost);

        Validate.inclusiveBetween(200, 399, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

        // work on response
        final HttpEntity entity = response.getEntity();
        return IOUtils.toString(entity.getContent(), "UTF-8");
    }
}

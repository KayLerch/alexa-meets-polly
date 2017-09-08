package io.klerch.alexa.translator.skill.tts;

import io.klerch.alexa.translator.skill.SkillConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

public class Mp3Converter {
    public static String convertMp3(final String mp3Path) throws URISyntaxException, IOException {
        // get credentials for webservice from application config
        final String apiKey = SkillConfig.getTranslatorConvertServiceUser();
        final String apiPass = SkillConfig.getTranslatorConvertServicePass();
        // build uri
        final String bucketName = SkillConfig.getS3BucketName();
        final URIBuilder uri = new URIBuilder(SkillConfig.getTranslatorConvertServiceUrl()).addParameter("bucket", bucketName).addParameter("path", mp3Path);
        // set up web request
        final HttpGet httpGet = new HttpGet(uri.build());
        httpGet.setHeader("Content-Type", "text/plain");
        // set up credentials
        final CredentialsProvider provider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(apiKey, apiPass);
        provider.setCredentials(AuthScope.ANY, credentials);
        // send request to convert webservice
        final HttpResponse response =
                HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build().execute(httpGet);

        //Validate.inclusiveBetween(200, 399, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        // work on response
        final HttpEntity entity = response.getEntity();
        return IOUtils.toString(entity.getContent(), "UTF-8");
    }
}


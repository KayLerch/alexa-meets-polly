package io.klerch.alexa.translator.client;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.lang3.Validate;

import java.util.Map;

public class TranslatorClientRequestHandler implements RequestHandler<Map<String,Object>, String> {
    private static String testPhrase = AppConfig.getTestPhrase();
    private static String locale = AppConfig.getLocale();

    @Override
    public String handleRequest(final Map<String,Object> input, final Context context) {
        final String response = new SkillClient(locale).translate(testPhrase, "englisch");

        Validate.notNull(response, "Skill was not invoked. See log details with error message.");
        Validate.matchesPattern(response, "(?i:.*de-DE/Salli/" + testPhrase +".mp3.*)", "Unexpected response. " + response);

        return "1";
    }
}

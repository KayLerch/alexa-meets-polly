import io.klerch.alexa.test.asset.AlexaAsset;
import io.klerch.alexa.test.client.AlexaClient;
import io.klerch.alexa.test.client.endpoint.AlexaEndpoint;
import io.klerch.alexa.test.client.endpoint.AlexaRequestStreamHandlerEndpoint;
import io.klerch.alexa.translator.skill.SkillConfig;
import io.klerch.alexa.translator.skill.TranslatorSpeechletHandler;

import org.junit.Ignore;
import org.junit.Test;
import java.util.Locale;

public class TranslatorSpeechletHandlerTest {
    public AlexaClient givenClient() throws Exception {
        final AlexaEndpoint endpoint = AlexaRequestStreamHandlerEndpoint.create(TranslatorSpeechletHandler.class).build();
        return AlexaClient.create(endpoint)
                .withLocale(Locale.GERMANY)
                .withApplicationId(SkillConfig.getAlexaAppId())
                .build();
    }

    public AlexaClient givenScriptClient(final String filePath) throws Exception {
        return AlexaClient.create(this.getClass().getResourceAsStream(filePath))
                .withApplicationId(SkillConfig.getAlexaAppId())
                .build();
    }

    @Test
    public void doConversation() throws Exception {
        final AlexaEndpoint endpoint = AlexaRequestStreamHandlerEndpoint.create(TranslatorSpeechletHandler.class).build();

        final AlexaClient client = AlexaClient.create(this.getClass().getResourceAsStream("de-DE/singleTranslationOneShot.xml"))
                .withApplicationId(SkillConfig.getAlexaAppId())
                .build();

        client.startScript();
    }

    @Test
    public void askForHelpOneShot() throws Exception {
        givenScriptClient("de-DE/askForHelpOneShot.xml").startScript();
    }

    @Test
    public void singleTranslationOneShot() throws Exception {
        givenScriptClient("de-DE/singleTranslationOneShot.xml").startScript();
    }

    @Test
    public void singleTranslationWithRepeat() throws Exception {
        givenScriptClient("de-DE/singleTranslationWithRepeat.xml").startScript();
    }

    @Test
    public void unsupportedLanguageTranslation() throws Exception {
        givenScriptClient("de-DE/unsupportedLanguageTranslation.xml").startScript();
    }

    @Test
    public void noLanguageTranslation() throws Exception {
        givenScriptClient("de-DE/noLanguageTranslation.xml").startScript();
    }
}

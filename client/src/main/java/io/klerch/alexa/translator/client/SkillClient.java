package io.klerch.alexa.translator.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SkillClient {
    private static final Logger log = Logger.getLogger(SkillClient.class);

    private static String requestId = AppConfig.getAlexaRequestId();
    private static String sessionId = AppConfig.getAlexaSessionId();
    private static String appId = AppConfig.getAlexaAppId();
    private static String userId = AppConfig.getAlexaUserId();
    private final String locale;
    private static String lambdaName = AppConfig.getLambdaName();

    public SkillClient(final String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return this.locale;
    }

    public String translate(final String testPhrase, final String language) {
        final Map<String, Slot> slots = new HashMap<>();
        slots.put("termA", Slot.builder().withName("termA").withValue(testPhrase).build());
        slots.put("termB", Slot.builder().withName("termB").build());
        slots.put("language", Slot.builder().withName("language").withValue(language).build());
        final SpeechletRequestEnvelope envelope = givenIntentSpeechletRequestEnvelope("Translate", slots);
        final ObjectMapper mapper = new ObjectMapper();
        String response = null;
        try {
            final AWSLambdaClient awsLambda = new AWSLambdaClient();
            final InvokeRequest invokeRequest = new InvokeRequest()
                    .withInvocationType(InvocationType.RequestResponse)
                    .withFunctionName(lambdaName)
                    .withPayload(mapper.writeValueAsString(envelope));
            final InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
            response = new String(invokeResult.getPayload().array());
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return response;
    }

    private SpeechletRequestEnvelope givenLaunchSpeechletRequestEnvelope() {
        return givenLaunchSpeechletRequestEnvelope(appId);
    }

    private SpeechletRequestEnvelope givenLaunchSpeechletRequestEnvelope(final String applicationId) {
        return SpeechletRequestEnvelope.builder()
                .withRequest(givenLaunchRequest())
                .withSession(givenSession(applicationId))
                .withVersion("1.0.0")
                .build();
    }

    private SpeechletRequestEnvelope givenIntentSpeechletRequestEnvelope(final String intentName) {
        return givenIntentSpeechletRequestEnvelope(intentName, null);
    }

    private SpeechletRequestEnvelope givenIntentSpeechletRequestEnvelope(final String intentName, final Map<String, Slot> slots) {
        return SpeechletRequestEnvelope.builder()
                .withRequest(givenIntentRequest(intentName, slots))
                .withSession(givenSession(appId))
                .withVersion("1.0.0")
                .build();
    }

    private Session givenSession() {
        return givenSession(appId);
    }

    private Session givenSession(final String applicationId) {
        final Application application = new Application(applicationId);
        final User user = User.builder().withUserId(userId).withAccessToken("accessToken").build();
        return Session.builder().withSessionId(sessionId)
                .withApplication(application).withUser(user).build();
    }

    private IntentRequest givenIntentRequest(final String intentName, final Map<String, Slot> slots) {
        Map<String, Slot> slotsForSure = slots != null ? slots : new HashMap<>();
        final Intent intent = Intent.builder()
                .withName(intentName)
                .withSlots(slotsForSure)
                .build();
        return IntentRequest.builder()
                .withRequestId(requestId)
                .withTimestamp(new Date())
                .withIntent(intent)
                .withLocale(Locale.forLanguageTag(locale))
                .build();
    }

    private IntentRequest givenIntentRequest(final String intentName) {
        return givenIntentRequest(intentName, null);
    }

    private LaunchRequest givenLaunchRequest() {
        return LaunchRequest.builder()
                .withRequestId(requestId)
                .withTimestamp(new Date())
                .withLocale(Locale.forLanguageTag(locale))
                .build();
    }

    private SessionStartedRequest givenSessionStartedRequest() {
        return SessionStartedRequest.builder()
                .withRequestId(requestId)
                .withLocale(Locale.forLanguageTag(locale))
                .withTimestamp(new Date()).build();
    }

    private SessionEndedRequest givenSessionEndedRequest() {
        return SessionEndedRequest.builder()
                .withRequestId(requestId)
                .withLocale(Locale.forLanguageTag(locale))
                .withReason(SessionEndedRequest.Reason.USER_INITIATED)
                .withTimestamp(new Date()).build();
    }
}

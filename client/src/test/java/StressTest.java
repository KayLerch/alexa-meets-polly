import io.klerch.alexa.translator.client.AppConfig;
import io.klerch.alexa.translator.client.SkillClient;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StressTest {
    final List<String> testPhrases = Arrays.asList("öl", "gut", "fuß", "lob", "tor", "rad", "tee", "rot", "not", "see", "fee", "zeh");
    final String language = "schwedisch";

    @Test
    @Ignore
    public void stress() {
        final ExecutorService executor = Executors.newFixedThreadPool(testPhrases.size());
        final String locale = AppConfig.getLocale();

        testPhrases.forEach(testPhrase -> {
            final Runnable worker = new LambdaRunner(locale, testPhrase, language);
            executor.execute(worker);
        });
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
    }

    public static class LambdaRunner implements Runnable {
        private final SkillClient client;
        private final String testPhrase;
        private final String language;

        LambdaRunner(final String locale, final String testPhrase, final String language) {
            this.client = new SkillClient(locale);
            this.testPhrase = testPhrase;
            this.language = language;
        }

        @Override
        public void run() {
            System.out.println("STARTED: " + testPhrase);
            long startTime = System.currentTimeMillis();
            final String response = client.translate(testPhrase, language);
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("FINISHED: " + testPhrase + " in " + elapsedTime + "ms : " + response);
        }
    }
}

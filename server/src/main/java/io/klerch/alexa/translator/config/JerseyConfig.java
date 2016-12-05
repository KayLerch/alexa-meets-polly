package io.klerch.alexa.translator.config;

import io.klerch.alexa.translator.service.ConvertService;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(ConvertService.class);
    }
}

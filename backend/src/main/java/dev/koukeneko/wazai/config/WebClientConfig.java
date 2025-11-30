package dev.koukeneko.wazai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient beans used by external API integrations.
 */
@Configuration
public class WebClientConfig {

    private static final int MAX_BUFFER_SIZE_MB = 5;
    private static final int MAX_BUFFER_SIZE_BYTES = MAX_BUFFER_SIZE_MB * 1024 * 1024;

    @Bean
    public WebClient.Builder webClientBuilder() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(MAX_BUFFER_SIZE_BYTES))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies);
    }
}

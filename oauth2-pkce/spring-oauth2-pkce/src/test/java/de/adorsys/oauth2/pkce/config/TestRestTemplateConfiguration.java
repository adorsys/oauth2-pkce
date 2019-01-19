package de.adorsys.oauth2.pkce.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.function.Supplier;

@Configuration
public class TestRestTemplateConfiguration {

    /**
     * For testing we need a rest-template without following 302 redirects
     */
    @Bean
    TestRestTemplate restTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .requestFactory(factory());

        return new TestRestTemplate(builder);
    }

    private Supplier<ClientHttpRequestFactory> factory() {
        final HttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);

        return () -> factory;
    }

}

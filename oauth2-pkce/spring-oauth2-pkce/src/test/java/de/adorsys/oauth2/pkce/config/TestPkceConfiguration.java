package de.adorsys.oauth2.pkce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "de.adorsys.oauth2.pkce")
public class TestPkceConfiguration {

    @Bean
    ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

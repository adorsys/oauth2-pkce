package de.adorsys.oauth2.pkce;

import de.adorsys.oauth2.pkce.context.Oauth2PkceFactory;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = {
        "de.adorsys.oauth2.pkce"
})
public class PkceConfiguration {

    @Autowired
    PkceProperties pkceProperties;

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    PkceTokenRequestService oauth2PkceTokenService(
            RestTemplate restTemplate
    ) {
        return new PkceTokenRequestService(restTemplate, pkceProperties);
    }

    @Bean
    Oauth2PkceFactory oauth2PkceFactory() {
        return new Oauth2PkceFactory();
    }

    @Bean
    LoginRedirectService loginRedirectService(
            Oauth2PkceFactory oauth2PkceFactory
    ) {
        return new LoginRedirectService(pkceProperties, oauth2PkceFactory);
    }
}

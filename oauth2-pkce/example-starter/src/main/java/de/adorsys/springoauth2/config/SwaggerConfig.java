package de.adorsys.springoauth2.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(
                        Predicates.or(
                                RequestHandlerSelectors.basePackage("de.adorsys.springoauth2"),
                                RequestHandlerSelectors.basePackage("de.adorsys.oauth2.pkce.endpoint")
                        )
                )
                .paths(PathSelectors.any())
                .build();

        docket.enableUrlTemplating(true);

        return docket;
    }
}

package de.adorsys.springoauth2;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.PkceWebConfig;

/**
 * Reregister request mappings from controllers annotated with @UserResource to the pattern /api/v1/**
 */
@Configuration
public class WebConfig {
    
    @Autowired
    private PkceProperties pkceProperties;

    @Bean
    public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                PkceWebConfig pkceWebConfig = new PkceWebConfig(pkceProperties);
                return new RequestMappingHandlerMapping() {
                    @Override
                    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
                        mapping = pkceWebConfig.registerAPI(handler, method, mapping);
                        super.registerHandlerMethod(handler, method, mapping);
                    }
                };
            }

        };
    }
}

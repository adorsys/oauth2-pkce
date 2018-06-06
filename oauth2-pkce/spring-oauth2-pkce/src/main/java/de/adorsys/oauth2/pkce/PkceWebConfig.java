package de.adorsys.oauth2.pkce;

import de.adorsys.oauth2.pkce.endpoint.PkceRestController;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;

/**
 * Reregister request mappings from PkceController with properties defines outside. see {@link PkceProperties#getAuthEndpoint()}
 */
public class PkceWebConfig {

    private PkceProperties pkceProperties;
    
    public PkceWebConfig(PkceProperties pkceProperties) {
        this.pkceProperties = pkceProperties;
    }

    public RequestMappingInfo registerAPI(Object handler, Method method, RequestMappingInfo mapping) {
        return registerAPI(method, mapping, PkceRestController.class, pkceProperties.getAuthEndpoint());
    }

    private <A> RequestMappingInfo registerAPI(Method method, RequestMappingInfo mapping,
            Class<A> controllerClass, String basePath) {
        Class<?> beanType = method.getDeclaringClass();

        if(beanType==controllerClass){
            // Replace default pattern
            PatternsRequestCondition apiPattern = new PatternsRequestCondition(basePath);
            
            mapping = new RequestMappingInfo(mapping.getName(), apiPattern, mapping.getMethodsCondition(),
                    mapping.getParamsCondition(), mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                    mapping.getProducesCondition(), mapping.getCustomCondition());
        }

        return mapping;
    }
}

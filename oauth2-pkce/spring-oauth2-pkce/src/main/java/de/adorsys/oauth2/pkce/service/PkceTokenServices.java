package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.model.Oauth2Authentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedPrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.*;

public class PkceTokenServices {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final String userInfoEndpointUrl;
    private final String clientId;
    private OAuth2RestOperations restTemplate;
    private String tokenType = "Bearer";
    private AuthoritiesExtractor authoritiesExtractor = new FixedAuthoritiesExtractor();
    private PrincipalExtractor principalExtractor = new FixedPrincipalExtractor();

    public PkceTokenServices(String userInfoEndpointUrl, String clientId) {
        this.userInfoEndpointUrl = userInfoEndpointUrl;
        this.clientId = clientId;
    }

    public Oauth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        Map<String, Object> map = this.getMap(this.userInfoEndpointUrl, accessToken);
        if (map.containsKey("error")) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("userinfo returned error: " + map.get("error"));
            }

            throw new InvalidTokenException(accessToken);
        } else {
            return this.extractAuthentication(map);
        }
    }

    private Oauth2Authentication extractAuthentication(Map<String, Object> map) {
        Object principal = this.getPrincipal(map);
        List<GrantedAuthority> authorities = this.authoritiesExtractor.extractAuthorities(map);
        OAuth2Request request = new OAuth2Request(
                (Map)null,
                this.clientId,
                (Collection)null,
                true,
                (Set)null,
                (Set)null,
                (String)null,
                (Set)null,
                (Map)null
        );
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
        token.setDetails(map);
        return new Oauth2Authentication(request, token);
    }

    protected Object getPrincipal(Map<String, Object> map) {
        Object principal = this.principalExtractor.extractPrincipal(map);
        return principal == null ? "unknown" : principal;
    }

    public OAuth2AccessToken readAccessToken(String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

    private Map<String, Object> getMap(String path, String accessToken) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Getting user info from: " + path);
        }

        try {
            OAuth2RestOperations restTemplate = this.restTemplate;
            if (restTemplate == null) {
                BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
                resource.setClientId(this.clientId);
                restTemplate = new OAuth2RestTemplate(resource);
            }

            OAuth2AccessToken existingToken = ((OAuth2RestOperations)restTemplate).getOAuth2ClientContext().getAccessToken();
            if (existingToken == null || !accessToken.equals(existingToken.getValue())) {
                DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
                token.setTokenType(this.tokenType);
                ((OAuth2RestOperations)restTemplate).getOAuth2ClientContext().setAccessToken(token);
            }

            return (Map)((OAuth2RestOperations)restTemplate).getForEntity(path, Map.class, new Object[0]).getBody();
        } catch (Exception var6) {
            this.logger.warn("Could not fetch user details: " + var6.getClass() + ", " + var6.getMessage());
            return Collections.singletonMap("error", "Could not fetch user details");
        }
    }
}

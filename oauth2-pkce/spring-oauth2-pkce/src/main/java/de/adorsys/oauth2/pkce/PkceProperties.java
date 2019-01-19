package de.adorsys.oauth2.pkce;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "pkce")
@Validated
public class PkceProperties {
    private String clientId;
    private String clientSecret;
    private String accessTokenUri;
    private String userAuthorizationUri;
    private String userInfoUri;
    private String logoutUri;
    private String scope;
    private String codeChallengeMethod;
    private String responseType;
    private Boolean secureCookie = true;

    @Value("${pkce.auth-endpoint:/oauth2/login}")
    private String authEndpoint;

    @Value("${pkce.token-endpoint:/oauth2/token}")
    private String tokenEndpoint;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessTokenUri() {
        return accessTokenUri;
    }

    public void setAccessTokenUri(String accessTokenUri) {
        this.accessTokenUri = accessTokenUri;
    }

    public String getUserAuthorizationUri() {
        return userAuthorizationUri;
    }

    public void setUserAuthorizationUri(String userAuthorizationUri) {
        this.userAuthorizationUri = userAuthorizationUri;
    }

    public String getUserInfoUri() {
        return userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    public String getLogoutUri() {
        return logoutUri;
    }

    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public Boolean getSecureCookie() {
        return secureCookie;
    }

    public void setSecureCookie(Boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }
}

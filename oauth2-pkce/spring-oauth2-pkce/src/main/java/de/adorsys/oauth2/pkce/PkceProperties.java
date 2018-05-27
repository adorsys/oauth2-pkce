package de.adorsys.oauth2.pkce;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
    private String scope;
    private String codeChallengeMethod;
    private String responseType;
    private Boolean secureCookie = true;
    private String authEndpoint;
    // List of requestUrl that lead to auto trigerring auth
    private String userAgentAutoProtectedPages;

    private String accessTokenCookieName;
    private String refreshTokenCookieName;
    // code_verifier
    private String codeVerifierCookieName;
    // =user_agent_state
    private String userAgentStateCookieName;

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

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    public String getUserAgentAutoProtectedPages() {
        return userAgentAutoProtectedPages;
    }

    public void setUserAgentAutoProtectedPages(String userAgentAutoProtectedPages) {
        this.userAgentAutoProtectedPages = userAgentAutoProtectedPages;
    }
    
    public List<String> userAgentAutoProtectedPages(){
        if(StringUtils.isBlank(userAgentAutoProtectedPages)) return Collections.emptyList();
        return Arrays.asList(userAgentAutoProtectedPages.split(","));
    }

    public String getAccessTokenCookieName() {
        return accessTokenCookieName;
    }

    public void setAccessTokenCookieName(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
    }

    public String getRefreshTokenCookieName() {
        return refreshTokenCookieName;
    }

    public void setRefreshTokenCookieName(String refreshTokenCookieName) {
        this.refreshTokenCookieName = refreshTokenCookieName;
    }

    public String getCodeVerifierCookieName() {
        return codeVerifierCookieName;
    }

    public void setCodeVerifierCookieName(String codeVerifierCookieName) {
        this.codeVerifierCookieName = codeVerifierCookieName;
    }

    public String getUserAgentStateCookieName() {
        return userAgentStateCookieName;
    }

    public void setUserAgentStateCookieName(String userAgentStateCookieName) {
        this.userAgentStateCookieName = userAgentStateCookieName;
    }
    
}

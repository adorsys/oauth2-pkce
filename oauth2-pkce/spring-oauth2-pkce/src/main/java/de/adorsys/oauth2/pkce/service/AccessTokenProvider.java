package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.model.Oauth2Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class AccessTokenProvider {

    public OAuth2AccessToken get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OAuth2AccessToken token = null;

        if(authentication != null && authentication instanceof Oauth2Authentication) {
            Oauth2Authentication oauth2Authentication = (Oauth2Authentication)authentication;
            token = oauth2Authentication.getAccessToken();
        }

        if(token == null) {
            throw new RuntimeException("No oauth2 access token");
        }

        return token;
    }
}

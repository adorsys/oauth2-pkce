package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.springframework.web.util.UriComponentsBuilder;

public class LogoutRedirectService {

    private final PkceProperties pkceProperties;

    public LogoutRedirectService(PkceProperties pkceProperties) {
        this.pkceProperties = pkceProperties;
    }

    public LogoutRedirect getRedirect(String redirectUri) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(pkceProperties.getLogoutUri());

        builder.queryParam(TokenConstants.REDIRECT_URI_PARAM_NAME, redirectUri);


        LogoutRedirect logoutRedirect = new LogoutRedirect();
        logoutRedirect.setRedirectUrl(builder.toUriString());

        return logoutRedirect;
    }

    public LogoutRedirect getRedirect() {
        LogoutRedirect logoutRedirect = new LogoutRedirect();
        logoutRedirect.setRedirectUrl(pkceProperties.getLogoutUri());

        return logoutRedirect;
    }

    public static class LogoutRedirect {
        private String redirectUrl;

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }
}

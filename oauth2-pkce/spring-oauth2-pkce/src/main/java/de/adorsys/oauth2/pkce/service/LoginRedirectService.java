package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.basetypes.CodeChallenge;
import de.adorsys.oauth2.pkce.basetypes.CodeVerifier;
import de.adorsys.oauth2.pkce.basetypes.Nonce;
import de.adorsys.oauth2.pkce.basetypes.State;
import de.adorsys.oauth2.pkce.context.Oauth2PkceFactory;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.springframework.web.util.UriComponentsBuilder;

public class LoginRedirectService {

    private final PkceProperties pkceProperties;
    private final Oauth2PkceFactory oauth2PkceFactory;

    public LoginRedirectService(PkceProperties pkceProperties, Oauth2PkceFactory oauth2PkceFactory) {
        this.pkceProperties = pkceProperties;
        this.oauth2PkceFactory = oauth2PkceFactory;
    }

    public LoginRedirect getRedirect(String redirectUri) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(pkceProperties.getUserAuthorizationUri());

        builder.queryParam("client_id", pkceProperties.getClientId());
        builder.queryParam("scope", pkceProperties.getScope());

        CodeVerifier codeVerifier = oauth2PkceFactory.generateCodeVerifier();
        CodeChallenge codeChallenge = oauth2PkceFactory.createCodeChallenge(codeVerifier);
        builder.queryParam("code_challenge", codeChallenge.getValue());
        builder.queryParam("code_challenge_method", pkceProperties.getCodeChallengeMethod());

        Nonce nonce = oauth2PkceFactory.generateNonce();
        builder.queryParam("nonce", nonce.getValue());

        builder.queryParam("response_type", pkceProperties.getResponseType());
        builder.queryParam(TokenConstants.REDIRECT_URI_PARAM_NAME, redirectUri);

        State state = oauth2PkceFactory.generateState();
        builder.queryParam("state", state.getValue());

        builder.queryParam("response_mode", "query");

        LoginRedirect loginRedirect = new LoginRedirect();
        loginRedirect.setCodeVerifier(codeVerifier);
        loginRedirect.setRedirectUrl(builder.toUriString());

        return loginRedirect;
    }

    public static class LoginRedirect {
        private CodeVerifier codeVerifier;
        private String redirectUrl;

        public CodeVerifier getCodeVerifier() {
            return codeVerifier;
        }

        public void setCodeVerifier(CodeVerifier codeVerifier) {
            this.codeVerifier = codeVerifier;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }
}

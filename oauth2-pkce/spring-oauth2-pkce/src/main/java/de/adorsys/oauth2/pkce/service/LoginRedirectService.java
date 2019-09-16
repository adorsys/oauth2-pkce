package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.model.CodeChallenge;
import de.adorsys.oauth2.pkce.model.CodeVerifier;
import de.adorsys.oauth2.pkce.model.Nonce;
import de.adorsys.oauth2.pkce.model.State;
import de.adorsys.oauth2.pkce.util.Oauth2PkceFactory;
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

        State state = oauth2PkceFactory.generateState();
        builder.queryParam("state", state.getValue());

        builder.queryParam("response_mode", "query");

        builder.queryParam(TokenConstants.REDIRECT_URI_PARAM_NAME, redirectUri);

        LoginRedirect loginRedirect = new LoginRedirect();
        loginRedirect.setCodeVerifier(codeVerifier);
        loginRedirect.setRedirectUrl(builder.toUriString());
        loginRedirect.setGeneratedNonce(nonce);

        return loginRedirect;
    }

    public static class LoginRedirect {
        private CodeVerifier codeVerifier;
        private String redirectUrl;
        private Nonce generatedNonce;

        public CodeVerifier getCodeVerifier() {
            return codeVerifier;
        }

        void setCodeVerifier(CodeVerifier codeVerifier) {
            this.codeVerifier = codeVerifier;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        public Nonce getGeneratedNonce() {
            return generatedNonce;
        }

        void setGeneratedNonce(Nonce generatedNonce) {
            this.generatedNonce = generatedNonce;
        }
    }
}

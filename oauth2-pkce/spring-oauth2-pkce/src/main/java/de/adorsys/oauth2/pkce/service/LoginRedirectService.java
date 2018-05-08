package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.context.Oauth2PkceContext;
import de.adorsys.oauth2.pkce.PkceProperties;
import org.springframework.web.util.UriComponentsBuilder;

public class LoginRedirectService {

    private final PkceProperties pkceProperties;
    private final Oauth2PkceContext context;

    public LoginRedirectService(PkceProperties pkceProperties, Oauth2PkceContext context) {
        this.pkceProperties = pkceProperties;
        this.context = context;
    }

    public String getRedirectUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(pkceProperties.getUserAuthorizationUri());

        builder.queryParam("client_id", pkceProperties.getClientId());
        builder.queryParam("scope", pkceProperties.getScope());

        builder.queryParam("code_challenge", context.getCodeChallenge().getValue());
        builder.queryParam("code_challenge_method", pkceProperties.getCodeChallengeMethod());

        builder.queryParam("nonce", context.getNonce().getValue());

        builder.queryParam("response_type", pkceProperties.getResponseType());
        builder.queryParam("redirect_uri", pkceProperties.getRedirectUri());

        builder.queryParam("state", context.getState().getValue());

        return builder.build().toUriString();
    }
}

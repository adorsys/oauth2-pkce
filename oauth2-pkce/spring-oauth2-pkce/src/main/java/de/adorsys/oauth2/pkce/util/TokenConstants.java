package de.adorsys.oauth2.pkce.util;

public interface TokenConstants {

    String AUTHORIZATION_HEADER_TOKEN_PREFIX = "Bearer ";

    String AUTHORIZATION_HEADER_NAME = "Authorization";

    String REDIRECT_URI_PARAM_NAME = "redirect_uri";

    String CODE_REQUEST_PARAMETER_NAME = "code";

    String CODE_VERIFIER_COOKIE_NAME = "code_verifier";

    String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    String USER_AGENT_STATE_COOKIE_NAME = "user_agent_state";

    String REFERER_HEADER_KEYWORD = "Referer";
}

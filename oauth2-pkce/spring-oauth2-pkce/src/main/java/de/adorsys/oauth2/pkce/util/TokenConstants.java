package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.endpoint.PkceRestLogoutController;

public interface TokenConstants {

    String AUTHORIZATION_HEADER_TOKEN_PREFIX = "Bearer ";

    String AUTHORIZATION_HEADER_NAME = "Authorization";

    String USER_AGENT_PAGE_ATTRIBUTE = "userAgentPage";

    String REDIRECT_URI_PARAM_NAME = "redirect_uri";

    String CODE_REQUEST_PARAMETER_NAME = "code";

    /**
     * Endpoint path to trigger the logout.
     * <p>
     * WARN : We do not use '/logout' as the end-point to logout, since that will conflict with the
     * (/logout) used in most spring-security enabled project.
     *
     * @see PkceRestLogoutController#logout(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    String LOGOUT_LINK = "/oauth2/logout";

    String REFERER_HEADER_KEYWORD = "Referer";
}

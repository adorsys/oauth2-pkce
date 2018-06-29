package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.endpoint.PkceRestLogoutController;

public interface TokenConstants {

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    
    public static final String USER_AGENT_PAGE_ATTRIBUTE = "userAgentPage";
    
    public static final String REDIRECT_URI_PARAM_NAME="redirect_uri";
    
    public static final String CODE_REQUEST_PARAMETER_NAME = "code";
    
    /**
     * Endpoint path to trigger the logout.
     * 
     * WARN : We do not use '/logout' as the end-point to logout, since that will conflict with the
     * (/logout) used in most spring-security enabled project.
     * 
     * @see PkceRestLogoutController#logout(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
	public static final String LOGOUT_LINK = "/oauth2/logout";


}


package de.adorsys.oauth2.pkce.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService.TokenResponse;
import de.adorsys.oauth2.pkce.util.TokenConstants;

/**
 * Check if a secure cookies is present and then add it to the request header.
 *
 * @author bwa
 */

@Component
public class CookiesAuthenticationFilter extends GenericFilterBean {

    @Autowired
    private PkceTokenRequestService authenticationService;

    @Autowired
    private PkceProperties pkceProperties;

    // ~ Methods
    // ========================================================================================================

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);

        if (request.getHeader(TokenConstants.AUTHORIZATION_HEADER_NAME) == null) {
            // Move token from cookie to authorization header if available.
            cookieToAuthHeader(request, response, requestWrapper);
        } else {

        }
        chain.doFilter(requestWrapper, response);
    }

    private void cookieToAuthHeader(HttpServletRequest request, HttpServletResponse response, HeaderMapRequestWrapper requestWrapper) {
        String accessToken = null;

        Cookie accessTokenCookie = WebUtils.getCookie(request, TokenConstants.ACCESS_TOKEN_COOKIE_NAME);

        if (accessTokenCookie == null || StringUtils.isBlank(accessTokenCookie.getValue())) {
            Cookie refreshTokenCookie = WebUtils.getCookie(request, TokenConstants.REFRESH_TOKEN_COOKIE_NAME);
            if (refreshTokenCookie != null && StringUtils.isNotBlank(refreshTokenCookie.getValue())) {
                accessToken = mightRefreshAccessToken(request, response, refreshTokenCookie.getValue());
            }
        } else {
            accessToken = accessTokenCookie.getValue();
        }

        // Attach the access token in the
        if (StringUtils.isNotBlank(accessToken)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding access_token from secure cookies to the Authorization header. '");
            }
            requestWrapper.addHeader(TokenConstants.AUTHORIZATION_HEADER_NAME, "Bearer " + accessToken);
        }
    }

    private String mightRefreshAccessToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        TokenResponse newBearerToken = authenticationService.refreshAccessToken(refreshToken);
        updateCookiesValueInResponse(response, newBearerToken);
        return newBearerToken != null ? newBearerToken.getAccess_token() : null;
    }

    protected void updateCookiesValueInResponse(HttpServletResponse response, TokenResponse newBearerToken) {
        String accessTokenToken = newBearerToken != null ? newBearerToken.getAccess_token() : null;
        String refreshToken = newBearerToken != null ? newBearerToken.getRefresh_token() : null;
        int expireIn = newBearerToken != null ? newBearerToken.getExpires_in().intValue() : 0;
        int refreshTokenExpireIn = newBearerToken != null ? newBearerToken.anyRefreshTokenExpireIn().intValue() : 0;

        response.addCookie(createCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, accessTokenToken, expireIn));
        response.addCookie(createCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTokenExpireIn));
    }

    private Cookie createCookie(String name, String token, int expiration) {
        Cookie cookie = new Cookie(name, token);

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(expiration);
        return cookie;
    }

}


package de.adorsys.oauth2.pkce.filter;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService.TokenResponse;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Check if a secure cookies is present and then add it to the request header.
 *
 * @author bwa
 */

@Component
public class CookiesAuthenticationFilter implements Filter {

    private final PkceTokenRequestService authenticationService;
    private final PkceProperties pkceProperties;
    private final CookieService cookieService;

    @Autowired
    public CookiesAuthenticationFilter(
            PkceTokenRequestService authenticationService,
            PkceProperties pkceProperties,
            CookieService cookieService
    ) {
        this.authenticationService = authenticationService;
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
    }


    // ~ Implementation of Filter interface
    // ========================================================================================================

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);

        if (request.getHeader(TokenConstants.AUTHORIZATION_HEADER_NAME) == null) {
            // Move token from cookie to authorization header if available.
            cookieToAuthHeader(request, response, requestWrapper);
        }

        chain.doFilter(requestWrapper, response);
    }

    @Override
    public void init(FilterConfig paramFilterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    // ~ Protected hook methods
    // ========================================================================================================

    protected void updateCookiesValueInResponse(HttpServletResponse response, TokenResponse refreshedBearerToken) {
        String accessTokenToken = refreshedBearerToken != null ? refreshedBearerToken.getAccess_token() : null;
        String refreshToken = refreshedBearerToken != null ? refreshedBearerToken.getRefresh_token() : null;
        int expireIn = refreshedBearerToken != null ? refreshedBearerToken.getExpires_in().intValue() : 0;
        int refreshTokenExpireIn = refreshedBearerToken != null ? refreshedBearerToken.anyRefreshTokenExpireIn().intValue() : 0;

        response.addCookie(createCookie(pkceProperties.getAccessTokenCookieName(), accessTokenToken, expireIn));
        response.addCookie(createCookie(pkceProperties.getRefreshTokenCookieName(), refreshToken, refreshTokenExpireIn));
    }

    // ~ Private methods
    // ========================================================================================================

    private void cookieToAuthHeader(HttpServletRequest request, HttpServletResponse response, HeaderMapRequestWrapper requestWrapper) {
        String accessToken = null;

        Cookie accessTokenCookie = WebUtils.getCookie(request, pkceProperties.getAccessTokenCookieName());

        if (accessTokenCookie == null || StringUtils.isBlank(accessTokenCookie.getValue())) {
            Cookie refreshTokenCookie = WebUtils.getCookie(request, pkceProperties.getRefreshTokenCookieName());
            if (refreshTokenCookie != null && StringUtils.isNotBlank(refreshTokenCookie.getValue())) {
                accessToken = mightRefreshAccessToken(response, refreshTokenCookie.getValue());
            }
        } else {
            accessToken = accessTokenCookie.getValue();
        }

        // Attach the access token in the
        if (StringUtils.isNotBlank(accessToken)) {
            requestWrapper.addHeader(TokenConstants.AUTHORIZATION_HEADER_NAME, "Bearer " + accessToken);
        }
    }

    private String mightRefreshAccessToken(HttpServletResponse response, String refreshToken) {
        TokenResponse refreshedBearerToken = authenticationService.refreshAccessToken(refreshToken);
        updateCookiesValueInResponse(response, refreshedBearerToken);

        return refreshedBearerToken != null ? refreshedBearerToken.getAccess_token() : null;
    }

    private Cookie createCookie(String name, String token, int expiration) {
        return cookieService.creationCookie(name, token, "/", expiration);
    }
}

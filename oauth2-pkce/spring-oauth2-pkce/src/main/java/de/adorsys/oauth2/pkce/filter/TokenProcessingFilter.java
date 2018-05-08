package de.adorsys.oauth2.pkce.filter;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.mapping.BearerTokenMapper;
import de.adorsys.oauth2.pkce.model.Oauth2Authentication;
import de.adorsys.oauth2.pkce.service.PkceTokenServices;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class TokenProcessingFilter extends GenericFilterBean {

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final PkceTokenServices tokenServices;
    private final PkceProperties pkceProperties;
    private final BearerTokenMapper bearerTokenMapper;

    public TokenProcessingFilter(
            PkceTokenServices tokenServices,
            PkceProperties pkceProperties,
            BearerTokenMapper bearerTokenMapper
    ) {
        this.tokenServices = tokenServices;
        this.pkceProperties = pkceProperties;
        this.bearerTokenMapper = bearerTokenMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            authentication = readAuthenticationFromRequest((HttpServletRequest)request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Authentication readAuthenticationFromRequest(HttpServletRequest request) throws AuthenticationException, IOException, ServletException {
        Optional<OAuth2AccessToken> accessTokenFromRequest = tryToReadTokenFromRequest(request);

        if (accessTokenFromRequest.isPresent()) {
            try {
                OAuth2AccessToken accessToken = accessTokenFromRequest.get();
                Oauth2Authentication result = tokenServices.loadAuthentication(accessToken.getValue());
                if (authenticationDetailsSource != null) {
                    request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, accessToken.getValue());
                    request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, accessToken.getTokenType());
                    result.setDetails(authenticationDetailsSource.buildDetails(request));
                }

                result.setAccessToken(accessToken);

                return result;
            } catch (InvalidTokenException e) {
                BadCredentialsException bad = new BadCredentialsException("Could not obtain user details from accessToken", e);
                throw bad;
            }
        }

        return null;
    }

    private Optional<OAuth2AccessToken> tryToReadTokenFromRequest(HttpServletRequest request) {
        Optional<OAuth2AccessToken> accessTokenFromRequest = tryToReadTokenFromCookie(request);

        if(!accessTokenFromRequest.isPresent()) {
            accessTokenFromRequest = tryToReadTokenFromHeader(request);
        }

        return accessTokenFromRequest;
    }

    private Optional<OAuth2AccessToken> tryToReadTokenFromHeader(HttpServletRequest request) {
        String authentication = request.getHeader("Authentication");
        if (authentication == null) {
            return Optional.empty();
        }

        String[] authenticationHeaderSplit = authentication.split("Bearer ");
        OAuth2AccessToken bearerToken = null;

        if (authenticationHeaderSplit.length > 1) {
            String bearerTokenAsBase64 = authenticationHeaderSplit[1];
            bearerToken = bearerTokenMapper.mapFromBase64(bearerTokenAsBase64);
        }

        return Optional.ofNullable(bearerToken);
    }

    private Optional<OAuth2AccessToken> tryToReadTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(c -> pkceProperties.getCookieName().equalsIgnoreCase(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .map(v -> bearerTokenMapper.mapFromBase64(v));
    }
}

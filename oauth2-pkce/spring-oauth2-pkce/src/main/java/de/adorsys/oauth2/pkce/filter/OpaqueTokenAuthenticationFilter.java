package de.adorsys.oauth2.pkce.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.adorsys.oauth2.pkce.exception.UnauthorizedException;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.model.UserInfo;

public class OpaqueTokenAuthenticationFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(OpaqueTokenAuthenticationFilter.class);

    private final PkceTokenRequestService pkceTokenRequestService;

    public OpaqueTokenAuthenticationFilter(PkceTokenRequestService pkceTokenRequestService) {
        this.pkceTokenRequestService = pkceTokenRequestService;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(logger.isTraceEnabled()) logger.trace("doFilter start");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication == null) {
                if(logger.isDebugEnabled()) logger.debug("No authentication got from context");

                authentication = getAuthentication((HttpServletRequest) request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);
        } catch(UnauthorizedException e) {
            if(logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }

            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }

        if(logger.isTraceEnabled()) logger.trace("doFilter end");
    }

    private Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(TokenConstants.AUTHORIZATION_HEADER_NAME);
        if (StringUtils.isBlank(token)) {
            if(logger.isDebugEnabled()) logger.debug("Token value got from header {} is blank", TokenConstants.AUTHORIZATION_HEADER_NAME);
            return null;
        }

        if (!StringUtils.startsWithIgnoreCase(token, TokenConstants.AUTHORIZATION_HEADER_TOKEN_PREFIX)) {
            if(logger.isDebugEnabled()) logger.debug("Token value {} does not start with {}", token, TokenConstants.AUTHORIZATION_HEADER_TOKEN_PREFIX);
            return null;
        }

        String accessToken = StringUtils.substringAfterLast(token, " ");

        if (!isOpaqueToken(accessToken)){
            if(logger.isDebugEnabled()) logger.debug("access-token is no opaque-token");
            return null;
        }

        UserInfo userInfo = pkceTokenRequestService.userInfo(accessToken);
        // process roles
        List<GrantedAuthority> authorities = new ArrayList<>();
        return new UsernamePasswordAuthenticationToken(userInfo.getSub(), userInfo, authorities);
    }

    // First attemp. Count dots. We assume opaque token does not contain dots.
    private boolean isOpaqueToken(String strippedToken) {
        return StringUtils.countMatches(strippedToken, ".") == 0;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}

package de.adorsys.springoauth2;

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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.service.UserInfo;

@Component
public class OpaqueTokenAuthenticationFilter implements Filter {
    static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_KEY = "Authorization";
    
    @Autowired
    private PkceTokenRequestService pkceTokenRequestService;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            authentication = getAuthentication((HttpServletRequest) request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);

    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_KEY);
        if(StringUtils.isBlank(token)) return null;
        if(!StringUtils.startsWithIgnoreCase(token, "Bearer ")) return null;
        String accessToken = StringUtils.substringAfterLast(token, " ");

        if(!isOpaqueToken(accessToken)) return null;
        
        UserInfo userInfo = pkceTokenRequestService.userInfo(accessToken);
        // process roles
        List<GrantedAuthority> authorities = new ArrayList<>();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userInfo.getSub(), userInfo, authorities);
        return authenticationToken;
    }

    // First attemp. Count dots. We assume opaque token does not contain dots.
    private boolean isOpaqueToken(String strippedToken) {
        return StringUtils.countMatches(strippedToken, ".")==0;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
    
}

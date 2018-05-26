package de.adorsys.oauth2.pkce.filter;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.oauth2.pkce.basetypes.ByteArray;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.util.Base64Encoder;
import de.adorsys.oauth2.pkce.util.TokenConstants;

@Component
public class ClientAuthencationEntryPoint implements Filter {
    
    Logger logger = Logger.getLogger(ClientAuthencationEntryPoint.class.getName());

    @Autowired
    private CookieService cookieService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // swagger-ui.html, Referer, redirec-uri for app.
        // oauth/pkce
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String requestUrl = request.getRequestURL().toString();

        String header = request.getHeader(TokenConstants.AUTHORIZATION_HEADER_NAME);
        if (header != null) {
            chain.doFilter(request, response);
            return;
        }
        
        // If request is a call from swagger ui
        String targetRequest = "/swagger-ui.html"; 
        if(StringUtils.containsIgnoreCase(requestUrl, targetRequest)){
            ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequestUri(request);
            String ext = builder.removePathExtension();
            String redirectUri = builder.replacePath("/oauth/pkce").build().toUriString();
            // Redirect to oauth pkce
            response.addCookie(redirectCookie(targetRequest, redirectUri));
            response.sendRedirect("/oauth/pkce?redirect_uri="+redirectUri);
            return;
        }

        // If request is a redirect back from idp
        if(StringUtils.containsIgnoreCase(requestUrl, "/oauth/pkce") && StringUtils.isNotBlank(request.getParameter("code"))){
            RedirectCookie redirectCookie = readRedirectCookie(request);
            if(redirectCookie!=null){
                
                // Add redirect uri
                request.setAttribute("redirect_uri", redirectCookie.getRedirectUri());
                request.setAttribute(TokenConstants.CLIENT_DISPLAY_PAGE, redirectCookie.getClientDisplayPage());
                // delete cookie
                response.addCookie(deleteRedirectCookie());
                
                // proceed with request
                chain.doFilter(request, response);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }

    private Cookie deleteRedirectCookie() {
        return cookieService.createDeletionCookie(TokenConstants.REDIRECT_COOKIE_NAME, "/oauth/pkce");
    }

    ObjectMapper objectMapper = new ObjectMapper();
    Base64Encoder base64Encoder = new Base64Encoder();

    private Cookie redirectCookie(String origLocation, String redirectUri) throws JsonProcessingException {
        RedirectCookie redirectCookie = new RedirectCookie(origLocation, redirectUri);
        String cokieValue = base64Encoder.toBase64(new ByteArray(objectMapper.writeValueAsBytes(redirectCookie)));
        return cookieService.createCookie(TokenConstants.REDIRECT_COOKIE_NAME, cokieValue, "/oauth/pkce", 3600);
    }
    
    private RedirectCookie readRedirectCookie(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, TokenConstants.REDIRECT_COOKIE_NAME);
        if(cookie==null || StringUtils.isBlank(cookie.getValue())) return null;
        byte[] decoded = Base64.getDecoder().decode(cookie.getValue());
        try {
            return objectMapper.readValue(decoded, RedirectCookie.class);
        } catch (IOException e) {
            logger.severe(e.getMessage());
            // Log exception
            return null;
        }
    }

    @Override
    public void destroy() {
    }
    
    @Override
    public void init(FilterConfig paramFilterConfig) throws ServletException {
    }
    
    public static class RedirectCookie {
        private String redirectUri;
        private String clientDisplayPage;
        
        public RedirectCookie() {
        }
        public RedirectCookie(String origLocation, String redirectUri) {
            super();
            this.redirectUri = redirectUri;
            this.clientDisplayPage = origLocation;
        }
        public String getRedirectUri() {
            return redirectUri;
        }
        public String getClientDisplayPage() {
            return clientDisplayPage;
        }
        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
        public void setClientDisplayPage(String origLocation) {
            this.clientDisplayPage = origLocation;
        }
        
    }
}

package de.adorsys.oauth2.pkce.filter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
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

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.basetypes.ByteArray;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.util.Base64Encoder;
import de.adorsys.oauth2.pkce.util.TokenConstants;

@Component
public class ClientAuthencationEntryPoint implements Filter {
    
    Logger logger = Logger.getLogger(ClientAuthencationEntryPoint.class.getName());

    @Autowired
    private CookieService cookieService;
    
    @Autowired
    private PkceProperties pkceProperties;
    
    List<String> userAgentAutoProtectedPages;
    
    @PostConstruct
    public void postConstruct(){
        userAgentAutoProtectedPages = pkceProperties.userAgentAutoProtectedPages();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // swagger-ui.html, Referer, redirec-uri for app.
        // oauth/pkce
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String requestUrl = request.getRequestURL().toString();
        String authEndpoint = pkceProperties.getAuthEndpoint();

        String header = request.getHeader(TokenConstants.AUTHORIZATION_HEADER_NAME);
        if (header != null) {
            chain.doFilter(request, response);
            return;
        }
        
        // If request is a call from swagger ui
        Optional<String> targetRequestPresent = findTargetRequest(requestUrl); 
        if(targetRequestPresent.isPresent()){
            String targetRequest = targetRequestPresent.get();
            ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequestUri(request);
            String ext = builder.removePathExtension();
            String redirectUri = builder.replacePath(authEndpoint).build().toUriString();
            // Redirect to oauth pkce
            response.addCookie(redirectCookie(targetRequest, redirectUri));
            response.sendRedirect(authEndpoint + "?"+TokenConstants.REDIRECT_URI_PARAM_NAME+"="+redirectUri);
            return;
        }

        // If request is a redirect back from idp
        if(StringUtils.endsWithIgnoreCase(requestUrl, authEndpoint) && StringUtils.isNotBlank(request.getParameter("code"))){
            UserAgentStateCookie redirectCookie = readUserAgentStateCookie(request);
            if(redirectCookie!=null){
                
                // Add redirect uri
                request.setAttribute(TokenConstants.REDIRECT_URI_PARAM_NAME, redirectCookie.getRedirectUri());
                request.setAttribute(TokenConstants.USER_AGENT_PAGE_ATTRIBUTE, redirectCookie.getUserAgentPage());
                // delete cookie
                response.addCookie(deleteUserAgentStateCookie());
                
                // proceed with request
                chain.doFilter(request, response);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }

    private Optional<String> findTargetRequest(String requestUrl) {
        return userAgentAutoProtectedPages.stream().filter(s -> StringUtils.endsWithIgnoreCase(requestUrl, s)).findFirst();
    }

    private Cookie deleteUserAgentStateCookie() {
        return cookieService.deletionCookie(pkceProperties.getUserAgentStateCookieName(), pkceProperties.getAuthEndpoint());
    }

    ObjectMapper objectMapper = new ObjectMapper();
    Base64Encoder base64Encoder = new Base64Encoder();

    private Cookie redirectCookie(String origLocation, String redirectUri) throws JsonProcessingException {
        UserAgentStateCookie userAgentStateCookie = new UserAgentStateCookie(origLocation, redirectUri);
        String cokieValue = base64Encoder.toBase64(new ByteArray(objectMapper.writeValueAsBytes(userAgentStateCookie)));
        return cookieService.creationCookie(pkceProperties.getUserAgentStateCookieName(), cokieValue, pkceProperties.getAuthEndpoint(), 3600);
    }
    
    private UserAgentStateCookie readUserAgentStateCookie(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, pkceProperties.getUserAgentStateCookieName());
        if(cookie==null || StringUtils.isBlank(cookie.getValue())) return null;
        byte[] decoded = Base64.getDecoder().decode(cookie.getValue());
        try {
            return objectMapper.readValue(decoded, UserAgentStateCookie.class);
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
    
    public static class UserAgentStateCookie {
        // URI used to redirect controll to client after tocken obtained.
        private String redirectUri;
        // location to redirect to.
        private String userAgentPage;
        
        public UserAgentStateCookie() {
        }
        public UserAgentStateCookie(String userAgentPage, String redirectUri) {
            super();
            this.redirectUri = redirectUri;
            this.userAgentPage = userAgentPage;
        }
        public String getRedirectUri() {
            return redirectUri;
        }
        public String getUserAgentPage() {
            return userAgentPage;
        }
        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
        public void setuserAgentPage(String userAgentPage) {
            this.userAgentPage = userAgentPage;
        }
        
    }
}

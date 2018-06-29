package de.adorsys.oauth2.pkce.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.util.Base64Encoder;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.WebUtils;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class ClientAuthencationEntryPoint implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ClientAuthencationEntryPoint.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Base64Encoder base64Encoder = new Base64Encoder();

    @Autowired
    private CookieService cookieService;

    @Autowired
    private PkceProperties pkceProperties;

    private List<String> userAgentAutoProtectedPages;

    @PostConstruct
    public void postConstruct() {
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
        
        if (StringUtils.endsWith(requestUrl, TokenConstants.LOGOUT_LINK)) {
        	chain.doFilter(request, response);
        	return;
        }

        // If request is a call from auto-protected-pages 
        Optional<String> targetRequestPresent = findTargetRequest(requestUrl, request);
        Optional<UserAgentStateCookie> optionalUserAgentStateCookie = readUserAgentStateCookie(request);

        if (targetRequestPresent.isPresent() && !optionalUserAgentStateCookie.isPresent()) {
            String targetRequest = targetRequestPresent.get();
            ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequestUri(request);
            String ext = builder.removePathExtension();
            String redirectUri = builder.replacePath(authEndpoint).build().toUriString();
            // Redirect to oauth pkce
            response.addCookie(createRedirectCookie(targetRequest, redirectUri));
            response.sendRedirect(redirectUri + "?" + TokenConstants.REDIRECT_URI_PARAM_NAME + "=" + redirectUri);

            return;
        }

        // If request is a redirect back from idp
        if (StringUtils.endsWithIgnoreCase(requestUrl, authEndpoint) && StringUtils.isNotBlank(request.getParameter("code"))) {
            if (optionalUserAgentStateCookie.isPresent()) {
                UserAgentStateCookie redirectCookie = optionalUserAgentStateCookie.get();

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

    private Optional<String> findTargetRequest(String requestUrl, HttpServletRequest request) {
        Optional<String> found = userAgentAutoProtectedPages.stream().filter(s -> StringUtils.endsWithIgnoreCase(requestUrl, s)).findFirst();
        if (found.isPresent()) return found;
        return findFromReferer(request);
    }

    private Optional<String> findFromReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return userAgentAutoProtectedPages.stream().filter(s -> StringUtils.startsWithIgnoreCase(referer, s)).findFirst();
    }

    private Cookie deleteUserAgentStateCookie() {
        return cookieService.deletionCookie(pkceProperties.getUserAgentStateCookieName(), pkceProperties.getAuthEndpoint());
    }

    private Cookie createRedirectCookie(String origLocation, String redirectUri) throws JsonProcessingException {
        UserAgentStateCookie userAgentStateCookie = new UserAgentStateCookie(origLocation, redirectUri);
        byte[] userAgentStateAsBytes = objectMapper.writeValueAsBytes(userAgentStateCookie);
        String cookieValue = base64Encoder.toBase64(userAgentStateAsBytes);

        return cookieService.creationCookieWithDefaultDuration(
                pkceProperties.getUserAgentStateCookieName(),
                cookieValue,
                pkceProperties.getAuthEndpoint()
        );
    }

    private Optional<UserAgentStateCookie> readUserAgentStateCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, pkceProperties.getUserAgentStateCookieName());

        if (cookie == null || StringUtils.isBlank(cookie.getValue()))
            return Optional.empty();

        byte[] decoded = Base64.getDecoder().decode(cookie.getValue());

        try {
            UserAgentStateCookie userAgentState = objectMapper.readValue(decoded, UserAgentStateCookie.class);
            return Optional.of(userAgentState);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            // Log exception
            return Optional.empty();
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

package de.adorsys.oauth2.pkce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.util.Base64Encoder;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Component
public class UserAgentStateService {

    private final Logger logger = LoggerFactory.getLogger(UserAgentStateService.class);

    private final Base64Encoder base64Encoder = new Base64Encoder();
    private final ObjectMapper objectMapper;
    private final CookieService cookieService;
    private final PkceProperties pkceProperties;

    public UserAgentStateService(
            ObjectMapper objectMapper,
            CookieService cookieService,
            PkceProperties pkceProperties
    ) {
        this.objectMapper = objectMapper;
        this.cookieService = cookieService;
        this.pkceProperties = pkceProperties;
    }

    public Optional<UserAgentState> readUserAgentStateCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, TokenConstants.USER_AGENT_STATE_COOKIE_NAME);

        if (cookie == null || StringUtils.isBlank(cookie.getValue()))
            return Optional.empty();

        return tryToReadUserAgentState(cookie.getValue());
    }

    public Optional<UserAgentState> tryToReadUserAgentState(String value) {
        byte[] decoded = Base64.getDecoder().decode(value);

        try {
            UserAgentState userAgentState = objectMapper.readValue(decoded, UserAgentState.class);
            return Optional.of(userAgentState);
        } catch (IOException e) {
            if(logger.isDebugEnabled()) logger.debug(e.getMessage());
            return Optional.empty();
        }
    }

    public UserAgentState readUserAgentState(String value) {
        byte[] decoded = Base64.getDecoder().decode(value);

        try {
            return objectMapper.readValue(decoded, UserAgentState.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Cookie createRedirectCookie(String origLocation, String redirectUri) throws JsonProcessingException {
        UserAgentState userAgentState = new UserAgentState(origLocation, redirectUri);

        byte[] userAgentStateAsBytes = objectMapper.writeValueAsBytes(userAgentState);
        String cookieValue = base64Encoder.toBase64(userAgentStateAsBytes);

        return cookieService.creationCookieWithDefaultDuration(
                TokenConstants.USER_AGENT_STATE_COOKIE_NAME,
                cookieValue,
                pkceProperties.getTokenEndpoint()
        );
    }

    public Cookie deleteUserAgentStateCookie() {
        return cookieService.deletionCookie(TokenConstants.USER_AGENT_STATE_COOKIE_NAME, pkceProperties.getTokenEndpoint());
    }

    public static class UserAgentState {
        // URI used to redirect controll to client after tocken obtained.
        private String redirectUri;
        // location to redirect to.
        private String userAgentPage;

        public UserAgentState() {
        }

        public UserAgentState(String userAgentPage, String redirectUri) {
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

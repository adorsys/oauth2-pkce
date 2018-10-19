package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.exception.ExceptionFormatter;
import de.adorsys.oauth2.pkce.exception.UnauthorizedException;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class PkceTokenRequestService {

    private final Logger logger = LoggerFactory.getLogger(PkceTokenRequestService.class);

    private static final Base64.Encoder BASE_64 = Base64.getEncoder();

    private final RestTemplate restTemplate;
    private final PkceProperties pkceProperties;

    public PkceTokenRequestService(
            RestTemplate restTemplate,
            PkceProperties pkceProperties
    ) {
        this.restTemplate = restTemplate;
        this.pkceProperties = pkceProperties;
    }

    public TokenResponse requestToken(String code, String codeVerifier, String redirectUri) {
        if(logger.isTraceEnabled()) logger.trace("Request token start...");

        HttpHeaders headers = new HttpHeaders();
        headers.add(TokenConstants.AUTHORIZATION_HEADER_NAME, "Basic " + buildAuthorizationHeader());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body= new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add(TokenConstants.REDIRECT_URI_PARAM_NAME, redirectUri);
        body.add("code", code);
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> exchange = restTemplate.exchange(
                pkceProperties.getAccessTokenUri(),
                HttpMethod.POST,
                request,
                TokenResponse.class
        );

        if(logger.isTraceEnabled()) logger.trace("Request token finished.");

        return exchange.getBody();
    }
    
    public TokenResponse refreshAccessToken(String refreshToken) {
        if(logger.isTraceEnabled()) logger.trace("Refresh access-token for refresh-token start...");

        HttpHeaders headers = new HttpHeaders();
        headers.add(TokenConstants.AUTHORIZATION_HEADER_NAME, "Basic " + buildAuthorizationHeader());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> exchange;
        try {
            exchange = restTemplate.exchange(
                    pkceProperties.getAccessTokenUri(),
                    HttpMethod.POST,
                    request,
                    TokenResponse.class
            );

        } catch(Exception e) {
            UUID uuid = UUID.randomUUID();
            String message = ExceptionFormatter.format(uuid.toString(), e);

            if(logger.isDebugEnabled()) logger.debug("Cannot refresh access-token. message: {}", message, e);

            throw new UnauthorizedException(message, e);
        }

        if(logger.isTraceEnabled()) logger.trace("Refresh access-token for refresh-token finished.");

        return exchange.getBody();
    }
    
    public UserInfo userInfo(String accessToken) {
        if(logger.isTraceEnabled()) logger.trace("Get user info for access-token start...");

        HttpHeaders headers = new HttpHeaders();
        headers.add(TokenConstants.AUTHORIZATION_HEADER_NAME, TokenConstants.AUTHORIZATION_HEADER_TOKEN_PREFIX + accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);

        ResponseEntity<UserInfo> exchange;
        try {
            exchange = restTemplate.exchange(
                    pkceProperties.getUserInfoUri(),
                    HttpMethod.GET, request,
                    UserInfo.class
            );
        } catch(Exception e) {
            UUID uuid = UUID.randomUUID();
            String message = ExceptionFormatter.format(uuid.toString(), e);

            if(logger.isDebugEnabled()) logger.debug("Cannot get user-info. message: {}", message, e);

            throw new UnauthorizedException(message, e);
        }

        if(logger.isTraceEnabled()) logger.trace("Get user info for access-token finished.");

        return exchange.getBody();
    }

    private String buildAuthorizationHeader() {
        String clientId = pkceProperties.getClientId();
        String clientSecret = pkceProperties.getClientSecret();

        String userPassword = clientId + ":" + clientSecret;

        return new String(BASE_64.encode(userPassword.getBytes()));
    }

    public static class TokenResponse {
        private String refresh_token;
        private Long refresh_token_expires_in;
        private String id_token;
        private String access_token;
        private String token_type;
        private Long expires_in;
        private Long refresh_expires_in;

        public String getRefresh_token() {
            return refresh_token;
        }

        public Long getRefresh_token_expires_in() {
            return refresh_token_expires_in;
        }

        public Long getRefresh_expires_in() {
			return refresh_expires_in;
		}

        public String getId_token() {
            return id_token;
        }

        public String getAccess_token() {
            return access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public Long getExpires_in() {
            return expires_in;
        }

        public boolean isExpired() {
        	return isExpiredInternal(expires_in);
        }
        
        public boolean isRefreshTokenExpired() {
        	return isExpiredInternal(refresh_expires_in) && isExpiredInternal(refresh_token_expires_in);
        }
        
        public Long anyRefreshTokenExpireIn() {
        	if(refresh_expires_in != null) return refresh_expires_in;
        	return refresh_token_expires_in;
        }
        
        private static boolean isExpiredInternal(Long expireIn) {
        	if(expireIn == null) return true;
            Date expiration = Date.from(Instant.ofEpochMilli(expireIn));
            return expiration.before(new Date());
        }
        

		@Override
        public String toString() {
            return "TokenResponse{" +
                    "refresh_token='" + refresh_token + '\'' +
                    ", refresh_token_expires_in=" + refresh_token_expires_in +
                    ", id_token='" + id_token + '\'' +
                    ", access_token='" + access_token + '\'' +
                    ", token_type='" + token_type + '\'' +
                    ", expires_in=" + expires_in +
                    '}';
        }
    }
}

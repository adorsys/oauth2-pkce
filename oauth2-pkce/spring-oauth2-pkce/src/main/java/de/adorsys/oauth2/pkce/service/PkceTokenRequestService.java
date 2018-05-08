package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.context.Oauth2PkceContext;
import de.adorsys.oauth2.pkce.PkceProperties;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Date;

public class PkceTokenRequestService {

    private final RestTemplate restTemplate;
    private final Oauth2PkceContext oauth2PkceContext;
    private final PkceProperties pkceProperties;

    public PkceTokenRequestService(
            RestTemplate restTemplate,
            Oauth2PkceContext oauth2PkceContext,
            PkceProperties pkceProperties
    ) {
        this.restTemplate = restTemplate;
        this.oauth2PkceContext = oauth2PkceContext;
        this.pkceProperties = pkceProperties;
    }

    public TokenResponse requestToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", pkceProperties.getAuthorizationHeader());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body= new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", pkceProperties.getRedirectUri());
        body.add("code", code);
        body.add("code_verifier", oauth2PkceContext.getCodeVerifier().getValue());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> exchange = restTemplate.exchange(
                pkceProperties.getAccessTokenUri(),
                HttpMethod.POST,
                request,
                TokenResponse.class
        );

        return exchange.getBody();
    }

    public static class TokenResponse {
        private String refresh_token;
        private Long refresh_token_expires_in;
        private String id_token;
        private String access_token;
        private String token_type;
        private Long expires_in;

        public String getRefresh_token() {
            return refresh_token;
        }

        public Long getRefresh_token_expires_in() {
            return refresh_token_expires_in;
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
            Date expiration = Date.from(Instant.ofEpochMilli(expires_in));
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

package de.adorsys.oauth2.pkce.mapping;

import java.io.IOException;
import java.util.Base64;

import org.adorsys.encobject.userdata.ObjectMapperSPI;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;

public class BearerTokenMapper {

    private final ObjectMapperSPI objectMapper;

    public BearerTokenMapper(ObjectMapperSPI objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public OAuth2AccessToken mapFromBase64(String tokenAsString) {
        String json = fromBase64(tokenAsString);
        return mapFromJson(json);
    }

    public OAuth2AccessToken mapFromJson(String json) {
        PkceTokenRequestService.TokenResponse tokenResponse;
        try {
            tokenResponse = objectMapper.readValue(json, PkceTokenRequestService.TokenResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new BearerToken(
                tokenResponse.getAccess_token(),
                tokenResponse.getExpires_in().intValue()
        );
    }

    private String fromBase64(String base64) {
        byte[] decodeBytes = Base64.getDecoder().decode(base64);
        return new String(decodeBytes);
    }

    private static final class BearerToken extends DefaultOAuth2AccessToken {

        private BearerToken(String value, int expireIn) {
            super(value);
            setExpiresIn(expireIn);
        }
    }
}

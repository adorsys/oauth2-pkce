package de.adorsys.oauth2.pkce.mapping;

import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import org.adorsys.encobject.userdata.ObjectMapperSPI;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class BearerTokenMapper {

    private final ObjectMapperSPI objectMapper;

    public BearerTokenMapper(ObjectMapperSPI objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String mapToBase64(PkceTokenRequestService.TokenResponse tokenResponse) {
        Map<String, Object> values = new HashMap<>();
        values.put("access_token", tokenResponse.getAccess_token());
        values.put("token_type", tokenResponse.getToken_type());
        values.put("expires_in", tokenResponse.getExpires_in());

        String valuesAsJson;
        try {
            valuesAsJson = objectMapper.writeValueAsString(values);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return toBase64(valuesAsJson);
    }

    private static String toBase64(String value) {
        byte[] valueAsBytes = value.getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(valueAsBytes);

        return new String(encodedBytes);
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

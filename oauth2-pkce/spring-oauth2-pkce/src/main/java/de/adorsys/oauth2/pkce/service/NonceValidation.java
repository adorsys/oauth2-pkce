package de.adorsys.oauth2.pkce.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.adorsys.oauth2.pkce.exception.UnauthorizedException;
import de.adorsys.oauth2.pkce.model.Nonce;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;

@Component
public class NonceValidation {

    public boolean hasNonce(final String accessToken, final Nonce expectedNonce) {
        Optional<Nonce> maybeNonce = tryToReadNonce(accessToken);

        return maybeNonce
                .map(n -> Objects.equals(n, expectedNonce))
                .orElse(false);
    }

    private Optional<Nonce> tryToReadNonce(String token) {
        JWTClaimsSet claims = parseClaims(token);
        Object rawNonce = claims.getClaim("nonce");

        String nonceAsString;
        if(rawNonce instanceof String) {
            nonceAsString = (String) rawNonce;
        } else {
            return Optional.empty();
        }

        return Optional.of(new Nonce(nonceAsString));
    }

    private JWTClaimsSet parseClaims(String token) {
        SignedJWT signedJWT = parseJwt(token);

        JWTClaimsSet claims;
        try {
            claims = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new UnauthorizedException(e);
        }

        return claims;
    }

    private SignedJWT parseJwt(String token) {
        SignedJWT signedJWT;

        try {
            signedJWT = SignedJWT.parse(token);
        } catch (java.text.ParseException e) {
            throw new UnauthorizedException(e);
        }

        return signedJWT;
    }
}

package de.adorsys.oauth2.pkce.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class RefererService {

    public String buildRedirectUri(String referer, String targetPath) {
        if(isInvalidTargetPath(targetPath)) {
            throw new BadTargetPathException("Illegal target_path");
        }

        return UriComponentsBuilder.fromUriString(referer)
                .replacePath(targetPath)
                .build()
                .toString();
    }

    private boolean isInvalidTargetPath(String targetPath) {
        UriComponents targetPathAsUriComponents = UriComponentsBuilder.fromUriString(targetPath).build();
        URI targetPathAsUri;

        try {
            targetPathAsUri = targetPathAsUriComponents.toUri();
        } catch(IllegalStateException e) {
            return true;
        }

        return Strings.isNotBlank(targetPathAsUri.getQuery()) || targetPathAsUri.isAbsolute();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class BadTargetPathException extends RuntimeException {
        public BadTargetPathException(String message) {
            super(message);
        }
    }
}

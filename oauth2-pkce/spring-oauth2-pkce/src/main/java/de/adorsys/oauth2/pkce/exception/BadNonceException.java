package de.adorsys.oauth2.pkce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadNonceException extends RuntimeException {

    public BadNonceException(String message) {
        super(message);
    }
}

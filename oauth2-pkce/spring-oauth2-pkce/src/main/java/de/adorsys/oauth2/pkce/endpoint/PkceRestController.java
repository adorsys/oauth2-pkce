package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.basetypes.CodeVerifier;
import de.adorsys.oauth2.pkce.mapping.BearerTokenMapper;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController("/oauth/pkce")
public class PkceRestController {

    private static final String CODE_VERIFIER_COOKIE_NAME = "code_verifier";
    private final PkceTokenRequestService pkceTokenRequestService;
    private final LoginRedirectService loginRedirectService;
    private final BearerTokenMapper mapper;
    private final PkceProperties pkceProperties;

    @Autowired
    public PkceRestController(
            PkceTokenRequestService pkceTokenRequestService,
            LoginRedirectService loginRedirectService,
            BearerTokenMapper mapper,
            PkceProperties pkceProperties
    ) {
        this.pkceTokenRequestService = pkceTokenRequestService;
        this.loginRedirectService = loginRedirectService;
        this.mapper = mapper;
        this.pkceProperties = pkceProperties;
    }

    @GetMapping
    public void redirectToLoginPage(HttpServletResponse response) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect();

        Cookie code_verifier = createCodeVerifierCookie(redirect.getCodeVerifier());

        response.addCookie(code_verifier);
        response.sendRedirect(redirect.getRedirectUrl());
    }

    @PostMapping
    public void getToken(
            @RequestBody @Valid TokenRequest tokenRequest,
            @CookieValue(CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            HttpServletResponse response
    ) {
        PkceTokenRequestService.TokenResponse bearerToken = pkceTokenRequestService.requestToken(
                tokenRequest.getCode(),
                codeVerifier
        );

        Cookie cookie = createBearerTokenCookie(bearerToken);

        response.addCookie(cookie);
        response.addCookie(createCodeVerifierDeletionCookie());
    }

    private Cookie createBearerTokenCookie(PkceTokenRequestService.TokenResponse token) {
        Cookie cookie = new Cookie(pkceProperties.getCookieName(), mapper.mapToBase64(token));

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(token.getExpires_in().intValue());

        return cookie;
    }

    private Cookie createCodeVerifierDeletionCookie() {
        Cookie cookie = new Cookie(CODE_VERIFIER_COOKIE_NAME, null);

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/oauth/pkce");
        cookie.setMaxAge(0);

        return cookie;
    }

    private Cookie createCodeVerifierCookie(CodeVerifier codeVerifier) {
        Cookie cookie = new Cookie(CODE_VERIFIER_COOKIE_NAME, codeVerifier.getValue());

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/oauth/pkce");
        cookie.setMaxAge(3600);

        return cookie;
    }

    public static class TokenRequest {
        @NotNull
        @NotEmpty
        private String code;

        public String getCode() {
            return code;
        }
    }
}

package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.basetypes.CodeVerifier;
import de.adorsys.oauth2.pkce.mapping.BearerTokenMapper;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController("/oauth/pkce")
public class PkceRestController {

    private static final String CODE_VERIFIER_COOKIE_NAME = "code_verifier";
    private static final String REDIRECT_URI_COOKIE_NAME = "redirect_uri";

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

    @GetMapping(params = "redirect_uri")
    public void redirectToLoginPage(
            @RequestParam("redirect_uri") String redirectUri,
            HttpServletResponse response
    ) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect(redirectUri);

        Cookie codeVerifier = createCodeVerifierCookie(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        Cookie redirectUriCookie = createRedirectUriCookie(redirectUri);
        response.addCookie(redirectUriCookie);

        response.sendRedirect(redirect.getRedirectUrl());
    }

    @GetMapping(params = "code")
    public void getToken(
            @RequestParam("code") String code,
            @CookieValue(CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            @CookieValue(REDIRECT_URI_COOKIE_NAME) String redirectUri,
            HttpServletResponse response
    ) {
        PkceTokenRequestService.TokenResponse bearerToken = pkceTokenRequestService.requestToken(
                code,
                codeVerifier,
                redirectUri
        );

        Cookie cookie = createBearerTokenCookie(bearerToken);
        response.addCookie(cookie);

        response.addCookie(createDeletionCookie(CODE_VERIFIER_COOKIE_NAME));
        response.addCookie(createDeletionCookie(REDIRECT_URI_COOKIE_NAME));
    }

    private Cookie createBearerTokenCookie(PkceTokenRequestService.TokenResponse token) {
        Cookie cookie = new Cookie(pkceProperties.getCookieName(), mapper.mapToBase64(token));

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(token.getExpires_in().intValue());

        return cookie;
    }

    private Cookie createDeletionCookie(String name) {
        Cookie cookie = new Cookie(name, null);

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

    private Cookie createRedirectUriCookie(String redirectUri) {
        Cookie cookie = new Cookie(REDIRECT_URI_COOKIE_NAME, redirectUri);

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/oauth/pkce");
        cookie.setMaxAge(3600);

        return cookie;
    }
}

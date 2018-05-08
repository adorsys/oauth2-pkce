package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.mapping.BearerTokenMapper;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController("/oauth/pkce")
public class PkceRestController {

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
        String redirectUrl = loginRedirectService.getRedirectUrl();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping
    public void getToken(
            @RequestBody TokenRequest tokenRequest,
            HttpServletResponse response
    ) {
        PkceTokenRequestService.TokenResponse bearerToken = pkceTokenRequestService.requestToken(tokenRequest.getCode());
        Cookie cookie = createBearerTokenCookie(bearerToken);

        response.addCookie(cookie);
    }

    private Cookie createBearerTokenCookie(PkceTokenRequestService.TokenResponse token) {
        Cookie cookie = new Cookie(pkceProperties.getCookieName(), mapper.mapToBase64(token));

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(token.getExpires_in().intValue());

        return cookie;
    }

    private static class TokenRequest {
        private String code;

        public String getCode() {
            return code;
        }
    }
}

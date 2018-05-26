package de.adorsys.oauth2.pkce.endpoint;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.basetypes.CodeVerifier;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.util.TokenConstants;

@RestController("Oauth Endpoint")
@RequestMapping(path=PkceRestController.OAUTH_AUTHENTICATION_ENDPOINT)
public class PkceRestController {

    public static final String OAUTH_AUTHENTICATION_ENDPOINT = "/oauth/pkce";
    private static final String CODE_REQUEST_PARAMETER_NAME = "code";
    private static final String CODE_VERIFIER_COOKIE_NAME = "code_verifier";
    private static final String REDIRECT_URI_REQUEST_PARAMETER_NAME = "redirect_uri";

    private final PkceTokenRequestService pkceTokenRequestService;
    private final LoginRedirectService loginRedirectService;
    private final PkceProperties pkceProperties;

    @Autowired
    public PkceRestController(
            PkceTokenRequestService pkceTokenRequestService,
            LoginRedirectService loginRedirectService,
            PkceProperties pkceProperties
    ) {
        this.pkceTokenRequestService = pkceTokenRequestService;
        this.loginRedirectService = loginRedirectService;
        this.pkceProperties = pkceProperties;
    }

    @GetMapping(params = REDIRECT_URI_REQUEST_PARAMETER_NAME)
    public void redirectToLoginPage(
            @RequestParam(REDIRECT_URI_REQUEST_PARAMETER_NAME) String redirectUri,
            HttpServletResponse response
    ) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect(redirectUri);

        Cookie codeVerifier = createCodeVerifierCookie(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        response.sendRedirect(redirect.getRedirectUrl());
    }

    @GetMapping(params = {CODE_REQUEST_PARAMETER_NAME})
    public void getTokenFromCode(
            HttpServletRequest request,
            @RequestParam(CODE_REQUEST_PARAMETER_NAME) String code,
            @CookieValue(CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            HttpServletResponse response
    ) throws IOException {
        String redirectUri = null;
        Object redirectUriAttribute = request.getAttribute(REDIRECT_URI_REQUEST_PARAMETER_NAME);
        if(redirectUriAttribute!=null){
            redirectUri = redirectUriAttribute.toString();
        }
        Assert.notNull(redirectUri, "Missing redirect URI");
        getToken(request, code, codeVerifier, redirectUri, response);
    }
    
    @GetMapping(params = {CODE_REQUEST_PARAMETER_NAME, REDIRECT_URI_REQUEST_PARAMETER_NAME})
    public void getToken(HttpServletRequest request,
            @RequestParam(CODE_REQUEST_PARAMETER_NAME) String code,
            @CookieValue(CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            @RequestParam(name = REDIRECT_URI_REQUEST_PARAMETER_NAME, required=false) String redirectUri,
            HttpServletResponse response
    ) throws IOException {        
        PkceTokenRequestService.TokenResponse bearerToken = pkceTokenRequestService.requestToken(
                code,
                codeVerifier,
                redirectUri
        );

        response.addCookie(createCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, bearerToken.getAccess_token(), bearerToken.getExpires_in()));
        response.addCookie(createCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, bearerToken.getRefresh_token(), bearerToken.anyRefreshTokenExpireIn()));
        
        response.addCookie(createDeletionCookie(CODE_VERIFIER_COOKIE_NAME));
        
        Object clientDisplayPage = request.getAttribute(TokenConstants.CLIENT_DISPLAY_PAGE);
        if(clientDisplayPage!=null){
            response.sendRedirect(clientDisplayPage.toString());
        }
    }

    private Cookie createCookie(String name, String token, Long expiration) {
    	Cookie cookie = new Cookie(name, token);

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(expiration.intValue());
        return cookie;
    }

    private Cookie createDeletionCookie(String name) {
        Cookie cookie = new Cookie(name, null);

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath(OAUTH_AUTHENTICATION_ENDPOINT);
        cookie.setMaxAge(0);

        return cookie;
    }

    private Cookie createCodeVerifierCookie(CodeVerifier codeVerifier) {
        Cookie cookie = new Cookie(CODE_VERIFIER_COOKIE_NAME, codeVerifier.getValue());

        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath(OAUTH_AUTHENTICATION_ENDPOINT);
        cookie.setMaxAge(3600);

        return cookie;
    }
}

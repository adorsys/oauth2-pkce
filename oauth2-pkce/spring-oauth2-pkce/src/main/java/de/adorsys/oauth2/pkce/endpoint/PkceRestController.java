package de.adorsys.oauth2.pkce.endpoint;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.basetypes.CodeVerifier;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.util.TokenConstants;

@RestController("Oauth Endpoint")
//@RequestMapping set with de.adorsys.oauth2.pkce.WebConfig
public class PkceRestController {
	
	private static final Logger LOG = LoggerFactory.getLogger(PkceRestController.class);

    private final PkceTokenRequestService pkceTokenRequestService;
    private final LoginRedirectService loginRedirectService;
    private final PkceProperties pkceProperties;
    private final CookieService cookieService;

    @Autowired
    public PkceRestController(
            PkceTokenRequestService pkceTokenRequestService,
            LoginRedirectService loginRedirectService,
            PkceProperties pkceProperties,
            CookieService cookieService
    ) {
        this.pkceTokenRequestService = pkceTokenRequestService;
        this.loginRedirectService = loginRedirectService;
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
    }

    @GetMapping(params = TokenConstants.REDIRECT_URI_PARAM_NAME)
    public void redirectToLoginPage(
            @RequestParam(TokenConstants.REDIRECT_URI_PARAM_NAME) String redirectUri,
            HttpServletResponse response
    ) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect(redirectUri);

        Cookie codeVerifier = createCodeVerifierCookie(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        response.sendRedirect(redirect.getRedirectUrl());
    }

    @GetMapping(params = {TokenConstants.CODE_REQUEST_PARAMETER_NAME})
    public void getTokenFromCode(
            HttpServletRequest request,
            @RequestParam(TokenConstants.CODE_REQUEST_PARAMETER_NAME) String code,
            HttpServletResponse response
    ) throws IOException {
        String redirectUri = null;
        Object redirectUriAttribute = request.getAttribute(TokenConstants.REDIRECT_URI_PARAM_NAME);
        if(redirectUriAttribute!=null){
            redirectUri = redirectUriAttribute.toString();
        }
        Assert.notNull(redirectUri, "Missing redirect URI");
        getToken(request, code, redirectUri, response);
    }
    
    @GetMapping(params = {TokenConstants.CODE_REQUEST_PARAMETER_NAME, TokenConstants.REDIRECT_URI_PARAM_NAME})
    public void getToken(HttpServletRequest request,
            @RequestParam(TokenConstants.CODE_REQUEST_PARAMETER_NAME) String code,
            @RequestParam(name = TokenConstants.REDIRECT_URI_PARAM_NAME, required=false) String redirectUri,
            HttpServletResponse response
    ) throws IOException {   
        Cookie cookie = WebUtils.getCookie(request, pkceProperties.getCodeVerifierCookieName());
        Assert.notNull(cookie, "Missing cookie with name: " + pkceProperties.getCodeVerifierCookieName());
        String codeVerifier = cookie.getValue();
        PkceTokenRequestService.TokenResponse bearerToken = pkceTokenRequestService.requestToken(
                code,
                codeVerifier,
                redirectUri
        );

        response.addCookie(createTokenCookie(pkceProperties.getAccessTokenCookieName(), bearerToken.getAccess_token(), bearerToken.getExpires_in()));
        response.addCookie(createTokenCookie(pkceProperties.getRefreshTokenCookieName(), bearerToken.getRefresh_token(), bearerToken.anyRefreshTokenExpireIn()));
        
        response.addCookie(deleteCodeVerifierCookie());
        
        Object clientDisplayPage = request.getAttribute(TokenConstants.USER_AGENT_PAGE_ATTRIBUTE);
        if(clientDisplayPage!=null){
            response.sendRedirect(clientDisplayPage.toString());
        }
    }
    
    // Cookie not deleted. they expire.
    private Cookie createTokenCookie(String name, String token, Long expiration) {
        return cookieService.creationCookie(name, token, "/", expiration.intValue());    	
    }

    private Cookie deleteCodeVerifierCookie() {
        return cookieService.deletionCookie(pkceProperties.getCodeVerifierCookieName(), pkceProperties.getAuthEndpoint());
    }

    private Cookie createCodeVerifierCookie(CodeVerifier codeVerifier) {
        return cookieService.creationCookieWithDefaultDuration(pkceProperties.getCodeVerifierCookieName(), codeVerifier.getValue(), pkceProperties.getAuthEndpoint());
    }
}

package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.model.CodeVerifier;
import de.adorsys.oauth2.pkce.model.Nonce;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.RefererService;
import de.adorsys.oauth2.pkce.service.UserAgentStateService;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(value = "OAUTH2 PKCE Login")
@RestController("OAUTH2 PKCE Login Controller")
@RequestMapping(path = "${pkce.auth-endpoint:/oauth2/login}")
public class PkceLoginRestController {

    private static final Logger LOG = LoggerFactory.getLogger(PkceLoginRestController.class);

    private final LoginRedirectService loginRedirectService;
    private final PkceProperties pkceProperties;
    private final CookieService cookieService;
    private final UserAgentStateService userAgentStateService;
    private final RefererService refererService;

    @Autowired
    public PkceLoginRestController(
            LoginRedirectService loginRedirectService,
            PkceProperties pkceProperties,
            CookieService cookieService,
            UserAgentStateService userAgentStateService,
            RefererService refererService
    ) {
        this.loginRedirectService = loginRedirectService;
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
        this.userAgentStateService = userAgentStateService;
        this.refererService = refererService;
    }

    // @formatter:off
    @ApiOperation(value = "Login with provided redirect-uri", code = 302)
    @ApiResponses(value = {
        @ApiResponse(
            code = HttpServletResponse.SC_FOUND,
            message = "Redirect to IDP login page",
            responseHeaders = {
                @ResponseHeader(
                    name = "Location",
                    response = String.class,
                    description = "Url to login page"
                ), @ResponseHeader(
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.CODE_VERIFIER_COOKIE_NAME + "=<code-verifier value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    // why i am using \0's?
                    // look here: https://github.com/OAI/OpenAPI-Specification/issues/1237#issuecomment-423955715
                    name = "\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.NONCE_COOKIE_NAME + "=<nonce value>; Path=/; Secure; HttpOnly; Max-Age=<nonce's max-age value>"
                )
            }
        )
    })
    // @formatter:on
    @GetMapping(params = TokenConstants.REDIRECT_URI_PARAM_NAME)
    public void redirectToLoginPageWithRedirectUrl(
            @RequestParam(TokenConstants.REDIRECT_URI_PARAM_NAME) String originLocation,
            HttpServletResponse response
    ) throws IOException {
        redirectToOriginLocation(originLocation, response);
    }

    // @formatter:off
    @ApiOperation(value = "Login without provided redirect-uri", code = 302)
    @ApiResponses(value = {
        @ApiResponse(
            code = HttpServletResponse.SC_FOUND,
            message = "Redirect to IDP login page",
            responseHeaders = {
                @ResponseHeader(
                    name = "location",
                    response = String.class,
                    description = "Url to login page"
                ), @ResponseHeader(
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.USER_AGENT_STATE_COOKIE_NAME + "=<user-agent-state value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    name = "\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.CODE_VERIFIER_COOKIE_NAME + "=<code-verifier value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    name = "\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.NONCE_COOKIE_NAME + "=<nonce value>; Path=/; Secure; HttpOnly; Max-Age=<nonce's max-age value>"
                )
            }
        )
    })
    // @formatter:on
    @GetMapping
    public void redirectToLoginPageWithReferer(
            HttpServletRequest request,
            @RequestHeader(TokenConstants.REFERER_HEADER_KEYWORD) String referer,
            HttpServletResponse response
    ) throws IOException {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequestUri(request);
        String redirectUri = builder.replacePath(pkceProperties.getTokenEndpoint()).build().toUriString();

        redirectToLogin(referer, redirectUri, response);
    }

    // @formatter:off
    @ApiOperation(value = "Login with provided target-path", code = 302)
    @ApiResponses(value = {
        @ApiResponse(
            code = HttpServletResponse.SC_FOUND,
            message = "Redirect to IDP login page",
            responseHeaders = {
                @ResponseHeader(
                    name = "location",
                    response = String.class,
                    description = "Url to login page"
                ), @ResponseHeader(
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.USER_AGENT_STATE_COOKIE_NAME + "=<user-agent-state value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    name = "\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.CODE_VERIFIER_COOKIE_NAME + "=<code-verifier value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    name = "\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.NONCE_COOKIE_NAME + "=<nonce value>; Path=/; Secure; HttpOnly; Max-Age=<nonce's max-age value>"
                )
            }
        )
    })
    // @formatter:on
    @GetMapping(params = TokenConstants.TARGET_PATH_PARAM_NAME)
    public void redirectToLoginPageWithTarget(
            HttpServletRequest request,
            @RequestParam(TokenConstants.TARGET_PATH_PARAM_NAME) String targetPath,
            @RequestHeader(TokenConstants.REFERER_HEADER_KEYWORD) String referer,
            HttpServletResponse response
    ) throws IOException {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequestUri(request);
        String redirectUri = builder.replacePath(pkceProperties.getTokenEndpoint()).build().toUriString();
        String refererUri = refererService.buildRedirectUri(referer, targetPath);

        redirectToLogin(refererUri, redirectUri, response);
    }

    private void redirectToLogin(String originLocation, String redirectUri, HttpServletResponse response) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect(redirectUri);

        Cookie codeVerifier = createCodeVerifierCookie(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        Cookie userAgentStateCookie = userAgentStateService.createRedirectCookie(originLocation, redirectUri);
        response.addCookie(userAgentStateCookie);

        Cookie nonceCookie = createNonceCookie(redirect.getGeneratedNonce());
        response.addCookie(nonceCookie);

        response.sendRedirect(redirect.getRedirectUrl());
    }

    private void redirectToOriginLocation(String originLocation, HttpServletResponse response) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect(originLocation);

        Cookie codeVerifier = createCodeVerifierCookie(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        Cookie nonce = createNonceCookie(redirect.getGeneratedNonce());
        response.addCookie(nonce);

        response.sendRedirect(redirect.getRedirectUrl());
    }

    private Cookie createCodeVerifierCookie(CodeVerifier codeVerifier) {
        return cookieService.creationCookieWithDefaultDuration(TokenConstants.CODE_VERIFIER_COOKIE_NAME, codeVerifier.getValue(), pkceProperties.getTokenEndpoint());
    }

    private Cookie createNonceCookie(Nonce nonce) {
        return cookieService.creationCookieWithDefaultDuration(TokenConstants.NONCE_COOKIE_NAME, nonce.getValue(), pkceProperties.getTokenEndpoint());
    }
}

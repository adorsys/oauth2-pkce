package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.model.CodeVerifier;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.LoginRedirectService;
import de.adorsys.oauth2.pkce.service.UserAgentStateService;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

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

    // only for deprecated endpoints (will be removed in future releases)
    private final PkceTokenRestController pkceTokenRestController;

    @Autowired
    public PkceLoginRestController(
            LoginRedirectService loginRedirectService,
            PkceProperties pkceProperties,
            CookieService cookieService,
            UserAgentStateService userAgentStateService,
            PkceTokenRestController pkceTokenRestController
    ) {
        this.loginRedirectService = loginRedirectService;
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
        this.userAgentStateService = userAgentStateService;
        this.pkceTokenRestController = pkceTokenRestController;
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
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.CODE_VERIFIER_COOKIE_NAME + "=<code-verifier value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
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
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.CODE_VERIFIER_COOKIE_NAME + "=<code-verifier value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
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
        String refererUri = buildRefererUri(targetPath, referer);

        redirectToLogin(refererUri, redirectUri, response);
    }

    private String buildRefererUri(String targetPath, String referer) {
        return UriComponentsBuilder.fromUriString(referer)
                .replacePath(targetPath)
                .build()
                .toString();
    }

    private void redirectToLogin(String originLocation, String redirectUri, HttpServletResponse response) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect(redirectUri);

        Cookie codeVerifier = createCodeVerifierCookie(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        Cookie userAgentStateCookie = userAgentStateService.createRedirectCookie(originLocation, redirectUri);
        response.addCookie(userAgentStateCookie);

        response.sendRedirect(redirect.getRedirectUrl());
    }

    private void redirectToOriginLocation(String originLocation, HttpServletResponse response) throws IOException {
        LoginRedirectService.LoginRedirect redirect = loginRedirectService.getRedirect(originLocation);

        Cookie codeVerifier = createCodeVerifierCookie(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        codeVerifier = createCodeVerifierCookieForDeprecatedEndpoint(redirect.getCodeVerifier());
        response.addCookie(codeVerifier);

        response.sendRedirect(redirect.getRedirectUrl());
    }

    private Cookie createCodeVerifierCookie(CodeVerifier codeVerifier) {
        return cookieService.creationCookieWithDefaultDuration(TokenConstants.CODE_VERIFIER_COOKIE_NAME, codeVerifier.getValue(), pkceProperties.getTokenEndpoint());
    }

    private Cookie createCodeVerifierCookieForDeprecatedEndpoint(CodeVerifier codeVerifier) {
        return cookieService.creationCookieWithDefaultDuration(TokenConstants.CODE_VERIFIER_COOKIE_NAME, codeVerifier.getValue(), pkceProperties.getAuthEndpoint());
    }

    /**
     * Deprecated: please use token endpoint instead
     */
    @Deprecated
    @GetMapping(params = {TokenConstants.CODE_REQUEST_PARAMETER_NAME})
    public void getTokenFromCode(
            @RequestParam(TokenConstants.CODE_REQUEST_PARAMETER_NAME) String code,
            @CookieValue(name = TokenConstants.CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            @CookieValue(name = TokenConstants.USER_AGENT_STATE_COOKIE_NAME) String userAgentStateValue,
            HttpServletResponse response
    ) throws IOException {
        pkceTokenRestController.getTokenFromCode(code, codeVerifier, userAgentStateValue, response);
    }

    /**
     * Deprecated: please use token endpoint instead
     */
    @Deprecated
    @GetMapping(params = {TokenConstants.CODE_REQUEST_PARAMETER_NAME, TokenConstants.REDIRECT_URI_PARAM_NAME})
    public void getTokenFromCodeWithRedirect(
            @RequestParam(TokenConstants.CODE_REQUEST_PARAMETER_NAME) String code,
            @RequestParam(name = TokenConstants.REDIRECT_URI_PARAM_NAME) String redirectUri,
            @CookieValue(name = TokenConstants.CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            HttpServletResponse response
    ) throws IOException {
        pkceTokenRestController.getTokenFromCodeWithRedirect(code, redirectUri, codeVerifier, response);
    }
}

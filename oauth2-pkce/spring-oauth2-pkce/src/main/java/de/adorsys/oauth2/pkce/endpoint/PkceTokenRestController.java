package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.service.UserAgentStateService;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(value = "OAUTH2 PKCE Token")
@RestController("OAUTH2 PKCE Token Controller")
@RequestMapping(path = "${pkce.token-endpoint:/oauth2/token}")
public class PkceTokenRestController {

    private final PkceProperties pkceProperties;
    private final CookieService cookieService;
    private final PkceTokenRequestService pkceTokenRequestService;
    private final UserAgentStateService userAgentStateService;

    public PkceTokenRestController(
            PkceProperties pkceProperties,
            CookieService cookieService,
            PkceTokenRequestService pkceTokenRequestService,
            UserAgentStateService userAgentStateService
    ) {
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
        this.pkceTokenRequestService = pkceTokenRequestService;
        this.userAgentStateService = userAgentStateService;
    }

    // @formatter:off
    @ApiOperation(value = "Get token for code without provided redirect-uri", code = 302)
    @ApiResponses(value = {
        @ApiResponse(
            code = HttpServletResponse.SC_FOUND,
            message = "Redirect back to user agent",
            responseHeaders = {
                @ResponseHeader(
                    name = "Location",
                    response = String.class,
                    description = "Url to origin/referer/redirectUri"
                ), @ResponseHeader(
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.ACCESS_TOKEN_COOKIE_NAME + "=<access-token-value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    // why i am using \0's?
                    // look here: https://github.com/OAI/OpenAPI-Specification/issues/1237#issuecomment-423955715
                    name = "\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.REFRESH_TOKEN_COOKIE_NAME + "=<refresh-token-value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    name = "\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.CODE_VERIFIER_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                ), @ResponseHeader(
                    name = "\0\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.USER_AGENT_STATE_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                )
            }
        )
    })
    // @formatter:on
    @GetMapping(params = {TokenConstants.CODE_REQUEST_PARAMETER_NAME})
    public void getTokenFromCode(
            @RequestParam(TokenConstants.CODE_REQUEST_PARAMETER_NAME) String code,
            @CookieValue(name = TokenConstants.CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            @CookieValue(name = TokenConstants.USER_AGENT_STATE_COOKIE_NAME) String userAgentStateValue,
            HttpServletResponse response
    ) throws IOException {
        UserAgentStateService.UserAgentState userAgentState = userAgentStateService.readUserAgentState(userAgentStateValue);

        Cookie deleteUserAgentState = userAgentStateService.deleteUserAgentStateCookie();
        response.addCookie(deleteUserAgentState);

        getTokenForCode(code, userAgentState.getRedirectUri(), userAgentState.getUserAgentPage(), codeVerifier, response);
    }

    // @formatter:off
    @ApiOperation(value = "Get token for code with provided redirect-uri", code = 302)
    @ApiResponses(value = {
        @ApiResponse(
            code = HttpServletResponse.SC_FOUND,
            message = "Redirect to IDP login page",
            responseHeaders = {
                @ResponseHeader(
                    name = "Location",
                    response = String.class,
                        description = "Url to user agent"
                ), @ResponseHeader(
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.ACCESS_TOKEN_COOKIE_NAME + "=<access-token-value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    // why i am using \0's?
                    // look here: https://github.com/OAI/OpenAPI-Specification/issues/1237#issuecomment-423955715
                    name = "\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.REFRESH_TOKEN_COOKIE_NAME + "=<refresh-token-value>; Path=/; Secure; HttpOnly; Max-Age=<token's max-age value>"
                ), @ResponseHeader(
                    name = "\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.CODE_VERIFIER_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                ), @ResponseHeader(
                    name = "\0\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.USER_AGENT_STATE_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                )
            }
        )
    })
    // @formatter:on
    @GetMapping(params = {TokenConstants.CODE_REQUEST_PARAMETER_NAME, TokenConstants.REDIRECT_URI_PARAM_NAME})
    public void getTokenFromCodeWithRedirect(
            @RequestParam(TokenConstants.CODE_REQUEST_PARAMETER_NAME) String code,
            @RequestParam(name = TokenConstants.REDIRECT_URI_PARAM_NAME) String redirectUri,
            @CookieValue(name = TokenConstants.CODE_VERIFIER_COOKIE_NAME) String codeVerifier,
            HttpServletResponse response
    ) throws IOException {
        Cookie deleteUserAgentState = userAgentStateService.deleteUserAgentStateCookie();
        response.addCookie(deleteUserAgentState);

        getTokenForCode(code, redirectUri, redirectUri, codeVerifier, response);
    }

    private void getTokenForCode(String code, String redirectUri, String originUri, String codeVerifier, HttpServletResponse response) throws IOException {
        PkceTokenRequestService.TokenResponse bearerToken = pkceTokenRequestService.requestToken(
                code,
                codeVerifier,
                redirectUri
        );

        response.addCookie(createTokenCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, bearerToken.getAccess_token(), bearerToken.getExpires_in()));
        response.addCookie(createTokenCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, bearerToken.getRefresh_token(), bearerToken.anyRefreshTokenExpireIn()));

        response.addCookie(deleteCodeVerifierCookie());
        response.addCookie(deleteCodeVerifierCookieForDeprecatedEndpoint());

        response.sendRedirect(originUri);
    }


    // Cookie not deleted. they expire.
    private Cookie createTokenCookie(String name, String token, Long expiration) {
        return cookieService.creationCookie(name, token, "/", expiration.intValue());
    }

    private Cookie deleteCodeVerifierCookie() {
        return cookieService.deletionCookie(TokenConstants.CODE_VERIFIER_COOKIE_NAME, pkceProperties.getTokenEndpoint());
    }

    private Cookie deleteCodeVerifierCookieForDeprecatedEndpoint() {
        return cookieService.deletionCookie(TokenConstants.CODE_VERIFIER_COOKIE_NAME, pkceProperties.getAuthEndpoint());
    }
}

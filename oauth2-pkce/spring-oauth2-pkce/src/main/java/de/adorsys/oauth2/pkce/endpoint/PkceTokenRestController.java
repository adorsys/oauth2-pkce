package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.exception.BadNonceException;
import de.adorsys.oauth2.pkce.model.Nonce;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.NonceValidation;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.service.UserAgentStateService;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Api(value = "OAUTH2 PKCE Token")
@RestController("OAUTH2 PKCE Token Controller")
@RequestMapping(path = "${pkce.token-endpoint:/oauth2/token}")
public class PkceTokenRestController {

    private final PkceProperties pkceProperties;
    private final CookieService cookieService;
    private final PkceTokenRequestService pkceTokenRequestService;
    private final UserAgentStateService userAgentStateService;
    private final NonceValidation nonceValidation;

    public PkceTokenRestController(
            PkceProperties pkceProperties,
            CookieService cookieService,
            PkceTokenRequestService pkceTokenRequestService,
            UserAgentStateService userAgentStateService,
            NonceValidation nonceValidation
    ) {
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
        this.pkceTokenRequestService = pkceTokenRequestService;
        this.userAgentStateService = userAgentStateService;
        this.nonceValidation = nonceValidation;
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
                ), @ResponseHeader(
                    name = "\0\0\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.NONCE_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
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
            @CookieValue(name = TokenConstants.NONCE_COOKIE_NAME, required = false) String nonceValue,
            HttpServletResponse response
    ) throws IOException {
        UserAgentStateService.UserAgentState userAgentState = userAgentStateService.readUserAgentState(userAgentStateValue);
        getTokenForCode(code, userAgentState.getRedirectUri(), userAgentState.getUserAgentPage(), codeVerifier, response, nonceValue);
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
                ), @ResponseHeader(
                    name = "\0\0\0\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.NONCE_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
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
            @CookieValue(name = TokenConstants.NONCE_COOKIE_NAME, required = false) String nonceValue,
            HttpServletResponse response
    ) throws IOException {
        getTokenForCode(code, redirectUri, redirectUri, codeVerifier, response, nonceValue);
    }

    private void getTokenForCode(String code, String redirectUri, String originUri, String codeVerifier, HttpServletResponse response, String nonceValue) throws IOException {
        final PkceTokenRequestService.TokenResponse bearerToken = getTokenForCode(code, redirectUri, codeVerifier, nonceValue);

        response.addCookie(createTokenCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, bearerToken.getAccess_token(), bearerToken.getExpires_in()));
        response.addCookie(createTokenCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, bearerToken.getRefresh_token(), bearerToken.anyRefreshTokenExpireIn()));
        response.addCookie(deleteCodeVerifierCookie());
        response.addCookie(deleteNonceCookie());
        response.addCookie(userAgentStateService.deleteUserAgentStateCookie());

        response.sendRedirect(originUri);
    }

    private PkceTokenRequestService.TokenResponse getTokenForCode(String code, String redirectUri, String codeVerifier, String nonceValue) {
        final PkceTokenRequestService.TokenResponse bearerToken = pkceTokenRequestService.requestToken(
                code,
                codeVerifier,
                redirectUri
        );

        Boolean isNonceValid = Optional.ofNullable(nonceValue)
                .map(Nonce::new)
                .map(n -> nonceValidation.hasNonce(bearerToken.getAccess_token(), n))
                .orElse(true);

        if (!isNonceValid) {
            throw new BadNonceException("Nonce '" + nonceValue + "' is not valid");
        }

        return bearerToken;
    }

    // Cookie not deleted. they expire.
    private Cookie createTokenCookie(String name, String token, Long expiration) {
        return cookieService.creationCookie(name, token, "/", expiration.intValue());
    }

    private Cookie deleteCodeVerifierCookie() {
        return cookieService.deletionCookie(TokenConstants.CODE_VERIFIER_COOKIE_NAME, pkceProperties.getTokenEndpoint());
    }

    private Cookie deleteNonceCookie() {
        return cookieService.deletionCookie(TokenConstants.NONCE_COOKIE_NAME, pkceProperties.getTokenEndpoint());
    }
}

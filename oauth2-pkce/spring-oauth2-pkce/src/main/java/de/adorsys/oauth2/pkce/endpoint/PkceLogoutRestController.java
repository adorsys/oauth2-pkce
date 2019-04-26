package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.LogoutRedirectService;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(value = "OAUTH2 PKCE Logout")
@RestController("OAUTH2 PKCE Logout Controller")
@RequestMapping(path = "${pkce.logout-endpoint:/oauth2/logout}")
public class PkceLogoutRestController {

    private final CookieService cookieService;
    private final LogoutRedirectService logoutRedirectService;

    @Autowired
    public PkceLogoutRestController(
            CookieService cookieService,
            LogoutRedirectService logoutRedirectService
    ) {
        this.cookieService = cookieService;
        this.logoutRedirectService = logoutRedirectService;
    }

    // @formatter:off
    @ApiResponses(value = {
        @ApiResponse(
            code = HttpServletResponse.SC_FOUND,
            message = "Redirect to IDP logout page",
            responseHeaders = {
                @ResponseHeader(
                    name = "Location",
                    response = String.class,
                    description = "Url to IDP's logout page"
                ), @ResponseHeader(
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.ACCESS_TOKEN_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                ), @ResponseHeader(
                    // why i am using \0's?
                    // look here: https://github.com/OAI/OpenAPI-Specification/issues/1237#issuecomment-423955715
                    name = "\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.REFRESH_TOKEN_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                )
            }
        )
    })
    // @formatter:on
    @GetMapping
    public void logout(
            @RequestHeader(value = TokenConstants.REFERER_HEADER_KEYWORD, required = false) String referer,
            HttpServletResponse response
    ) throws IOException {
        response.addCookie(cookieService.deletionCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, "/"));
        response.addCookie(cookieService.deletionCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, "/"));

        LogoutRedirectService.LogoutRedirect redirect = buildLogoutRedirect(referer);
        response.sendRedirect(redirect.getRedirectUrl());
    }

    // @formatter:off
    @ApiResponses(value = {
        @ApiResponse(
            code = HttpServletResponse.SC_FOUND,
            message = "Redirect to IDP logout page",
            responseHeaders = {
                @ResponseHeader(
                    name = "Location",
                    response = String.class,
                    description = "Url to IDP's logout page"
                ), @ResponseHeader(
                    name = "Set-Cookie",
                    response = String.class,
                    description = TokenConstants.ACCESS_TOKEN_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                ), @ResponseHeader(
                    // why i am using \0's?
                    // look here: https://github.com/OAI/OpenAPI-Specification/issues/1237#issuecomment-423955715
                    name = "\0Set-Cookie",
                    response = String.class,
                    description = TokenConstants.REFRESH_TOKEN_COOKIE_NAME + "=null; Path=/; Secure; HttpOnly; Max-Age=0"
                )
            }
        )
    })
    // @formatter:on
    @GetMapping(params = TokenConstants.REDIRECT_URI_PARAM_NAME)
    public void logoutWithRedirectUri(
            @RequestParam(name = TokenConstants.REDIRECT_URI_PARAM_NAME) String redirectUri,
            HttpServletResponse response
    ) throws IOException {
        response.addCookie(cookieService.deletionCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, "/"));
        response.addCookie(cookieService.deletionCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, "/"));

        LogoutRedirectService.LogoutRedirect redirect = logoutRedirectService.getRedirect(redirectUri);
        response.sendRedirect(redirect.getRedirectUrl());
    }

    private LogoutRedirectService.LogoutRedirect buildLogoutRedirect(String referer) {
        LogoutRedirectService.LogoutRedirect redirect;

        if (Strings.isNotBlank(referer)) {
            redirect = logoutRedirectService.getRedirect(referer);
        } else {
            redirect = logoutRedirectService.getRedirect();
        }

        return redirect;
    }
}

package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.LogoutRedirectService;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Api(value = "OAUTH2 PKCE Logout")
@RestController("OAUTH2 PKCE Logout Controller")
@RequestMapping(path = "${pkce.logout-endpoint:/oauth2/logout}")
public class PkceLogoutRestController {

    private static final Logger LOG = LoggerFactory.getLogger(PkceLogoutRestController.class);

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
            HttpServletRequest request,
            @RequestParam(name = TokenConstants.REDIRECT_URI_PARAM_NAME, required = false) String redirectUri,
            HttpServletResponse response
    ) throws IOException {
        if (LOG.isTraceEnabled()) LOG.trace("started logout(HttpServletRequest request)");

        response.addCookie(cookieService.deletionCookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, "/"));
        response.addCookie(cookieService.deletionCookie(TokenConstants.REFRESH_TOKEN_COOKIE_NAME, "/"));

        LogoutRedirectService.LogoutRedirect redirect = buildLogoutRedirect(request, redirectUri);
        response.sendRedirect(redirect.getRedirectUrl());

        if (LOG.isTraceEnabled())
            LOG.trace("finished logout(HttpServletRequest request, HttpServletResponse response)");
    }

    private LogoutRedirectService.LogoutRedirect buildLogoutRedirect(HttpServletRequest request, String redirectUri) {
        LogoutRedirectService.LogoutRedirect redirect;

        Optional<String> maybeSelectedRedirectUri = selectRedirectUri(request, redirectUri);
        if (maybeSelectedRedirectUri.isPresent()) {
            String selectedRedirectUri = maybeSelectedRedirectUri.get();
            redirect = logoutRedirectService.getRedirect(selectedRedirectUri);
        } else {
            redirect = logoutRedirectService.getRedirect();
        }

        return redirect;
    }

    private Optional<String> selectRedirectUri(HttpServletRequest request, String redirectUri) {
        Optional<String> selectedRedirectUri = Optional.empty();

        if (StringUtils.isNotEmpty(redirectUri)) {
            selectedRedirectUri = Optional.of(redirectUri);
        }

        String referer = request.getHeader(TokenConstants.REFERER_HEADER_KEYWORD);

        if (StringUtils.isNotBlank(referer)) {
            selectedRedirectUri = Optional.of(referer);
        }

        return selectedRedirectUri;
    }
}

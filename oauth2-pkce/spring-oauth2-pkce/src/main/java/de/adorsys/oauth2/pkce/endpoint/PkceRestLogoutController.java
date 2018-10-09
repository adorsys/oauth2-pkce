package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.service.LogoutRedirectService;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RestController("Pkce Logout Endpoint")
//@RequestMapping set with de.adorsys.oauth2.pkce.WebConfig
public class PkceRestLogoutController {

    private static final Logger LOG = LoggerFactory.getLogger(PkceRestLogoutController.class);

    private final PkceProperties pkceProperties;
    private final CookieService cookieService;
    private final LogoutRedirectService logoutRedirectService;

    @Autowired
    public PkceRestLogoutController(
            PkceProperties pkceProperties,
            CookieService cookieService,
            LogoutRedirectService logoutRedirectService
    ) {
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
        this.logoutRedirectService = logoutRedirectService;
    }
    
    @GetMapping(path = {TokenConstants.LOGOUT_LINK})
    public void logout(
            HttpServletRequest request,
            @RequestParam(name = TokenConstants.REDIRECT_URI_PARAM_NAME, required=false) String redirectUri,
            HttpServletResponse response
    ) throws IOException{
        if(LOG.isTraceEnabled()) LOG.trace("started logout(HttpServletRequest request)");

    	response.addCookie(cookieService.deletionCookie(pkceProperties.getAccessTokenCookieName(), "/"));
    	response.addCookie(cookieService.deletionCookie(pkceProperties.getRefreshTokenCookieName(), "/"));

        LogoutRedirectService.LogoutRedirect redirect = buildLogoutRedirect(request, redirectUri);
        response.sendRedirect(redirect.getRedirectUrl());

        if(LOG.isTraceEnabled()) LOG.trace("finished logout(HttpServletRequest request, HttpServletResponse response)");
    }

    private LogoutRedirectService.LogoutRedirect buildLogoutRedirect(HttpServletRequest request, String redirectUri) {
        LogoutRedirectService.LogoutRedirect redirect;

        Optional<String> maybeSelectedRedirectUri = selectRedirectUri(request, redirectUri);
        if(maybeSelectedRedirectUri.isPresent()) {
            String selectedRedirectUri = maybeSelectedRedirectUri.get();
            redirect = logoutRedirectService.getRedirect(selectedRedirectUri);
        } else {
            redirect = logoutRedirectService.getRedirect();
        }

        return redirect;
    }

    private Optional<String> selectRedirectUri(HttpServletRequest request, String redirectUri) {
        Optional<String> selectedRedirectUri = Optional.empty();

        if(StringUtils.isNotEmpty(redirectUri)) {
            selectedRedirectUri = Optional.of(redirectUri);
        }

        String referer = request.getHeader(TokenConstants.REFERER_HEADER_KEYWORD);

    	if(StringUtils.isNotBlank(referer)) {
            selectedRedirectUri = Optional.of(referer);
    	}

        return selectedRedirectUri;
    }
}

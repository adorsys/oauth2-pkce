package de.adorsys.oauth2.pkce.endpoint;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.service.CookieService;
import de.adorsys.oauth2.pkce.util.TokenConstants;

@RestController("Pkce Logout Endpoint")
//@RequestMapping set with de.adorsys.oauth2.pkce.WebConfig
public class PkceRestLogoutController {
	
	private static final Logger LOG = LoggerFactory.getLogger(PkceRestLogoutController.class);

    private final PkceProperties pkceProperties;
    private final CookieService cookieService;

    @Autowired
    public PkceRestLogoutController(
            PkceProperties pkceProperties,
            CookieService cookieService
    ) {
        this.pkceProperties = pkceProperties;
        this.cookieService = cookieService;
    }
    
    @GetMapping(path = {TokenConstants.LOGOUT_LINK})
    public void logout(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(name = TokenConstants.REDIRECT_URI_PARAM_NAME, required=false) String redirectUri) throws IOException{
    	LOG.debug("started logout(HttpServletRequest request)");
    	response.addCookie(cookieService.deletionCookie(pkceProperties.getAccessTokenCookieName(), "/"));
    	response.addCookie(cookieService.deletionCookie(pkceProperties.getRefreshTokenCookieName(), "/"));
    	if(StringUtils.isBlank(redirectUri)) redirectUri = request.getHeader(TokenConstants.REFERER_HEADER_KEYWORD);
    	
    	LOG.debug("finished logout(HttpServletRequest request, HttpServletResponse response)");
    	StringBuilder ssoLogoutUri = new StringBuilder(pkceProperties.getSsoLogoutUri());
    	if(StringUtils.isNotBlank(redirectUri)) {
    		ssoLogoutUri.append("?"+TokenConstants.REDIRECT_URI_PARAM_NAME+"="+redirectUri);
    	}
    	response.sendRedirect(ssoLogoutUri.toString());
    }

}

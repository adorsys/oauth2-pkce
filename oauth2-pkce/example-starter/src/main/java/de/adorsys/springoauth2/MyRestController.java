package de.adorsys.springoauth2;

import de.adorsys.oauth2.pkce.service.AccessTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class MyRestController {

    private final AccessTokenProvider accessTokenProvider;

    @Autowired
    public MyRestController(AccessTokenProvider accessTokenProvider) {
        this.accessTokenProvider = accessTokenProvider;
    }

    @GetMapping({ "/user", "/me" })
    public Principal user(Principal principal) {
        return principal;
    }

    @GetMapping("/accesstoken")
    public OAuth2AccessToken getToken() {
        return accessTokenProvider.get();
    }
}

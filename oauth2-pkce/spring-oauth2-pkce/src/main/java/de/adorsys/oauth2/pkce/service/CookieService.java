package de.adorsys.oauth2.pkce.service;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.oauth2.pkce.PkceProperties;

@Service
public class CookieService {
    
    @Autowired
    private PkceProperties pkceProperties;
    
    public Cookie creationCookie(String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public Cookie deletionCookie(String name, String path) {
        Cookie cookie = new Cookie(name, null);
        cookie.setSecure(pkceProperties.getSecureCookie());
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        cookie.setMaxAge(0);

        return cookie;
    }
    
}

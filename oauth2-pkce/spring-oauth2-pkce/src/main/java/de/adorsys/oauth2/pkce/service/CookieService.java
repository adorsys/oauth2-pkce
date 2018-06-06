package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.PkceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;

@Service
public class CookieService {

    private static final int DEFAULT_COOKIE_DURATION = 3600;

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

    public Cookie creationCookieWithDefaultDuration(String name, String value, String path) {
        return creationCookie(name, value, path, DEFAULT_COOKIE_DURATION);
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

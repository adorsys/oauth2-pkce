package de.adorsys.utils;

import org.springframework.http.HttpHeaders;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

public class UriCookieUtils {

    public static List<HttpCookie> parseCookiesToList(HttpHeaders responseHeaders) {
        List<String> cookieHeaderValues = responseHeaders.get("Set-Cookie");

        List<HttpCookie> parsedCookies = new ArrayList<>();
        if(cookieHeaderValues != null) {
            for(String cookieHeaderValue : cookieHeaderValues) {
                List<HttpCookie> parsedCookie = HttpCookie.parse(cookieHeaderValue);
                parsedCookies.addAll(parsedCookie);
            }
        }

        return parsedCookies;
    }
}

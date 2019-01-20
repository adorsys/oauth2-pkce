package de.adorsys.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

public class UriCookieUtils {

    public static MultiValueMap<String, HttpCookie> parseCookiesAsMap(HttpHeaders responseHeaders) {
        MultiValueMap<String, HttpCookie> map = new LinkedMultiValueMap<>();

        List<HttpCookie> httpCookies = parseCookiesToList(responseHeaders);

        for(HttpCookie httpCookie : httpCookies) {
            map.add(httpCookie.getName(), httpCookie);
        }

        return map;
    }

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

package de.adorsys.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * https://stackoverflow.com/questions/11733500/getting-url-parameter-in-java-and-extract-a-specific-text-from-that-url
 */
public class UriQueryUtils {

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];

            map.put(name, value);
        }

        return map;
    }
}

package com.fibanez.java8.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author fibanez
 */
public class StringUtils {

    public static final String EMPTY = "";

    /**
     * Returns the id content, which is the last path part, from a URL
     *
     * i.e
     * if the URL looks like:
     *  https://ichef-1.bbci.co.uk/news/1024/cpsprodpb/8F1A/production/_98943663_de27-1.jpg
     * then the id content is:
     *  _98943663_de27-1.jpg
     *
     * @param strUrl
     * @return id content
     */
    public static String getLastPathPart(String strUrl) {
        try {
            URL url = new URL(strUrl);
            String[] part = url.getPath().split("/");
            return part[part.length -1];
        } catch (MalformedURLException e) {
            System.err.println("The following url is not valid: "+ strUrl);
            return StringUtils.EMPTY;
        }
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

}

package io.github.Cruisoring.helpers;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class URLHelper {

    public static Proxy getSocketProxy(String host, int port) {
        if(StringUtils.isEmpty(host))
            return null;
        port = port < 0 ? 1080 : port;

        InetSocketAddress address = new InetSocketAddress(host, port);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, address);
        Logger.D(proxy.toString());
        return proxy;
    }

    public static Proxy getHttpProxy(String host, int port) {
        if(StringUtils.isEmpty(host))
            return null;
        port = port < 0 ? 80 : port;

        InetSocketAddress address = new InetSocketAddress(host, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        Logger.D("Get proxy: %s", proxy);
        return proxy;
    }

    public static Proxy getDefaultProxy(URL url){
        Objects.requireNonNull(url);

        try {
            List<Proxy> proxies = ProxySelector.getDefault().select(url.toURI());
            return proxies.isEmpty() ? null : proxies.get(0);
        } catch (URISyntaxException e) {
            Logger.W(e);
            return null;
        }
    }

    public static String getLink(URL baseUrl, String href){
        try {
            return new URL(baseUrl, href).toString();
        }catch(Exception e){
            Logger.W("Failed to merge %s and %s: %s", baseUrl, href, e.getMessage());
            return href;
        }
    }

    public static byte[] readAsBytes(URL url, Proxy proxy){
        if(url == null)
            return null;

        try(
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InputStream inputStream = proxy != null ?
                        url.openConnection(getDefaultProxy(url)).getInputStream() : url.openStream();
                ) {
                int readSize;
                byte[] buffer = new byte[4096];

                while( ( readSize = inputStream.read(buffer)) > 0){
                    byteArrayOutputStream.write(buffer, 0, readSize);
                }
                return byteArrayOutputStream.toByteArray();
        }catch (Exception e) {
            System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            return null;
        }
    }

//    public static int indexOf(byte[] bytes, byte[] pattern, int limit){
//        if(bytes == null || pattern == null)
//            return -1;
//
//        int patternSize = pattern.length;
//        limit = limit < 1 ? bytes.length - pattern.length+1 : limit;
//
//        int index = -1;
//        for (int i = 0; i < limit; i++) {
//            if(bytes[i] != pattern[++index]){
//                index = -1;
//                continue;
//            } else {
//                index++;
//                if(index == patternSize){
//                    index = i;
//                    break;
//                }
//            }
//        }
//        return index;
//    }

    private final static Pattern charsetElementPattern =Pattern.compile("<meta\\s[^>]*charset=[^>]*>");
    public static final String readHtml(URL url){
        byte[] bytes = readAsBytes(url, null);

        if(bytes == null)
            return null;

        String utf8 = new String(bytes);
        List<String> matched = StringExtensions.getSegments(utf8, charsetElementPattern);
        if(matched.isEmpty())
            return utf8;

        String meta = matched.get(0);
        String charset = StringExtensions.valueOfAttribute(meta, "charset");
        if("utf-8".equalsIgnoreCase(charset))
            return utf8;

        try {
            String html = new String(bytes, charset);
            return html;
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }
}

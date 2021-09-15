package io.github.Cruisoring.helpers;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

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

    public static String getLink(URL baseUrl, String href){
        try {
            return new URL(baseUrl, href).toString();
        }catch(Exception e){
            Logger.W("Failed to merge %s and %s: %s", baseUrl, href, e.getMessage());
            return href;
        }
    }

    /**
     * The User Agent
     */
    private static final String AGENT = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

    public static byte[] readAsBytes2(URL url, Proxy proxy){
        if(url == null)
            return null;

        try(
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ) {
            if(proxy != null) {
                String address = proxy.address().toString();
                System.setProperty("http.proxySet", "true");
                System.setProperty("http.proxyHost", address.substring(1, address.indexOf(":")));
                System.setProperty("http.proxyPort", address.substring(address.indexOf(":")+1));
            }
            HttpURLConnection connection = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
            connection.setRequestProperty("User-Agent", AGENT);
            try (InputStream inputStream = connection.getInputStream();) {
                int readSize;
                byte[] buffer = new byte[4096];

                while ((readSize = inputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, readSize);
                }
                return byteArrayOutputStream.toByteArray();
            }
        }catch (Exception e) {
            Logger.W("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            return null;
        }
    }

    public static byte[] readAsBytes(URL url, Proxy proxy){
        if(url == null)
            return null;

        try(
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ) {
            if(proxy != null) {
                String address = proxy.address().toString();
                System.setProperty("https.proxySet", "true");
                System.setProperty("https.proxyHost", address.substring(1, address.indexOf(":")));
                System.setProperty("https.proxyPort", address.substring(address.indexOf(":")+1));
            }
//            HttpURLConnection connection = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
//            connection.setRequestProperty("User-Agent", AGENT);
            try (InputStream inputStream = url.openStream();) {
                int readSize;
                byte[] buffer = new byte[4096];

                while ((readSize = inputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, readSize);
                }
                return byteArrayOutputStream.toByteArray();
            }
        }catch (Exception e) {
            Logger.W("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            return null;
        } finally {
            if(proxy != null){
                System.setProperty("https.proxySet", "false");
//                System.setProperty("http.proxyHost", null);
//                System.setProperty("http.proxyPort", null);
            }
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
    public static final String readHtml(URL url, Proxy proxy){
        byte[] bytes = readAsBytes(url, proxy);

        if(bytes == null)
            return null;

        String utf8 = new String(bytes);
        List<String> matched = StringExtensions.getSegments(utf8, charsetElementPattern);
        if(matched.isEmpty())
            return utf8;

        String meta = matched.get(0);
        String charset = StringExtensions.valueOfAttribute(meta, "charset");
        if(charset == null || "utf-8".equalsIgnoreCase(charset))
            return utf8;

        try {
            String html = new String(bytes, charset);
            return html;
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }
}

package io.github.Cruisoring.helpers;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class HttpClientHelper {

    public static String readStringFromURL(String requestURL, String encoding){
        encoding = encoding == null ? "UTF-8" : encoding;
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), encoding))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException ex){
            return null;
        }
    }

//    public static String getHtml(String url) {
//        Objects.requireNonNull(url);
//
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpGet request = new HttpGet(url);
//        CloseableHttpResponse response = null;
//        try {
//            response = client.execute(request);
//            int status = response.getStatusLine().getStatusCode();
//
//            if (status >= 200 && status < 300) {
//                BufferedReader br;
//
//                br = new BufferedReader(new InputStreamReader(response
//                        .getEntity().getContent()));
//
//                StringBuilder stringBuilder = new StringBuilder();
//                String line;
//                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } else {
//                System.out.println("Unexpected response status: " + status);
//            }
//        } catch (IOException | UnsupportedOperationException e) {
//            e.printStackTrace();
//        } finally {
//            if(null != response){
//                try {
//                    response.close();
//                    client.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


    private String cookies;
    private HttpClient client = HttpClientBuilder.create().build();
    private final String USER_AGENT = "Mozilla/5.0";

//    public static void main(String[] args) throws Exception {
//
//        String url = "https://accounts.google.com/ServiceLoginAuth";
//        String gmail = "https://mail.google.com/mail/";
//
//        // make sure cookies is turn on
//        CookieHandler.setDefault(new CookieManager());
//
//        HttpCilentExample http = new HttpCilentExample();
//
//        String page = http.GetPageContent(url);
//
//        List<NameValuePair> postParams =
//                http.getFormParams(page, "username","password");
//
//        http.sendPost(url, postParams);
//
//        String result = http.GetPageContent(gmail);
//        System.out.println(result);
//
//        System.out.println("Done");
//    }

    private void sendPost(String url, List<NameValuePair> postParams)
            throws Exception {

        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Host", "accounts.google.com");
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Cookie", getCookies());
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Referer", "https://accounts.google.com/ServiceLoginAuth");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        post.setEntity(new UrlEncodedFormEntity(postParams));

        HttpResponse response = client.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + postParams);
        System.out.println("Response Code : " + responseCode);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        // System.out.println(result.toString());

    }

    private String GetPageContent(String url) throws Exception {

        HttpGet request = new HttpGet(url);

        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        // set cookies
        setCookies(response.getFirstHeader("Set-Cookie") == null ? "" :
                response.getFirstHeader("Set-Cookie").toString());

        return result.toString();

    }

//    public List<NameValuePair> getFormParams(
//            String html, String username, String password)
//            throws UnsupportedEncodingException {
//
//        System.out.println("Extracting form's data...");
//
//        Document doc = Jsoup.parse(html);
//
//        // Google form id
//        Element loginform = doc.getElementById("gaia_loginform");
//        Elements inputElements = loginform.getElementsByTag("input");
//
//        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
//
//        for (Element inputElement : inputElements) {
//            String key = inputElement.attr("name");
//            String value = inputElement.attr("value");
//
//            if (key.equals("Email"))
//                value = username;
//            else if (key.equals("Passwd"))
//                value = password;
//
//            paramList.add(new BasicNameValuePair(key, value));
//
//        }
//
//        return paramList;
//    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }
}

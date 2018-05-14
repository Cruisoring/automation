package io.github.Cruisoring.helpers;

import io.github.Cruisoring.wrappers.UIObject;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HtmlHelper {
    public final static String URIReservedChars = "!*'();:@&=+$,/?%#[]";
    public final static boolean imageAsBase64;
    public final static Map<String, String> escapedChars = new HashMap<>();
    public final static By RootBy = By.tagName("html");
    public final static By BodyBy = By.tagName("body");
    public final static By HtmlTitleBy = By.tagName("head>title");
    public final static By linkBy = By.cssSelector("a");
    public final static By ImageBy = By.tagName("img");
    public static final String DefaultSuffix = ".html";
    public static final Function<String, String> DefaultFilenameGenerator =
            filename -> getFilename(filename, DefaultSuffix);
    protected static Path booksDirectory = Paths.get("C:/test/books/");

    static {
        for (int i=0; i< URIReservedChars.length(); i++) {
            String sub = URIReservedChars.substring(i, i+1);
            char ch = URIReservedChars.charAt(i);
            escapedChars.put(sub,  "%" + String.format("%02x", (int)ch));
        }

        String imageAsBase64Value = System.getProperty("imageAsBase64");
        imageAsBase64 = imageAsBase64Value != null && Boolean.getBoolean(imageAsBase64Value);

        if (!Files.exists(booksDirectory)){
            try {
                booksDirectory = Files.createDirectories(booksDirectory);
            } catch (IOException e) {
                Logger.I(e);
            }
        }
    }

    public static String getFilename(String filename, String suffix){
        filename = filename.trim().replaceAll("\\s+", " ");
        return (filename.endsWith(suffix)) ? filename : filename + suffix;
    }

    public static String percentageEncoding(String urlString){
        return StringExtensions.replaceAll(urlString, escapedChars);
    }

    public static String replaceImgWithBase64(String html, String source, String base64) {
        int startIndex = source.indexOf("src=");
        startIndex = StringExtensions.indexOfAny(source, startIndex, '"', '\'');
        int endIndex = source.indexOf(source.charAt(startIndex), startIndex+1);
        String replacement = source.substring(0, startIndex+1) + base64 + source.substring(endIndex);
        return html.replace(source, replacement);
    }

    public static String replaceLink(String html, String source, String filename){
        int startIndex = source.indexOf("href=");
        startIndex = StringExtensions.indexOfAny(source, startIndex, '"', '\'');
        int endIndex = source.indexOf(source.charAt(startIndex), startIndex+1);
        if(filename == null) {
            filename = source.substring(source.indexOf('>', endIndex)+1);
            filename = filename.substring(0, filename.indexOf("</a>"));
            filename = filename.trim() + ".html";
        }
        String replacement = source.substring(0, startIndex+1) + filename + source.substring(endIndex);
        return html.replace(source, replacement);
    }

    public static final Map<URL, String> mappedImages = new HashMap<>();

    protected final Worker worker;
    public final URL rootUrl;
    private final Function<String, String> filenameGenerator;
    private final UIObject rootObject;
    public final Map<URL, String> mappedURLs = new HashMap<>();
    public final List<URL> topics = new ArrayList<>();
    File bookRootFolder;

    public HtmlHelper(URL rootUrl, Worker worker, Function<String, String> filenameGenerator){
        if(worker == null || rootUrl == null)
            throw new ExceptionInInitializerError("Arguments cannot be null.");

        this.filenameGenerator = (filenameGenerator == null) ? DefaultFilenameGenerator : filenameGenerator;
        this.worker = worker;
        rootObject = new UIObject(worker, RootBy);
        this.rootUrl = rootUrl;
    }

    public HtmlHelper(URL rootUrl, Worker worker){
        this(rootUrl, worker, null);
    }

    private URL getUrl(String href){
        URL url = null;
        try {
            url = new URL(rootUrl, href);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public Path saveIndex(String bookname){
        return saveIndex(bookname, null, null);
    }

    public Path saveIndex(String bookname, UIObject.Collection content){
        return saveIndex(bookname, content, null);
    }

    //TODO: using Pattern instead of UICollection ?
    public Path saveIndex(String bookname, UIObject.Collection content, String indexFilename){
        Path bookRoot = Paths.get(booksDirectory.toString(), bookname);
        if (!Files.exists(bookRoot)){
            try {
                bookRoot = Files.createDirectories(bookRoot);
            } catch (IOException e) {
                Logger.I(e);
            }
        }
        indexFilename = filenameGenerator.apply(indexFilename == null ? bookname : indexFilename);
        bookRootFolder = bookRoot.toFile();
        File file = new File(bookRootFolder, indexFilename);
        content = content == null ? new UIObject.Collection(rootObject, linkBy) : content;
        String reLinkedHtml = getRelinkedHtml(content);

        Map<String, String> imageTokens = getImageTokens(content);
        String replacement = StringExtensions.replaceAll(reLinkedHtml, imageTokens);

        String html = replacedHtml(content, replacement);
        try {
            Path bookPath = Files.write(file.toPath(), html.getBytes());
            Logger.I("%s of %s is saved as %s", content, bookname, bookPath);
            mappedURLs.put(rootUrl, indexFilename);
            return bookPath;
        } catch (Exception ex) {
            return null;
        }
    }

    private String replacedHtml(UIObject content, String replacement) {
        String html;
        if (content != rootObject) {
            html = rootObject.getOuterHTML();
            int startIndex = html.indexOf("<", html.indexOf("<body") + 1);
            int endIndex = html.lastIndexOf(">", html.lastIndexOf("</body>")) + 1;
            html = html.substring(0, startIndex) + replacement + html.substring(endIndex);
        } else {
            html = replacement;
        }
        return html;
    }

    private Map<String,String> getImageTokens(UIObject content) {
        Map<String,String> tokens = new HashMap<>();
        UIObject.Collection images = new UIObject.Collection(content, ImageBy);
        for(UIObject image: images.getChildren()){
            String src = image.getAttribute("src");
            URL imageUrl = getUrl(src);
            if(imageUrl == null)
                continue;

            if(mappedImages.containsKey(imageUrl)){
                tokens.put(src, mappedImages.get(imageUrl));
            } else {
                String base64 = image.asBase64();
                if (base64 != null) {
                    tokens.put(src, base64);
                    mappedImages.put(imageUrl, base64);
                }
            }
        }
        return tokens;
    }

    private String getRelinkedHtml(UIObject.Collection links) {
        Map<String, String> hrefTitleMap = new HashMap<>();
        final String rootUrlPath = rootUrl.getPath();
        for (UIObject link : links.getChildren()) {
            String href = link.getAttribute("href").trim();
            URL url = getUrl(href);

            if(href.startsWith("#") || !url.getPath().contains(rootUrlPath))
                continue;

            int index = href.indexOf('#');
            if(index != -1){
                href = href.substring(0, index);
                url = getUrl(href);
            }

            if(!hrefTitleMap.containsKey(href)){
                topics.add(url);
                String title = StringExtensions.removeAllCharacters(link.getAllText().trim(),
                        StringExtensions.WindowsSpecialCharacters)
                        .replaceAll("\\s+", " ");
                if(StringUtils.isNotEmpty(title))
                    hrefTitleMap.put(href, filenameGenerator.apply(title));
            }
        }

        Map<String, String> tokens = new HashMap<>();
        for (Map.Entry<String, String> entry : hrefTitleMap.entrySet()) {
            String relativePath = entry.getKey();
            URL url = getUrl(relativePath);
            if(!mappedURLs.containsKey(url)){
                String title = hrefTitleMap.get(relativePath);
                mappedURLs.put(url, title);
            }
            String mapped = mappedURLs.get(url);
            String encoded = percentageEncoding(mapped);
            if(!mapped.equals(encoded)){
                Logger.V("'%s' is encoded as '%s'", mapped, encoded);
            }
            tokens.put(relativePath, encoded);
        }
        String html = links.getOuterHTML();
        String result = StringExtensions.replaceAll(html, tokens);
        return result;
    }

    public Path saveChapter(URL url, UIObject.Collection content){
        if(!mappedURLs.containsKey(url))
            return null;

        worker.gotoUrl(url);

        content = content == null ? new UIObject.Collection(rootObject, linkBy) : content;
        String reLinkedHtml = replaceChapterLinks(content);
        Map<String, String> imageTokens = getImageTokens(content);
        String replacement = StringExtensions.replaceAll(reLinkedHtml, imageTokens);

        String html = replacedHtml(content, replacement);
        File file = new File(bookRootFolder, mappedURLs.get(url));
        try {
            Path path = Files.write(file.toPath(), html.getBytes());
            Logger.I("%s is saved as %s", content, path);
            return path;
        } catch (Exception ex) {
            Logger.W(ex);
            return null;
        }
    }

    public int saveTopics(){
        int count = 0;
        for (URL chapterUrl : topics) {
//            if(!chapterUrl.toString().contains("012"))
//                continue;
            if(null != saveChapter(chapterUrl, null))
                count++;
        }
        return count;
    }

    private String replaceChapterLinks(UIObject.Collection content) {
        List<String> knownUrls = StringExtensions.sortedListByLengthDesc(
                mappedURLs.keySet().stream()
                        .map(url -> url.toString())
                        .collect(Collectors.toList()));
        String contentHtml = content.getOuterHTML();
        final String rootUrlPath = rootUrl.getPath();
        List<String> hrefs = StringExtensions.getSegments(contentHtml, StringExtensions.linkPattern)
                .stream()
                .map(e -> StringExtensions.valueOfAttribute(e, "href"))
                .distinct()
                .filter(href -> href != null && !href.contains("#") &&
                        (!href.startsWith("http") || href.startsWith(rootUrlPath)))
                .collect(Collectors.toList());

        Map<String, URL> urlMap = hrefs.stream()
                .collect(Collectors.toMap(
                        href -> href,
                        href -> getUrl((String)href)
                ));

        Map<String, String> tokens = new HashMap<>();
        for (String ref : hrefs) {
            String href = ref;
            URL url = urlMap.get(href);

            String matched = StringExtensions.firstStartsWith(getUrl(href).toString(), knownUrls);
            if (matched == null)
                continue;
            URL matchedUrl = getUrl(matched);
            String mapped = mappedURLs.get(matchedUrl);
            String encoded = percentageEncoding(mapped);
            if(!mapped.equals(encoded)){
                Logger.V("'%s' is encoded as '%s'", mapped, encoded);
            }
            tokens.put(href, encoded);
        }
        String html = content.getOuterHTML();
        return StringExtensions.replaceAll(html, tokens);
    }

//
//    public Path saveAsHtml(File file) {
//        return saveAsHtml(file, null);
//    }
//
//    public Path saveAsHtml(File file, UIObject contentObject) {
//        return saveAsHtml(file, contentObject,
//                new UIObject.Collection(contentObject==null? getRoot() : contentObject, By.cssSelector("img")),
//                new UIObject.Collection(contentObject==null? getRoot() : contentObject, By.cssSelector("a"))
//        );
//    }
//
//    public Path saveAsHtml(File file, UIObject contentObject, UICollection... toBeReplaced) {
//        List<String> knownUrls = mappedURLs.isEmpty() ? null : StringExtensions.sortedListByLengthDesc(mappedURLs.keySet());
//
//        for (UICollection collection : toBeReplaced) {
//            List children = collection == null ? null : collection.getChildren();
//            if (children == null)
//                continue;
//            for (Object child : collection.getChildren()) {
//                UIObject uiObject = (UIObject) child;
//                if (uiObject == null){
//                    uiObject.invalidate();
//                }
//                String tagname = uiObject.getTagName();
//                if (StringUtils.equalsIgnoreCase(tagname, "img")) {
//                    String src = uiObject.getAttribute("src");
//                    if(src == null)
//                        continue;
//                    if (!tokens.containsKey(src)) {
//                        String base64 = uiObject.executeScript(getImageBase64).toString();
//                        tokens.put(src, base64);
//                    }
//                } else if (StringUtils.equalsIgnoreCase(tagname, "a")) {
//                    String href = uiObject.getAttribute("href");
//                    if(href == null)
//                        continue;
//                    String absoluteUrl = null;
//                    try {
//                        URL asUrl = new URL(href);
//                        URL aUrl = new URL(baseUrl, href);
//                        absoluteUrl = aUrl.getPath();
//                    } catch (MalformedURLException e) {
//                        continue;
//                    }
//                    if(absoluteUrl != null && knownUrls != null) {
//                        String matched = StringExtensions.firstStartsWith(absoluteUrl, knownUrls);
//                        if (matched == null) {
//                            String newFilename = uiObject.getTextContent().trim() + ".html";
//                            tokens.put(href, newFilename);
//                        } else {
//                            String newLink = href.replace(matched, mappedURLs.get(matched));
//                            if(!href.contentEquals(newLink))
//                                tokens.put(href, newLink);
//                        }
//                        continue;
//                    }
//
//                    if (!tokens.containsKey(href)) {
//                        String newFilename = uiObject.getAllText().trim() + ".html";
//                        tokens.put(href, newFilename);
//                    }
//                }
//            }
//        }
//
//        String replacement = StringExtensions.replaceAll(contentHtml, tokens);
//
//        String html;
//        if (contentObject != null) {
//            UIObject root = getRoot();
//            html = root.getOuterHTML();
//            int startIndex = html.indexOf("<", html.indexOf("<body") + 1);
//            int endIndex = html.lastIndexOf(">", html.lastIndexOf("</body>")) + 1;
//            html = html.substring(0, startIndex) + replacement + html.substring(endIndex);
//        } else {
//            html = replacement;
//        }
//        try {
//            Path path = Files.write(file.toPath(), html.getBytes());
//            final URL startUrl = baseUrl;
//            tokens.forEach((k, v)->{
//                try {
//                    URL aUrl = new URL(startUrl, k);
//                    String key = aUrl.getPath();
//                    if(!mappedURLs.containsKey(key))
//                        mappedURLs.put(key, v);
//                }catch (Exception e){
//                }
//            });
//            Logger.I("%s is saved as %s.", currentUrl, path);
//            return path;
//        } catch (Exception ex) {
//            return null;
//        }
//    }

}

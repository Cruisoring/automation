package com.least.automation.helpers;

import com.least.automation.wrappers.UICollection;
import com.least.automation.wrappers.UIObject;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookHelper {
    public final static By RootBy = By.tagName("html");
    public final static By BodyBy = By.tagName("body");
    public final static By HtmlTitleBy = By.tagName("head>title");
    public final static By linkBy = By.cssSelector("a");
    public final static By ImageBy = By.tagName("img");
    public static final String DefaultSuffix = ".html";
    public static final Function<String, String> DefaultFilenameGenerator =
            n -> n.endsWith(DefaultSuffix) ? n : n + DefaultSuffix;
    protected static Path booksDirectory = Paths.get("C:/test/books/");

    static {
        if (!Files.exists(booksDirectory)){
            try {
                booksDirectory = Files.createDirectories(booksDirectory);
            } catch (IOException e) {
                Logger.I(e);
            }
        }
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
//
//
//    public static String asHtmlElement(UIObject object){
//        String objectHtml = object.getOuterHTML();
//        int startIndex, endIndex;
//        UIObject.Collection images = new UIObject.Collection(object, By.cssSelector("img"));
//        for (UIObject image : images.getChildren()) {
//            String base64 = image.executeScript(getImageBase64).toString();
//            if (base64 == null || base64.length() == 0){
//                Logger.W("Failed to replace " + image);
//                continue;
//            }
//            String imageHtml = image.getOuterHTML();
//            startIndex = imageHtml.indexOf("src=");
//            startIndex = StringExtensions.indexOfAny(imageHtml, startIndex, '"', '\'');
//            endIndex = imageHtml.indexOf(imageHtml.charAt(startIndex), startIndex+1);
//            String replacement = imageHtml.substring(0, startIndex+1) + base64 + imageHtml.substring(endIndex);
//            objectHtml = objectHtml.replace(imageHtml, replacement);
//        }
//
//        String html = root.getOuterHTML();
//        startIndex = html.indexOf("<", html.indexOf("<body") + 3);
//        endIndex = html.lastIndexOf(">", html.lastIndexOf("</body>"))+1;
//        html = html.substring(0, startIndex) + objectHtml + html.substring(endIndex);
//        return html;
//    }

    protected final Worker worker;
    private final URL rootUrl;
    private final Function<String, String> filenameGenerator;
    private final UIObject rootObject;
    private final Map<String, String> mappedURLs = new HashMap<>();
    Path bookPath;

    public BookHelper(URL rootUrl, Worker worker, Function<String, String> filenameGenerator){
        if(worker == null || rootUrl == null)
            throw new ExceptionInInitializerError("Arguments cannot be null.");

        this.filenameGenerator = (filenameGenerator == null) ? DefaultFilenameGenerator : filenameGenerator;
        this.worker = worker;
        rootObject = new UIObject(worker, RootBy);
        this.rootUrl = rootUrl;
    }

    public BookHelper(URL rootUrl, Worker worker){
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
        File file = new File(bookRoot.toFile(), indexFilename);
        content = content == null ? new UIObject.Collection(rootObject, linkBy) : content;
        String reLinkedHtml = getRelinkedHtml(content, true);
//        if(!mappedURLs.containsKey(rootUrl))
//            mappedURLs.put(rootUrl, indexFilename);

        Map<String, String> imageTokens = getImageTokens(content);
        String replacement = StringExtensions.replaceAll(reLinkedHtml, imageTokens);

        String html = replacedHtml(content, replacement);
        try {
            bookPath = Files.write(file.toPath(), html.getBytes());
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

    private String getRelinkedHtml(UIObject.Collection links, boolean alwaysMap) {
        Map<String, String> hrefTitleMap = new HashMap<>();
        for (UIObject link : links.getChildren()) {
            String href = link.getAttribute("href");
            if(!hrefTitleMap.containsKey(href)){
                String title = link.getAllText().trim();
                hrefTitleMap.put(href, title);
            }
        }

        Map<String, URL> urlMap = hrefTitleMap.keySet().stream()
                .collect(Collectors.toMap(
                href -> href,
                href -> getUrl((String)href)
        ));

        final String rootUrlPath = rootUrl.getPath();
        Map<String, String> tokens = new HashMap<>();
        for (Map.Entry<String, URL> entry : urlMap.entrySet()) {
            URL entryUrl = entry.getValue();
            if(!entryUrl.getPath().contains(rootUrlPath))
                continue;

            if(!mappedURLs.containsKey(entryUrl) && alwaysMap){
                String title = hrefTitleMap.get(entry.getKey());
                title = filenameGenerator.apply(title);
                mappedURLs.put(entryUrl.getPath(), title);
            }
            tokens.put(entry.getKey(), mappedURLs.get(entryUrl));
        }
        String html = links.getOuterHTML();
        String result = StringExtensions.replaceAll(html, tokens);
        return result;
    }

    public Path saveChapter(URL chapterUrl, UIObject content){
        worker.driver.get(chapterUrl.getPath());
        worker.waitPageReady();

        content = content == null ? rootObject : content;
        String reLinkedHtml = replaceChapterLinks(content);
        Map<String, String> imageTokens = getImageTokens(content);
        String replacement = StringExtensions.replaceAll(reLinkedHtml, imageTokens);

        String html = replacedHtml(content, replacement);
        File file = new File(bookPath.toFile(), mappedURLs.get(chapterUrl));
        try {
            Path path = Files.write(file.toPath(), html.getBytes());
            return path;
        } catch (Exception ex) {
            return null;
        }
    }

    private String replaceChapterLinks(UIObject content) {
        List<String> knownUrls = StringExtensions.sortedListByLengthDesc(
                mappedURLs.keySet().stream()
                        .collect(Collectors.toList()));
        String contentHtml = content.getOuterHTML();
        List<String> urlElements = StringExtensions.getSegments(contentHtml, StringExtensions.linkPattern);
        Map<String, URL> urlMap = urlElements.stream()
                .map(e -> StringExtensions.valueOfAttribute(e, "href"))
                .collect(Collectors.toMap(
                        href -> href,
                        href -> getUrl((String)href)
                ));

        final String rootUrlPath = rootUrl.getPath();
        Map<String, String> tokens = new HashMap<>();
        for (Map.Entry<String, URL> entry : urlMap.entrySet()) {
            URL entryUrl = entry.getValue();
            if(!entryUrl.getPath().contains(rootUrlPath))
                continue;

            String matched = StringExtensions.firstStartsWith(entry.getKey(), knownUrls);
            URL matchedUrl = getUrl(matched);
            if (matchedUrl == null) continue;
            String replacedUrl = entry.getKey().replace(matched, mappedURLs.get(matchedUrl));
            tokens.put(entry.getKey(), replacedUrl);
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

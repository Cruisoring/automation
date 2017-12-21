package com.least.automation.helpers;

public class HtmlHelper {
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
}

package screens;

import com.least.automation.helpers.ResourceHelper;
import com.least.automation.helpers.Worker;
import com.least.automation.wrappers.Screen;
import com.least.automation.wrappers.UIObject;
import org.openqa.selenium.By;

import java.net.URL;
import java.util.Properties;

public class ViewScreen extends Screen {
    public static final String defaultPropertyFilename = "default.properties";
    public static final String indexFilename;
    static {
        Properties properties = ResourceHelper.getProperties(defaultPropertyFilename);
        indexFilename = properties.getProperty("indexFilename");
    }

    public final UIObject detailBook;
    public final UIObject.Collection topics;
//    public final UIObject.Collection links;
    public URL baseUrl;

    public ViewScreen(Worker worker){
        super(worker);
        detailBook = new UIObject(this, By.cssSelector("section[role='document'], section.detail-book"));
        topics = new UIObject.Collection(detailBook, By.cssSelector("li[class^='toc-level']>a"));
//        links = new UIObject.Collection(detailBook, By.cssSelector("a"));
    }

//    public String saveIndex(Path folderPath){
//        waitScreenVisible(5*1000);
//        try {
//            baseUrl = new URL(worker.driver.getCurrentUrl());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        if (!Files.exists(folderPath)){
//            try {
//                folderPath = Files.createDirectories(folderPath);
//            } catch (IOException e) {
//                Logger.I(e);
//            }
//        }
//        File indexFile = new File(folderPath.toFile(), indexFilename);
//        Path saved = worker.saveAsHtml(indexFile, detailBook, topics,
//                new UIObject.Collection(detailBook, By.cssSelector("img")));
//        return saved.toString();
//    }
//
//    public boolean saveTopic(String topicUrl, File file){
//        if(topicUrl == null || file == null)
//            return false;
//
//        worker.gotoUrl(topicUrl);
//        waitPageReady();
//
//        Path saved = worker.saveAsHtml(file, detailBook,
//                new UIObject.Collection(detailBook, By.cssSelector("a")),
//                new UICollection(detailBook, By.cssSelector("img")));
//        return saved != null;
//    }
//
//    public int saveAllTopics(Path folderPath){
//        List<UIObject> topicObjects = topics.getChildren();
//        List<String> topicUrls = topicObjects.stream().map(o -> {
//                String href = o.getAttribute("href");
//                if (href == null) return null;
//                try {
//                    return new URL(baseUrl, href).getPath();
//                }catch (Exception e){
//                    return null;
//                }
//            }
//        ).filter(u -> u!=null).collect(Collectors.toList());
//
//        int len = topicUrls.size();
//        int count = 0;
//        File dir = folderPath.toFile();
//        for (int i = 0; i < len; i ++){
//            String url = topicUrls.get(i);
//            String filename = Worker.mappedURLs.get(url);
//            try {
//                URL aUrl = new URL(baseUrl, url);
//                url = aUrl.toString();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//
//            File file = new File(dir, filename);
//            if(saveTopic(url, file))
//                count++;
//        }
//
//        return count;
//    }
//
//    public String saveTopic(UIObject topicLink, Path folderPath){
//        if(topicLink == null || !topicLink.isVisible())
//            return "";
//
//        String href = topicLink.getAttribute("href");
//        if(href == null)
//            return null;
//        String absoluteUrl = null;
//        try {
//            URL aUrl = new URL(baseUrl, href);
//            absoluteUrl = aUrl.getPath();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        String filename = Worker.mappedURLs.get(absoluteUrl);
//        if(filename == null){
//            filename = topicLink.getTextContent() + ".html";
//        }
//
//        topicLink.click(3000);
//        waitPageReady();
//
//        File topicFile = new File(folderPath.toFile(), filename);
//        Path saved = worker.saveAsHtml(topicFile, detailBook,
//                new UIObject.Collection(detailBook, By.cssSelector("a")),
//                new UICollection(detailBook, By.cssSelector("img")));
//        return saved.toString();
//    }
}

package screens;

import io.github.Cruisoring.helpers.ResourceHelper;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UIObject;
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
        detailBook = new UIObject(this, By.cssSelector("section[role='document'], section.detail-book, .detail-toc"));
        topics = new UIObject.Collection(detailBook, By.cssSelector("li[class^='toc-level']>a"));
//        links = new UIObject.Collection(detailBook, By.cssSelector("a"));
    }
}

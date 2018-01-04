import com.least.automation.helpers.HtmlHelper;
import com.least.automation.helpers.Logger;
import com.least.automation.helpers.ResourceHelper;
import com.least.automation.helpers.Worker;
import org.testng.Assert;
import org.testng.annotations.Test;
import screens.LoginScreen;
import screens.SearchScreen;
import screens.ViewScreen;

import java.net.URL;
import java.util.Properties;

@Test
public class saveBookTests {
    public static final String defaultPropertyFilename = "default.properties";
    public static final String startUrl;
    public static final String firstBook;
    public static final String saveLocation;

    static Worker worker;

    static SearchScreen searchScreen;
    static ViewScreen viewScreen;

    static {
        Properties properties = ResourceHelper.getProperties(defaultPropertyFilename);

        startUrl = properties.getProperty("startUrl");
        firstBook = properties.getProperty("firstBook");
        saveLocation = properties.getProperty("saveLocation");

        worker = Worker.getAvailable();

        searchScreen = worker.getScreen(SearchScreen.class);
        viewScreen = worker.getScreen(ViewScreen.class);
    }

//    @BeforeClass
//    public static void beforeClass(){
//    }

    @Test
    public void saveBookTest_withIndexSaved_ChapterURLsUpdated(){
        worker.gotoUrl(startUrl);

        String bookname = searchScreen.searchBook(firstBook);
        boolean loginSuccess= worker.getScreen(LoginScreen.class).login();
        Assert.assertTrue(loginSuccess);

        URL url = worker.getUrl();
        HtmlHelper bookHelper = new HtmlHelper(url, worker);
        bookHelper.saveIndex(bookname, viewScreen.topics, String.format("_%s.html", bookname));
    }

    @Test
    public void saveBookTest_withIndexAndTopicsSaved_AllLinksUpdated(){
        worker.gotoUrl(startUrl);

        String bookname = searchScreen.searchBook(firstBook);
        boolean loginSuccess= worker.getScreen(LoginScreen.class).login();
        Assert.assertTrue(loginSuccess);

        URL url = worker.getUrl();
        HtmlHelper bookHelper = new HtmlHelper(url, worker);
        bookHelper.saveIndex(bookname, viewScreen.topics, String.format("_%s.html", bookname));

        int count = 0;
        for (URL chapterUrl : bookHelper.topics) {
//            if(!chapterUrl.toString().contains("9781680502794/f_0005.xhtml#d24e111"))
//                continue;
            if(null != bookHelper.saveChapter(chapterUrl, null))
                count++;
        }
        Logger.I("There are %d topics saved successfully.", count);
    }
}

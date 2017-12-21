import com.least.automation.helpers.Logger;
import com.least.automation.helpers.ResourceHelper;
import com.least.automation.helpers.Worker;
import org.testng.Assert;
import org.testng.annotations.Test;
import screens.LoginScreen;
import screens.SearchScreen;
import screens.ViewScreen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Test
public class saveBookTests {
    public static final String defaultPropertyFilename = "default.properties";
    public static final String startUrl;
    public static final String firstBook;
    public static final String saveLocation;

    static Worker worker;
    SearchScreen searchScreen;
    ViewScreen bookScreen;

    static {
        Properties properties = ResourceHelper.getProperties(defaultPropertyFilename);
        properties.stringPropertyNames()
                .stream().filter(p -> p.contains("webdriver")).forEach(p ->
                System.setProperty(p.toString(), properties.getProperty(p.toString()))
        );
        String chromeDriverPath = System.getProperty("webdriver.chrome.driver");

        startUrl = properties.getProperty("startUrl");
        firstBook = properties.getProperty("firstBook");
        saveLocation = properties.getProperty("saveLocation");


        worker = Worker.getAvailable();
    }

//    @BeforeClass
//    public static void beforeClass(){
//    }


    @Test
    public void saveBookTest_withSingleBook_Success(){
        searchScreen = worker.getScreen(SearchScreen.class);
        bookScreen = worker.getScreen(ViewScreen.class);
        worker.gotoUrl(startUrl);

        String bookname = searchScreen.searchBook(firstBook);
        boolean loginSuccess= worker.getScreen(LoginScreen.class).login();
        Assert.assertTrue(loginSuccess);

        Path folderPath = Paths.get(saveLocation, bookname);

        bookScreen.saveIndex(folderPath);

        int count = bookScreen.saveAllTopics(folderPath);
        Logger.I("There are %d topics saved successfully.", count);
//        bookScreen.saveTopic(bookScreen.topics.get(20), folderPath);
    }
}

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.ResourceHelper;
import io.github.Cruisoring.helpers.Worker;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import screens.ArticleScreen;
import screens.SearchScreen;

import java.util.*;

@Test
public class searchToMine {
    public static final String defaultPropertyFilename = "default.properties";
    public static final String startUrl;


    static {
        Properties properties = ResourceHelper.getProperties(defaultPropertyFilename);

        startUrl = properties.getProperty("startUrl");
    }

    static Worker worker;

    SearchScreen searchScreen;
    ArticleScreen articleScreen;

    @BeforeClass
    public static void beforeClass(){
        worker = Worker.getAvailable();
    }

    @BeforeMethod
    public void beforeMethod(){
        worker.invalidate();
        searchScreen = worker.getScreen(SearchScreen.class);
        articleScreen = worker.getScreen(ArticleScreen.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if(worker != null){
            worker.close();
            worker = null;
        }
    }

    @Test
    public void searchThrowable() {
        List<String> keywords = new ArrayList<String>( Arrays.asList("functional interface", "lambda throws",
                "lambda exception", "checked exception", "unchecked exception", "exception in stream", "throws in stream", "throwable lambda"));
        String[] expectedTitleKeys = new String[]{"functionExtensions", "function extension"};
        String expectedUrl ="codeproject.com/Articles/1231137";

        Random random = new Random();
        StringBuilder sb = new StringBuilder("java 8 ");
        int count = keywords.size();
        int index = random.nextInt(count);
        sb.append(" " + keywords.get(index));
        keywords.remove(index);
        index = random.nextInt(keywords.size());
        sb.append(" " + keywords.get(index));

        String keyword = sb.toString();
        worker.gotoUrl(startUrl);
        searchScreen.search(keyword);

        do{
            if(searchScreen.openResultPage(expectedTitleKeys, expectedUrl)) {
                articleScreen.read();
                successCount++;
                break;
            }
            if(!searchScreen.hasMorePage())
                break;
            searchScreen.next.scrollIntoView();
            searchScreen.next.clickByScript();
        }while (true);

    }

    @Test
    public void searchTuple() {
        List<String> keywords = new ArrayList<String>( Arrays.asList("functional programming", "tuple", "tuples", "multi different objects",
                "Data Pair", "Immutable set", "immutable data", "strong-typed values", "tuplets", "autocloseable data set", "comparable",
                "multiple return"));
        String[] expectedTitleKeys = new String[]{"functionExtensions", "function extension"};
        String expectedUrl ="codeproject.com/Articles/1232570";

        Random random = new Random();
        StringBuilder sb = new StringBuilder("java 8 ");
        int count = keywords.size();
        int index = random.nextInt(count);
        sb.append(" " + keywords.get(index));
        keywords.remove(index);
        index = random.nextInt(keywords.size());
        sb.append(" " + keywords.get(index));

        String keyword = sb.toString();
        worker.gotoUrl(startUrl);
        searchScreen.search(keyword);

        do{
            if(searchScreen.openResultPage(expectedTitleKeys, expectedUrl)) {
                articleScreen.read();
                successCount++;
                break;
            }
            if(!searchScreen.hasMorePage())
                break;
            searchScreen.next.scrollIntoView();
            searchScreen.next.clickByScript();
        }while (true);
    }

    int successCount = 0;
    int testCount = 0;
    @Test
    public void runEndlessly(){

        try {
            while (true) {
                if (worker == null) {
                    worker = Worker.getAvailable();
                    searchScreen = worker.getScreen(SearchScreen.class);
                    articleScreen = worker.getScreen(ArticleScreen.class);
                }
                testCount++;
                searchThrowable();
                searchTuple();
                if (worker != null) {
                    worker.close();
                    worker = null;
                }
            }
        }catch (Exception ex){
            Logger.E(ex);
            Logger.I("Try %d, %d success", testCount, successCount);
            runEndlessly();
        }finally {
            Logger.I("Try %d, %d success", testCount, successCount);
        }
    }
}

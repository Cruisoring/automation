import io.github.Cruisoring.helpers.ResourceHelper;
import io.github.Cruisoring.helpers.Worker;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import screens.ArticleScreen;
import screens.SearchScreen;

import java.util.Properties;
import java.util.Random;

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
        String[] keywords = new String[]{"functional interface", "lambda", "lambda expression", "checked", "unchecked", "exception", "exception in stream", "throws in stream", "throwable"};
        String[] expectedTitleKeys = new String[]{"functionExtensions", "function extension"};
        String expectedUrl ="codeproject.com/Articles/1231137";

        Random random = new Random();
        StringBuilder sb = new StringBuilder("java 8 ");
        int count = keywords.length;
        int index = random.nextInt(count);
        sb.append(" " + keywords[index]);
        index += random.nextInt(count-1);
        sb.append(" " + keywords[index % count]);

        String keyword = sb.toString();
        worker.gotoUrl(startUrl);
        searchScreen.search(keyword);

        do{
            if(searchScreen.openResultPage(expectedTitleKeys, expectedUrl)) {
                articleScreen.read();
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
        String[] keywords = new String[]{"functional programming", "tuple", "tuples", "Triple", "Dual", "Pair", "immutable data", "strong-typed values", "tuplets", "autocloseable", "comparable"};
        String[] expectedTitleKeys = new String[]{"functionExtensions", "function extension"};
        String expectedUrl ="codeproject.com/Articles/1232570";

        Random random = new Random();
        StringBuilder sb = new StringBuilder("java ");
        int count = keywords.length;
        int index = random.nextInt(count);
        sb.append(" " + keywords[index]);
        index += random.nextInt(count-1);
        sb.append(" " + keywords[index % count]);

        String keyword = sb.toString();
        worker.gotoUrl(startUrl);
        searchScreen.search(keyword);

        do{
            if(searchScreen.openResultPage(expectedTitleKeys, expectedUrl)) {
                articleScreen.read();
                break;
            }
            if(!searchScreen.hasMorePage())
                break;
            searchScreen.next.scrollIntoView();
            searchScreen.next.clickByScript();
        }while (true);
    }

    @Test
    public void runEndlessly(){
        while (true){
            searchThrowable();
            searchTuple();
        }
    }
}

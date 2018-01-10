import com.least.automation.helpers.*;
import org.testng.Assert;
import org.testng.annotations.*;
import screens.LoginScreen;
import screens.SearchScreen;
import screens.ViewScreen;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

@Test
public class saveBookTests {
    public static final String defaultPropertyFilename = "default.properties";
    public static final String startUrl;
    public static final String firstBook;
    public static final String saveLocation;
    protected static Path booksDirectory = Paths.get("C:/test/books/");


    static {
        Properties properties = ResourceHelper.getProperties(defaultPropertyFilename);

        startUrl = properties.getProperty("startUrl");
        firstBook = properties.getProperty("firstBook");
        saveLocation = properties.getProperty("saveLocation");

        if (!Files.exists(booksDirectory)){
            try {
                booksDirectory = Files.createDirectories(booksDirectory);
            } catch (IOException e) {
                Logger.I(e);
            }
        }
    }

    static Worker worker;

    SearchScreen searchScreen;
    ViewScreen viewScreen;

    @BeforeClass
    public static void beforeClass(){
        worker = Worker.getAvailable();
    }

    @BeforeMethod
    public void beforeMethod(){
        worker.invalidate();
        searchScreen = worker.getScreen(SearchScreen.class);
        viewScreen = worker.getScreen(ViewScreen.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if(worker != null){
            worker.close();
            worker = null;
        }
    }

    @Test
    public void saveBookTest_withIndexSaved_ChapterURLsUpdated() {
        worker.gotoUrl(startUrl);
        saveBookIndex(firstBook);
    }

    private HtmlHelper saveBookIndex(String bookKey){
        String bookname = searchScreen.searchBook(bookKey);
        Path bookRoot = Paths.get(booksDirectory.toString(), bookname);
        if (!Files.exists(bookRoot)){
            try {
                bookRoot = Files.createDirectories(bookRoot);
            } catch (IOException e) {
                Logger.I(e);
            }
        }

        boolean loginSuccess= worker.getScreen(LoginScreen.class).login();
        Assert.assertTrue(loginSuccess);

        URL url = worker.getUrl();
        HtmlHelper bookHelper = new HtmlHelper(url, worker);
        bookHelper.saveIndex(bookname, viewScreen.topics, String.format("_%s.html", bookname));
        return bookHelper;
    }

    private void saveBook(String bookKey){
        worker.gotoUrl(startUrl);

        HtmlHelper bookHelper = saveBookIndex(bookKey);

        int count = bookHelper.saveTopics();
        Logger.I("There are %d topics saved successfully.", count);
    }

    @Test
    public void saveBookTest_withIndexAndTopicsSaved_AllLinksUpdated(){
        saveBook(firstBook);
    }

    @Test
    public void saveBookTest_withFoldersExisted_reloadAllOfThem(){
        File bookDir = booksDirectory.toFile();
        File[] folders = bookDir.listFiles(f -> f.isDirectory());
        LocalDateTime since = LocalDate.now().atTime(11, 28);
        for (File folder : folders) {
            LocalDateTime lastModified = DateTimeHelper.fromTimestamp(folder.lastModified()/1000);
            if(lastModified.compareTo(since) < 0)
                continue;
            for (File file : folder.listFiles()) {
                file.delete();
            }
            String folderKey = folder.getName();
            saveBook(folderKey);
            beforeMethod();
        }
    }
}

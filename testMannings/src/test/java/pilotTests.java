import com.least.automation.helpers.Worker;
import org.testng.Assert;
import org.testng.annotations.Test;
import screens.baseScreen;

@Test
public class pilotTests {
    public static String startUrl = "https://livebook.manning.com/#!/book/rx-dot-net-in-action/chapter-1";

    static {
        System.setProperty("webdriver.chrome.driver", "../drivers/chromedriver.exe");
//        System.setProperty("webdriver.ie.driver", "vendor/IEDriverServer.exe");
    }

    @Test
    public void openChapterTwo(){
        Worker worker = Worker.getAvailable();
        worker.gotoUrl(startUrl);

        baseScreen screen = new baseScreen(worker);
        Assert.assertTrue(screen.openChapterOf(1));

        String html = worker.asHtmlElement(screen.content, true);
    }
}

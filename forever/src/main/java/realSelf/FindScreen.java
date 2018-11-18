package realSelf;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIObject;
import io.github.cruisoring.Functions;
import org.openqa.selenium.By;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FindScreen extends Screen {
    public static final URL baseUrl = (URL)Functions.Default.apply(() -> new URL("https://www.realself.com/find/"));

//    public static final List<String> places = new ArrayList<>();

    public final UIObject topCities;
    public final UIObject states;
    public final UIObject canadians;
    public final UIObject countries;
    public final UIObject[] categories;

    protected FindScreen(Worker worker) {
        super(worker);

        topCities = new UIObject(this, By.cssSelector("div#directory-cities"), 0);
        states = new UIObject(this, By.cssSelector("div#directory-states"), 0);
        canadians = new UIObject(this, By.cssSelector("div#directory-states"), 1);
        countries = new UIObject(this, By.cssSelector("div#directory-states"), 2);
        categories = new UIObject[] { topCities, states, canadians, countries};
    }

    public List<String> findPlaces(){
        List<String> links = new ArrayList<>();
        for (UIObject category : categories) {
            String html = category.getOuterHTML();
            List<String> segments = StringExtensions.getSegments(html, StringExtensions.SimpleListItemPattern);
            for (String segment : segments) {
                String place = StringExtensions.extractHtmlText(segment);
                String href = StringExtensions.valueOfAttribute(segment, "href");
                String url = StringExtensions.getUrl(baseUrl, href);
                Logger.D("%s: %s", place, url);
                links.add(url);
            }
        }
        return findPlaces();
    }
}

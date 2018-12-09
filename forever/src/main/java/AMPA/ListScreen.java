package AMPA;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.URLHelper;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UINavigator;
import org.openqa.selenium.By;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ListScreen extends Screen {
    public static final Pattern namePattern = StringExtensions.LinkPattern;
    public static final Pattern locationPattern = Pattern.compile("<div class=\"event-location[\\s\\S]*?</div>");

    public final UICollection list;
    public final UINavigator navigator;

    protected ListScreen(Worker worker) {
        super(worker);
        list = new UICollection(this, By.cssSelector("div.tab-pane>ul"), By.cssSelector("div.event-info a"));
        navigator = new UINavigator(this, By.cssSelector("ul.pagination"), 0);
    }

    public Map<String, String> getSchoolAddresses(){
        String html = list.getOuterHTML();
        List<String> names = StringExtensions.getSegments(html, namePattern);
        names = names.stream()
                .map(element -> StringExtensions.extractHtmlText(element).trim()).collect(Collectors.toList());
        List<String> locations  = StringExtensions.getSegments(html, locationPattern);
        locations = locations.stream()
                .map(element -> StringExtensions.extractHtmlText(element).trim()).collect(Collectors.toList());

        int namesSize = names.size();
        if(namesSize == locations.size()){
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < namesSize; i++) {
                map.put(names.get(i), locations.get(i));
            }
            return map;
        } else {
            return null;
        }
    }

    public Map<String, String> getAllSchoolAddresses(){
        Map<String, String> map = getSchoolAddresses();
        while (!navigator.isLastPage()){
            navigator.goPageOrNext();
            map.putAll(getSchoolAddresses());
        }
        return map;
    }

    public List<String> getDisplayedLinks(URL baseUrl){
        String html = list.getOuterHTML();
        List<String> segments = StringExtensions.getSegments(html, StringExtensions.LinkPattern);
        List<String> hrefs = segments.stream()
                .map(element -> StringExtensions.valueOfAttribute(element, "href"))
                .collect(Collectors.toList());
        List<String> links = hrefs.stream()
                .map(href -> URLHelper.getLink(baseUrl, href))
                .collect(Collectors.toList());
        Logger.D(String.join("\r\n", links));
        return links;
    }

    public List<String> getAllLinks(URL baseUrl){
        List<String> allLinks = getDisplayedLinks(baseUrl);
        while (!navigator.isLastPage()){
            navigator.goPageOrNext();
            allLinks.addAll(getDisplayedLinks(baseUrl));
        }
        return allLinks;
    }
}

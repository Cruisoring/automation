package io.github.Cruisoring.screens;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.*;
import org.openqa.selenium.By;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ListScreen extends Screen {
    private static final Map<String, By> locators = new HashMap<String, By>(){{
        put("Nexts", By.cssSelector("i.fa-chevron-right"));
        put("Previouses", By.cssSelector("i.fa-chevron-left"));
    }};

//    public final Navigator navigator;
    public final UIObject table;
    public final UINavigator navigator;
//    public final UILink.Collection links;

    public ListScreen(Worker worker) {
        super(worker);
//        navigator = new Navigator(this);
        table = new UIObject(this, By.cssSelector("table.enf-list-table "));
        navigator = new UINavigator(this, By.cssSelector("nav"), 0, By.cssSelector("li"), locators);
//        links = new UILink.Collection(table, By.cssSelector("a[href]"));
    }

    Pattern linkPattern = Pattern.compile("(?:<tr>\\s+<td>\\s+)<(a)\\b[^>]*>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>", Pattern.MULTILINE);
    public List<String> getLinks(URL baseUrl){
        List<String> elements = StringExtensions.getSegments(table.getOuterHTML(), linkPattern);
        List<String> hrefs = elements.stream()
                .map(e -> StringExtensions.valueOfAttribute(e, "href"))
                .filter(href -> href != null)
                .collect(Collectors.toList());

        List<String> urls = hrefs.stream()
                .map(href -> StringExtensions.getUrl(baseUrl, href)).collect(Collectors.toList());
        urls.forEach(Logger::I);
        return urls;
    }

    public List<String> getAllLinks(URL baseUrl){
        List<String> links = getLinks(baseUrl);
        while (!navigator.isLastPage()) {
            navigator.goPageOrNext();
            links.addAll(getLinks(baseUrl));
        }
        return links;

    }
}


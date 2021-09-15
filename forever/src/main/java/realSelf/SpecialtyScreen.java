package realSelf;

import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpecialtyScreen extends Screen {
    public static final Pattern plasticSurgeonPattern = Pattern.compile("<a [^>]*?Plastic-Surgeon[^>]*?>[^<]*</a>", Pattern.MULTILINE);

    public final UIObject speciaties;

    protected SpecialtyScreen(Worker worker) {
        super(worker);

        speciaties = new UIObject(this, By.cssSelector("ul.directory-list"), 0);
    }

    List<String> getListLinks(){
        String html = speciaties.getOuterHTML();
        List<String> elements = StringExtensions.getSegments(html, plasticSurgeonPattern);

        List<String> links = elements.stream()
                .map(e -> StringExtensions.valueOfAttribute(e, "href"))
                .map(href -> StringExtensions.getUrl(FindScreen.baseUrl, href))
                .collect(Collectors.toList());
        return links;
    }
}

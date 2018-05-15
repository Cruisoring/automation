package components;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class ResultItem extends UIObject {
    public static final By resultItemBy = By.cssSelector("div.g");

    public final UIObject resultTitle;
    public final UIObject address;
    public final UIObject actions;
    public final UIObject details;
    public final UIObject publishDate;
    public final UIObject description;

    public ResultItem(WorkingContext context, By by, Integer index) {
        super(context, by, index);
        resultTitle = new UIObject(this, By.cssSelector("h3 a"));
        address = new UIObject(this, By.cssSelector("cite"));
        actions = new UIObject(this, By.cssSelector("div.action-menu"));
        details = new UIObject(this, By.cssSelector("h3+div"));
        publishDate = new UIObject(details, By.cssSelector("span>span"));
        description = new UIObject(details, By.cssSelector("div[style$='nowrap']+span"));
    }

    public String getUrl(){
        return resultTitle.getAttribute("href");
    }

    public boolean isMatched(String expectedUrl, String... expectedTitleKeys) {
        String title = resultTitle.getAllText();
        String url = getUrl();
        if (url.length() > 80)
            url = url.substring(0, 80);

        Logger.D("%s: %s", title, url);

        return StringExtensions.containsAnyIgnoreCase(title, expectedTitleKeys) && StringExtensions.containsAllIgnoreCase(url, expectedUrl);
    }
}

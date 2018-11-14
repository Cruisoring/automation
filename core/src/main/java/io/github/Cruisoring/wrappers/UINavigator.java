package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.cruisoring.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UINavigator extends UIObject {
    public static final By defaultPageBy = By.cssSelector("li");
    public static final Pattern defaultPagePattern = Pattern.compile("<(li)\\b[^>]*>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>");

    public static final String[] Active = new String[]{"active", "current"};
    public static final String[] First = new String[]{"first"};
    public static final String[] Last = new String[]{"last"};
    public static final String[] Previous = new String[]{"previous", "prev"};
    public static final String[] Next = new String[]{"next"};

    private static final Map<String, String[]> defaultPagePatterns = new HashMap<String, String[]>(){{
        put(StringUtils.join(Active, "|"), Active);
        put(StringUtils.join(First, "|"), First);
        put(StringUtils.join(Last, "|"), Last);
        put(StringUtils.join(Previous, "|"), Previous);
        put(StringUtils.join(Next, "|"), Next);
    }};

    private static By getLocator(By pageBy, String key){
        String t = pageBy.toString();
        final String pageLabel = t.substring(t.indexOf(":")+2);
        String matched = defaultPagePatterns.keySet().stream()
                .filter(k -> StringUtils.containsIgnoreCase(k, key))
                .findFirst().orElse(null);
        if(matched == null){
            return By.cssSelector(pageLabel + "." + key);
        }

        String locator = Arrays.stream(Active)
                .map(clz -> pageLabel + "." + clz)
                .reduce((s1, s2) -> s1 + ", " + s2).orElse("");
        return By.cssSelector(locator);
    }

    private final By pageBy;
    private final Pattern pagePattern;
    private final Lazy<UICollection> pages;
    private final Lazy<UIObject> currentPage, firstPage, previousPage, nextPage, lastPage;

    public UINavigator(WorkingContext context, By by, Integer index, By pageBy, Map<String, By> pagelocators){
        super(context, by, index);

        this.pageBy = pageBy == null ? defaultPageBy : pageBy;
        this.pagePattern = defaultPagePattern;
        pages = new Lazy<UICollection>(() -> new UICollection(context, by, this.pageBy));

        currentPage = new Lazy<UIObject>(()-> new UIObject(context, pagelocators.containsKey(Active[0])? pagelocators.get(Active[0]) : getLocator(pageBy, Active[0])));
        firstPage = new Lazy<UIObject>(()-> new UIObject(context, pagelocators.containsKey(First[0])? pagelocators.get(First[0]) : getLocator(pageBy, First[0])));
        lastPage = new Lazy<UIObject>(()-> new UIObject(context, pagelocators.containsKey(Last[0])? pagelocators.get(Last[0]) : getLocator(pageBy, Last[0])));
        nextPage = new Lazy<UIObject>(()-> new UIObject(context, pagelocators.containsKey(Next[0])? pagelocators.get(Next[0]) : getLocator(pageBy, Next[0])));
        previousPage = new Lazy<UIObject>(()-> new UIObject(context, pagelocators.containsKey(Previous[0])? pagelocators.get(Previous[0]) : getLocator(pageBy, Previous[0])));
    }

    public UINavigator(WorkingContext context, By by, Integer index, By pageBy, Pattern pagePattern){
        super(context, by, index);

        this.pageBy = pageBy == null ? defaultPageBy : pageBy;
        this.pagePattern = pagePattern == null ? defaultPagePattern : pagePattern;

        pages = new Lazy<UICollection>(() -> new UICollection(context, by, this.pageBy));

        currentPage = pages.create(container -> new UIObject(container, getLocator(pageBy, Active[0])));
        firstPage = pages.create(container -> container.get(this.pagePattern, "First"));
        lastPage = pages.create(container -> container.get(this.pagePattern,  "Last"));
        nextPage = pages.create(container -> container.get(this.pagePattern,  "Next"));
        previousPage = pages.create(container -> container.get(this.pagePattern,  "Prev"));
    }

    public UINavigator(WorkingContext context, By by, Integer index) {
        this(context, by, index, defaultPageBy, defaultPagePattern);
    }

    public UINavigator(WorkingContext context, By by) {
        this(context, by, null);
    }

    public boolean isFirstPage(){
        UIObject first = firstPage.getValue();
        return (first != null ? first : previousPage.getValue()).isDisabled();
    }

    public boolean isLastPage(){
        UIObject last = lastPage.getValue();
        return (last != null ? last : nextPage.getValue()).isDisabled();
    }

    public boolean goNext(){
        UIObject next = nextPage.getValue();

        if(next.isVisible() && !next.isDisabled()){
            next.click();
            pages.closing();
            nextPage.closing();
            return true;
        }
        return false;
    }

    public boolean goPrevious(){
        UIObject previous = previousPage.getValue();

        if(previous.isVisible() && !previous.isDisabled()){
            previous.click();
            pages.closing();
            nextPage.closing();
            return true;
        }
        return false;
    }

    public boolean goLast(){
        UIObject last = lastPage.getValue();
        last.click();
        pages.closing();
        lastPage.closing();

        return isLastPage();
    }
}

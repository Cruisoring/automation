package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.MapHelper;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.utility.ArrayHelper;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class UINavigator extends UICollection {
    public static final By defaultPageBy = By.cssSelector("li");
    public static final Pattern defaultPagePattern = Pattern.compile("<(li)\\b[^>]*>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>");
    public static int DefaultAfterClickingWaitMills = 5000;

    public static final List<String> Actives = Arrays.asList("active", "current", "strong");
    public static final List<String> Firsts = Arrays.asList("first", "<<", "1");
    public static final List<String> Lasts = Arrays.asList("last", ">>");
    public static final List<String> Previouses = Arrays.asList("previous", "prev", "back", "<", "Anterior", "上一页");
    public static final List<String> Nexts = Arrays.asList("next", "forward", ">", "Siguiente", "下一页");

    private final By pageBy;
    private final Lazy<UIObject> currentPage, firstPage, previousPage, nextPage, lastPage;

    public UINavigator(WorkingContext context, By by, Integer index, By pageBy, Map<String, By> pagelocators){
        super(context, by, index, pageBy);

        this.pageBy = pageBy == null ? defaultPageBy : pageBy;

        currentPage = new Lazy<UIObject>(() -> findChildWithClues(pagelocators, Actives));
        firstPage = new Lazy<UIObject>(() -> findChildWithClues(pagelocators, Firsts));
        lastPage = new Lazy<UIObject>(() -> findChildWithClues(pagelocators, Lasts));
        nextPage = new Lazy<UIObject>(() -> findChildWithClues(pagelocators, Nexts));
        previousPage = new Lazy<UIObject>(() -> findChildWithClues(pagelocators, Previouses));
    }

    public UINavigator(WorkingContext context, By by, Integer index, By pageBy) {
        super(context, by, index, pageBy);

        this.pageBy = pageBy == null ? defaultPageBy : pageBy;

        currentPage = new Lazy<UIObject>(() -> findByConventions(Actives));
        firstPage = new Lazy<UIObject>(() -> findByConventions(Firsts));
        lastPage = new Lazy<UIObject>(() -> findByConventions(Lasts));
        nextPage = new Lazy<UIObject>(() -> findByConventions(Nexts));
        previousPage = new Lazy<UIObject>(() -> findByConventions(Previouses));
    }

    @Override
    public void invalidate(){
        super.invalidate();
        currentPage.closing();
        firstPage.closing();
        lastPage.closing();
        nextPage.closing();
        previousPage.closing();
    }

    public UINavigator(WorkingContext context, By by, By pageBy){
        this(context, by, 0, pageBy);
    }

    public UINavigator(WorkingContext context, By by) {
        this(context, by, null);
    }

    public UIObject findChildWithClues(Map<String, By> locators, List<String> keys){
        String key = MapHelper.bestMatchedKey(locators.keySet(), keys);
        if(key != null){
            return new UIObject(this, pageBy, keys.indexOf(key));
        }
        return null;
    }

    public UIObject findByConventions(List<String> keys){
        List<String> values = asTexts();
        String matched = MapHelper.bestMatchedKey(values, keys);

        if(matched != null){
            Logger.D("Page with text of '%s' is found at %d", matched, values.indexOf(matched));
            return get(values.indexOf(matched));
        }

        values = valuesOf(c -> ((UIObject)c).getAttribute("title"));
        matched = MapHelper.bestMatchedKey(values, keys);
        if(matched != null){
            Logger.D("Page with class of '%s' is found at %d", matched, values.indexOf(matched));
            return  get(values.indexOf(matched));
        }

        values = asClasses();
        matched = MapHelper.bestMatchedKey(values, keys);
        if(matched != null){
            Logger.D("Page with class of '%s' is found at %d", matched, values.indexOf(matched));
            return  get(values.indexOf(matched));
        }
        return null;
    }

    public boolean isCurrentPage(String... pageNames){
        if(pageNames.length == 0)
            return true;

        List<String> pageTexts = asTexts();
        List<String> keys = Arrays.asList(pageNames);
        String matched = MapHelper.bestMatchedKey(pageTexts, keys);
        if(matched != null){
            return get(pageTexts.indexOf(matched)).isDisabled();
        }
        return false;
    }

    public boolean isFirstPage(String... pageNames){
        UIObject first = firstPage.getValue();
        return first != null && first.isDisabled();
    }

    public boolean isLastPage(String... pageNames){
        UIObject last = getLast();
        return last.isDisabled();
    }

    public boolean gotoPage(String... pageNames){
        if(pageNames.length > 0) {
            List<String> pageTexts = asTexts();
            List<String> keys = Arrays.asList(pageNames);
            String matched = MapHelper.bestMatchedKey(pageTexts, keys);
            if(matched != null){
                get(pageTexts.indexOf(matched)).click(DefaultAfterClickingWaitMills);
                invalidate();
                return true;
            }
        }
        return false;
    }

    public boolean gotoPage(UIObject page){
        if (page != null && page.isVisible() && !page.isDisabled()) {
            page.click(DefaultAfterClickingWaitMills);
            invalidate();
            return true;
        }
        return false;
    }

    public boolean gotoPageOrNext(String... pageNames){
        return gotoPage(pageNames)
                || gotoPage(nextPage.getValue()) || gotoPage(Nexts.toArray(new String[0]));
    }

    public boolean gotoPageOrPrevious(String... pageNames){
        return gotoPage(pageNames)
                || gotoPage(previousPage.getValue()) || gotoPage(Previouses.toArray(new String[0]));
    }

    public boolean goPageOrLast(String... pageNames){
        return gotoPage(pageNames)
                || gotoPage(lastPage.getValue()) || gotoPage(Lasts.toArray(new String[0]));
    }

    public boolean goPageOrFirst(String... pageNames){
        return gotoPage(pageNames)
                || gotoPage(firstPage.getValue()) || gotoPage(Firsts.toArray(new String[0]));
    }
}

//public class UINavigator extends UIObject {
//    public static final By defaultPageBy = By.cssSelector("li");
//    public static final Pattern defaultPagePattern = Pattern.compile("<(li)\\b[^>]*>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>");
//
//    public static final List<String> Actives = Arrays.asList("active", "current", "strong");
//    public static final List<String> Firsts = Arrays.asList("first", "<<", "1");
//    public static final List<String> Lasts = Arrays.asList("last", ">>");
//    public static final List<String> Previouses = Arrays.asList("previous", "prev", "back", "<", "Anterior", "上一页");
//    public static final List<String> Nexts = Arrays.asList("next", "forward", ">", "Siguiente", "下一页");
//
//    private final By pageBy;
//    private final Lazy<UICollection> pages;
//    private final Lazy<List<String>> pageTexts;
//    private final Lazy<List<String>> pageClasses;
//    private final Lazy<List<String>> pageTitles;
//    private final Lazy<UIObject> currentPage, firstPage, previousPage, nextPage, lastPage;
//
//    public UINavigator(WorkingContext context, By by, Integer index, By pageBy, Map<String, By> pagelocators){
//        super(context, by, index);
//
//        this.pageBy = pageBy == null ? defaultPageBy : pageBy;
//        pages = new Lazy<UICollection>(() -> new UICollection(context, by, this.pageBy));
//        pageTexts = pages.create(UICollection::asTexts);
//        pageTitles = pages.create(all -> all.valuesOf(control -> ((UIObject)control).getAttribute("title")));
//        pageClasses = pages.create(UICollection::asClasses);
//
//        currentPage = pages.create(list -> findChildWithClues(pagelocators, Actives));
//        firstPage = pages.create(list -> findChildWithClues(pagelocators, Firsts));
//        lastPage = pages.create(list -> findChildWithClues(pagelocators, Lasts));
//        nextPage = pages.create(list -> findChildWithClues(pagelocators, Nexts));
//        previousPage = pages.create(list -> findChildWithClues(pagelocators, Previouses));
//    }
//
//    public UINavigator(WorkingContext context, By by, Integer index, By pageBy) {
//        super(context, by, index);
//
//        this.pageBy = pageBy == null ? defaultPageBy : pageBy;
//        pages = new Lazy<UICollection>(() -> new UICollection(context, by, this.pageBy));
//        pageTexts = pages.create(UICollection::asTexts);
//        pageTitles = pages.create(all -> all.valuesOf(control -> ((UIObject)control).getAttribute("title")));
//        pageClasses = pages.create(UICollection::asClasses);
//
//        currentPage = pages.create(list -> findByConventions(Actives));
//        firstPage = pages.create(list -> findByConventions(Firsts));
//        lastPage = pages.create(list -> findByConventions(Lasts));
//        nextPage = pages.create(list -> findByConventions(Nexts));
//        previousPage = pages.create(list -> findByConventions(Previouses));
//    }
//
//    public UINavigator(WorkingContext context, By by, By pageBy){
//        this(context, by, 0, pageBy);
//    }
//
//    public UINavigator(WorkingContext context, By by) {
//        this(context, by, null);
//    }
//
//    public UIObject findChildWithClues(Map<String, By> locators, List<String> keys){
//        String key = MapHelper.bestMatchedKey(locators.keySet(), keys);
//        if(key != null){
//            return new UIObject(pages.getValue(), pageBy, keys.indexOf(key));
//        }
//        return null;
//    }
//
//    public UIObject findByConventions(List<String> keys){
//        String matched = MapHelper.bestMatchedKey(pageTexts.getValue(), keys);
//
//        if(matched != null){
//            List<String> texts = pageTexts.getValue();
//            Logger.D("Page with text of '%s' is found at %d", matched, texts.indexOf(matched));
//            return  pages.getValue().get(texts.indexOf(matched));
//        }
//
//        matched = MapHelper.bestMatchedKey(pageTitles.getValue(), keys);
//        if(matched != null){
//            List<String> titles = pageTitles.getValue();
//            Logger.D("Page with class of '%s' is found at %d", matched, titles.indexOf(matched));
//            return  pages.getValue().get(titles.indexOf(matched));
//        }
//
//        matched = MapHelper.bestMatchedKey(pageClasses.getValue(), keys);
//        if(matched != null){
//            List<String> classes = pageClasses.getValue();
//            Logger.D("Page with class of '%s' is found at %d", matched, classes.indexOf(matched));
//            return  pages.getValue().get(classes.indexOf(matched));
//        }
//        return null;
//    }
//
//
//    public boolean isFirstPage(){
//        UIObject first = firstPage.getValue();
//        if(first != null){
//            return first.isDisabled();
//        }
//
//        return pages.getValue().getFirst().isDisabled();
//    }
//
//    public boolean isLastPage(){
//        UIObject next = nextPage.getValue();
//        if(next != null){
//            return next.isDisabled();
//        }
//        UICollection allPages = pages.getValue();
//        return allPages.getLast().isDisabled();
//    }
//
//    public boolean goNext(String pageNumber){
//        UICollection allPages = pages.getValue();
//        List<String> pageTexts = allPages.asTexts();
//        int index = pageTexts.indexOf(pageNumber);
//        if(index != -1){
//            allPages.get(index).click(-1);
//            pages.closing();
//        } else {
//            UIObject next = nextPage.getValue();
//            if (next.isVisible() && !next.isDisabled()) {
//                next.click(3000);
//                pages.closing();
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean goPrevious(){
//        UIObject previous = previousPage.getValue();
//
//        if(previous.isVisible() && !previous.isDisabled()){
//            previous.click(3000);
//            pages.closing();
//            return true;
//        }
//        return false;
//    }
//
//    public boolean goLast(){
//        UIObject last = lastPage.getValue();
//        last.click(3000);
//        pages.closing();
//
//        return isLastPage();
//    }
//}

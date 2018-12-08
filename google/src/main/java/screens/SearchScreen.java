package screens;

import components.ResultItem;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.workers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIEdit;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class SearchScreen extends Screen {
    public final UIEdit search;
    public final UIObject submit;

    public final UIObject searchDiv;
    public final UICollection<ResultItem> searchResults;

    public final UIObject navigation;
    public final UIObject previous;
    public final UIObject next;
    public final UICollection<UIObject> pages;
    public final UIObject current;

    public SearchScreen(Worker worker){
        super(worker);
        search = new UIEdit(this, By.cssSelector("input[title='Search']"));
        submit = new UIObject(this, By.cssSelector("inpt[type='submit']"));
        searchDiv = new UIObject(this, By.cssSelector("div#search"));
        searchResults = new UICollection<ResultItem>(searchDiv, By.cssSelector("div.srg"), 0, ResultItem.class, ResultItem.resultItemBy);

        navigation = new UIObject(this, By.cssSelector("div#foot"));
        previous = new UIObject(navigation, By.cssSelector("td.navend>a#pnprev"));
        next = new UIObject(navigation, By.cssSelector("td.navend>a#pnnext"));
        pages = new UICollection<UIObject>(navigation, By.cssSelector("td"));
        current = new UIObject(navigation, By.cssSelector("td.cur"));
    }

//    public void search(String keyword){
//        search.enterText(keyword);
//        submit.clickByScript();
//        search.waitPageReady();
//    }

    @Override
    public void invalidate(){
        super.invalidate();
        searchResults.invalidate();
        pages.invalidate();
        next.invalidate();
    }


//
//    public boolean openResultPage(final String[] expectedTitleKeys, String expectedUrl){
//        try {
//            invalidate();
//            waitPageReady();
//            ResultItem matched = searchResults.get(
//                    result -> result.isMatched(expectedUrl, expectedTitleKeys));
//            if (matched == null)
//                return false;
//            matched.resultTitle.clickByScript();
//            invalidate();
//            return matched.waitGone();
//        }catch (Exception ex){
//            return false;
//        }
//    }

    public boolean hasMorePage(){
        return next.isDisplayed();
    }
}

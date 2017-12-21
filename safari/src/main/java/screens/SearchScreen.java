package screens;

import com.least.automation.helpers.StringExtensions;
import com.least.automation.helpers.Worker;
import com.least.automation.wrappers.Screen;
import com.least.automation.wrappers.UICollection;
import com.least.automation.wrappers.UIEdit;
import com.least.automation.wrappers.UIObject;
import components.resultItem;
import org.openqa.selenium.By;

public class SearchScreen extends Screen {
    public final By resultItemContainerBy = By.cssSelector("div#js-results-region");
    public final UIEdit search;
    public final UIObject searchButton;
    public final UICollection<resultItem> resultItems;

    protected SearchScreen(Worker worker){
        super(worker);
        search = new UIEdit(this, By.cssSelector("input.search-query"));
        searchButton = new UIObject(this, By.cssSelector("button.search-button"));

        resultItems = new UICollection<resultItem>(this, resultItemContainerBy, 0, resultItem.class, resultItem.resultItemBy);
    }


    public String searchBook(String bookname){
        waitScreenVisible();
        search.enterByScript(bookname);
        searchButton.click(3*1000);

        resultItem firstResult = resultItems.getChildren().get(0);
        firstResult.waitPageReady();
        String bookTitle = firstResult.bookTitle.getAllText().trim();
        if(!StringExtensions.containsAllIgnoreCase(bookTitle, bookname)){
            return null;
        }
        firstResult.bookTitle.click();
        firstResult.waitGone();
        return bookTitle;
    }
}

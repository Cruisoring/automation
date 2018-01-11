package screens;

import com.least.automation.helpers.Logger;
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
        final String[] keys = bookname.split(" ");
        resultItem firstResult = resultItems.get(0);
        waitScreenVisible();
        search.enterText(bookname);

        firstResult.waitChanges(o -> o.getAllText());
        firstResult = resultItems.get(r -> r.bookTitle.getTextContent().trim(), t -> t.equalsIgnoreCase(bookname));
        if(firstResult == null)
            firstResult = resultItems.get( r -> StringExtensions.containsAllIgnoreCase(r.bookTitle.getAllText(), keys));
        String bookTitle = firstResult.bookTitle.getAllText().trim();
        Logger.I("book found: " + bookTitle);

        if(!StringExtensions.containsAllIgnoreCase(bookTitle, keys)){
            return null;
        }
        bookTitle = StringExtensions.removeAllCharacters(bookTitle, StringExtensions.WindowsSpecialCharacters)
                .replaceAll("\\s+", " ");
        firstResult.bookTitle.click();
        firstResult.waitGone();
        return bookTitle;
    }
}

package screens;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIEdit;
import io.github.Cruisoring.wrappers.UIObject;
import components.resultItem;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

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
        resultItem firstResult = resultItems.getChildren().get(0);
        waitScreenVisible();
        search.enterByScript(bookname);
        if(searchButton.isVisible())
            searchButton.click();
        else
            search.getElement().sendKeys(Keys.RETURN);

        firstResult.waitChanges(o -> o.getAllText());
        String bookTitle = firstResult.bookTitle.getAllText().trim();
        Logger.I("book found: " + bookTitle);

        if(!StringExtensions.containsAllIgnoreCase(bookTitle, bookname.split(" "))){
            return null;
        }
        bookTitle = StringExtensions.removeAllCharacters(bookTitle, StringExtensions.WindowsSpecialCharacters)
                .replaceAll("\\s+", " ");
        firstResult.bookTitle.click();
        firstResult.waitGone();
        return bookTitle;
    }
}

package screens;

import com.least.automation.wrappers.Screen;
import com.least.automation.wrappers.UICollection;
import com.least.automation.wrappers.UIObject;
import components.bookFull;
import components.topBars;
import org.openqa.selenium.By;

public class baseScreen extends Screen {
    public static final By contentBy = By.cssSelector("div#book-markup-container");
    public static final By titleBy = By.cssSelector("h1");

    public final topBars bars;
    public final bookFull book;
    public final UICollection<UIObject> chapters;
    public final UIObject content;
    public final UIObject contentTitle;

    public baseScreen(){
        super();
        bars = new topBars(this);
        book = new bookFull(this);
        chapters = book.chapters;
        content = new UIObject(this, contentBy);
        contentTitle = new UIObject(content, titleBy);
    }

    public boolean openChapterOf(int chapterNumber){
        if(waitAjaxDone() && contentTitle.getAttribute("id").endsWith(String.format("%02d", chapterNumber)))
            return true;

        if(!chapters.isDisplayed()){
            bars.leftButtons.get(topBars.contentIndex).click();
            if(!chapters.waitDisplayed(1000))
                return false;
            UIObject chapterLink = chapters.get(String.format("Chapter %d", chapterNumber));
            if (chapterLink == null)
                return false;
            chapterLink.click();
        }

        return contentTitle.waitDisplayed() && contentTitle.getAttribute("id").endsWith(String.format("%02d", chapterNumber));
    }
}

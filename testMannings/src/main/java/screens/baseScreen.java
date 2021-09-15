package screens;

import components.bookFull;
import components.topBars;
import io.github.Cruisoring.workers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class baseScreen extends Screen {
    public static final By contentBy = By.cssSelector("div#book-markup-container");
    public static final By titleBy = By.cssSelector("h1");

    public final topBars bars;
    public final bookFull book;
    public final UIObject.Collection chapters;
    public final UIObject content;
    public final UIObject contentTitle;

    public baseScreen(Worker worker){
        super(worker);
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

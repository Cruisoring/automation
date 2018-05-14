package components;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class bookFull {
    public static final By bookFullBy = By.cssSelector("div#toc-menu ul.book-full");
    public static final By bookChapterBy = By.cssSelector("li.book-chapter");

    public final UIObject.Collection chapters;

    public bookFull(WorkingContext context){
        chapters = new UIObject.Collection(context, bookFullBy, 0, bookChapterBy);
    }
}

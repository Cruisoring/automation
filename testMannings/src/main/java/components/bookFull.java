package components;

import com.least.automation.interfaces.WorkingContext;
import com.least.automation.wrappers.UICollection;
import com.least.automation.wrappers.UIObject;
import org.openqa.selenium.By;

public class bookFull {
    public static final By bookFullBy = By.cssSelector("div#toc-menu ul.book-full");
    public static final By bookChapterBy = By.cssSelector("li.book-chapter");

    public final UICollection<UIObject> chapters;

    public bookFull(WorkingContext context){
        chapters = new UICollection<UIObject>(context, bookFullBy, 0, bookChapterBy);
    }
}

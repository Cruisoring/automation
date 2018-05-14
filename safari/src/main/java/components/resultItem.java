package components;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class resultItem extends UIObject {
    public static final By resultItemBy = By.cssSelector("li.result-item");

    public final UIObject bookTitle;
//    public final UICollection<UIObject> metas;
    public final UIObject.Collection metas;
    public final UIObject author;
    public final UIObject publisher;
    public final UIObject publishDate;
    public final UIObject description;

    public resultItem(WorkingContext context, By by, Integer index) {
        super(context, by, index);
        bookTitle = new UIObject(this, By.cssSelector("div.book-title>a"));
        metas = new UIObject.Collection(this, By.cssSelector("div.chapter-meta"), By.cssSelector("span"));
        author = metas.get(0);
        publisher = metas.get(1);
        publishDate = metas.get(2);
        description = new UIObject(this, By.cssSelector("span.description"));
    }

    @Override
    public void invalidate(){
        super.invalidate();
        bookTitle.invalidate();
    }
}

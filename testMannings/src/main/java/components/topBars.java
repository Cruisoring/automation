package components;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class topBars {
    public final static By leftSectionBy = By.cssSelector("div#top-bar-container section.pull-left");
    public final static By rightSectionBy = By.cssSelector("div#top-bar-container section.pull-right");
    public final static By liBy = By.cssSelector("ul>li");

    public static final int libraryIndex = 2;
    public static final int contentIndex = 3;

    public final UIObject.Collection leftButtons;
    public final UIObject.Collection rightButtons;

    public topBars(WorkingContext context){
        leftButtons = new UIObject.Collection(context, leftSectionBy, 0, liBy);
        rightButtons = new UIObject.Collection(context, rightSectionBy, 0, liBy);
    }
}

package components;

import com.least.automation.interfaces.WorkingContext;
import com.least.automation.wrappers.UICollection;
import com.least.automation.wrappers.UIObject;
import org.openqa.selenium.By;

public class topBars {
    public final static By leftSectionBy = By.cssSelector("div#top-bar-container section.pull-left");
    public final static By rightSectionBy = By.cssSelector("div#top-bar-container section.pull-right");
    public final static By liBy = By.cssSelector("ul>li");

    public static final int libraryIndex = 2;
    public static final int contentIndex = 3;

    public final UICollection<UIObject> leftButtons;
    public final UICollection<UIObject> rightButtons;

    public topBars(WorkingContext context){
        leftButtons = new UICollection<UIObject>(context, leftSectionBy, 0, liBy);
        rightButtons = new UICollection<UIObject>(context, rightSectionBy, 0, liBy);
    }
}

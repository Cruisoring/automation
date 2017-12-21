package screens;

import com.least.automation.helpers.Worker;
import com.least.automation.wrappers.Screen;
import com.least.automation.wrappers.UIEdit;
import com.least.automation.wrappers.UIObject;
import org.openqa.selenium.By;

public class HomeScreen extends Screen {
    public final UIEdit search;
    public final UIObject searchButton;

    public HomeScreen(Worker worker){
        super(worker);
        search = new UIEdit(this, By.cssSelector("input.search-query"));
        searchButton = new UIObject(this, By.cssSelector("button.search-button"));
    }

}

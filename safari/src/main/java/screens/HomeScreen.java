package screens;

import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UIEdit;
import io.github.Cruisoring.wrappers.UIObject;
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

package io.github.Cruisoring.screens;

import io.github.Cruisoring.components.Navigator;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import org.openqa.selenium.By;

public class BaseScreen extends Screen {

    protected final Navigator navigator;
    public BaseScreen(Worker worker){
        this(worker, By.cssSelector("div.c_container"));
    }

    public BaseScreen(Worker worker, By rootBy){
        super(worker, rootBy);

        navigator = new Navigator(worker);
    }

    public void openMenu(String menuKeyword, String subMenuKeyword){
        navigator.openMenu(menuKeyword, subMenuKeyword);
    }

    public void openMenu(String menuKeyword){
        navigator.openMenu(menuKeyword);
    }

}

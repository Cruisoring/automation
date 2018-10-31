package io.github.Cruisoring.screens;

import io.github.Cruisoring.components.Navigator;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;

public class BaseScreen extends Screen {

    protected final Navigator navigator;
    public BaseScreen(Worker worker){
        super(worker);

        navigator = new Navigator(worker);
    }


    public void openSubMenu(String menuKey, String subMenuKey){
        navigator.openSubMenu(menuKey, subMenuKey);
    }

}

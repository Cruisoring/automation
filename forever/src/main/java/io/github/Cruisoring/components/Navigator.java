package io.github.Cruisoring.components;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class Navigator extends UIObject {
    public static final By navigatorBy = By.cssSelector("div.nav_primary");

    public final UICollection menus;
    public final UICollection subMenus;
    public Navigator(WorkingContext context) {
        super(context, navigatorBy);
        menus = new UICollection(this, By.cssSelector("div.d_mega_menu"));
        subMenus = new UICollection(this, By.cssSelector("div.d_mega_sub li"));
    }

    public void openMenu(String menuKeyword, String subMenuKeyword){
        UIObject menu = menus.get(menuKeyword);
        menu.mouseOver();

        UIObject sub = subMenus.get(subMenuKeyword);
        sub.click();
    }

    public void openMenu(String menuKeyword){
        UIObject menu = menus.get(menuKeyword);
        menu.click();
    }
}

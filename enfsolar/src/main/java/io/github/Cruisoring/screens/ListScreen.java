package io.github.Cruisoring.screens;

import io.github.Cruisoring.components.Navigator;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UILink;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.util.List;

public class ListScreen extends Screen {
    public final Navigator navigator;
    public final UIObject table;
    public final UILink.Collection links;

    public ListScreen(Worker worker) {
        super(worker);
        navigator = new Navigator(this);
        table = new UIObject(this, By.cssSelector("table.enf-list-table "));
        links = new UILink.Collection(table, By.cssSelector("a[href]"));
    }
}


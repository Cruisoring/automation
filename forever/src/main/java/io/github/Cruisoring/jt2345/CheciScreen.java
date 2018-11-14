package io.github.Cruisoring.jt2345;

import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UILink;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class CheciScreen extends Screen {
    public final UIObject table;
    public final UILink.Collection all;

    protected CheciScreen(Worker worker) {
        super(worker);
        table = new UIObject(this, By.cssSelector("table"), 0);
        all = new UILink.Collection(table, By.cssSelector("a"));
    }
}

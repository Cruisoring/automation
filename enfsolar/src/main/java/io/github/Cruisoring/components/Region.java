package io.github.Cruisoring.components;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class Region extends UIObject {
    public static final By regionBy = By.cssSelector("li.pull-left");

    public final UICollection<Country> countries;

    public Region(WorkingContext context, By by, Integer index) {
        super(context, by, index);

        countries = new UICollection<Country>(this, By.cssSelector("ul"), 0, Country.class, Country.countryBy);
    }


}

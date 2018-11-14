package io.github.Cruisoring.components;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UILink;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class Country extends UIObject {
    public static final By countryBy = By.cssSelector("div.clearfix");

    public final UILink link;
    public final UIObject name;

    public Country(WorkingContext context, By by, Integer index) {
        super(context, by, index);
        link = new UILink(this);
        name = new UIObject(this, By.cssSelector("div+span"));
    }

    public String getLink(){
        String url = link.getURL();
        Logger.D("URL: %s", url);
        return url;
    }

    public String getName() {
        String text = name.getTextContent();
        if(text == null)
            return null;
        return text.substring(0, text.indexOf('(')-1);
    }

    public int getCount() {
        String text = name.getTextContent();
        int count = text.indexOf('(')+1;
        text = text.substring(count, text.indexOf(')'));
        return Integer.valueOf(text);
    }
}

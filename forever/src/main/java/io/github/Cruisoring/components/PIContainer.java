package io.github.Cruisoring.components;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UILink;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.net.URL;

public class PIContainer extends UIObject {
    public static final By piContainerBy = By.cssSelector("div.pi_container");

    public final UILink link;

    public PIContainer(WorkingContext context, By by, Integer index) {
        super(context, by, index);
        link = new UILink(this);
    }

    public String getLink(){
        String url = link.getURL();
        Logger.D("URL: %s", url);
        return url;
    }
}

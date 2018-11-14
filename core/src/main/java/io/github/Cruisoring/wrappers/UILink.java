package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.interfaces.WorkingContext;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URL;

public class UILink extends UIObject {
    public static final Class UILinkClass = UILink.class;
    public static final By defaultUILinkBy = By.cssSelector("a");

    public static class Collection extends UICollection<UILink> {
        public Collection(WorkingContext context, By containerBy, Integer index, By childrenBy){
            super(context, containerBy, index, UILinkClass, childrenBy);
        }

        public Collection(WorkingContext context, By containerBy, By childrenBy){
            this(context, containerBy, 0, childrenBy);
        }

        public Collection(UIObject context, By childrenBy) {
            this(context.parent, context.locator, null, childrenBy);
        }

        public Collection(UIObject context) {
            this(context.parent, context.locator, null, defaultUILinkBy);
        }
    }


    public UILink(WorkingContext context, By by, Integer index) {
        super(context, by, index);
    }

    public UILink(WorkingContext context, By by) {
        this(context, by, null);
    }

    public UILink(WorkingContext context){
        this(context, defaultUILinkBy, null);
    }

    public String getURL(){
        String href = getAttribute("href");
        URL base = worker.getUrl();
        URL url = null;
        try {
            url = new URL(base, href);
            return url.toString();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}

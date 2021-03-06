package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.interfaces.IUIEdit;
import io.github.Cruisoring.interfaces.WorkingContext;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class UIEdit extends UIObject implements IUIEdit {
    public static final Class UIEditClass = UIEdit.class;
    public static final By defaultEditLocator = By.cssSelector("input");

    public static class Collection extends UICollection<UIEdit> {
        public Collection(WorkingContext context, By containerBy, Integer index, By childrenBy){
            super(context, containerBy, index, UIEditClass, childrenBy);
        }

        public Collection(WorkingContext context, By containerBy, By childrenBy){
            this(context, containerBy, 0, childrenBy);
        }

        public Collection(UIObject context, By childrenBy) {
            this(context.parent, context.locator, null, childrenBy);
        }

        public Collection(UIObject context) {
            this(context.parent, context.locator, null, defaultEditLocator);
        }
    }

    public UIEdit(WorkingContext context, By by, Integer index) {
        super(context, by, index);
    }

    public UIEdit(WorkingContext context, By by) {
        super(context, by);
    }

    public UIEdit(WorkingContext context) {
        super(context, defaultEditLocator);
    }

    @Override
    public String getTextContent() {
        return getAttribute("value");
    }

    @Override
    public void enterByScript(String text) {
//        fireEvent("focus");
        String script = "arguments[0].value='" + text + "';";
        executeScript(script);
//        fireEvent("change");
//        fireEvent("blur");
        Logger.D("'%s' is entered by Script to %s", text, this);
    }

    @Override
    public void enterChars(String text) {
        char[] chars = text.toCharArray();
//        fireEvent("focus");
        getElement().sendKeys(Keys.chord(Keys.CONTROL, "A"));
        for (Character ch : chars) {
            getElement().sendKeys(ch.toString());
        }
//        fireEvent("change");
//        fireEvent("blur");
    }

    @Override
    public void enterText(String text) {
        if(perform(text + "\t")) {
            Logger.D("'%s' is entered to %s", text, this);
        }
    }

    public void sendKeys(String text) {
        WebElement e = getFreshElement();
        e.clear();
        sleep(50);
        e.sendKeys(text);
        sleep(50);
        e.sendKeys(Keys.TAB);
    }

    @Override
    public Boolean perform(String text) {
        return super.perform(new Runnable[]{
                () -> enterByScript(text),
                () -> sendKeys(text),
                () -> enterChars(text),
        }, null, 0);
    }

}

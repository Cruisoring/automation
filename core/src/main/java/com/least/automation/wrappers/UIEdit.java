package com.least.automation.wrappers;

import com.least.automation.helpers.Logger;
import com.least.automation.interfaces.IUIEdit;
import com.least.automation.interfaces.WorkingContext;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class UIEdit extends UIObject implements IUIEdit {

    public static final By defaultEditLocator = By.cssSelector("input");

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
        if(perform(text)) {
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
                () -> sendKeys(text),
                () -> enterByScript(text),
                () -> enterChars(text),
        }, null, 0);
    }

}

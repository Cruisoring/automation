package com.least.automation.interfaces;

import org.openqa.selenium.WebElement;

import java.util.function.BooleanSupplier;

public interface IUIObject extends WorkingContext {
    WebElement getElement();

    Integer getElementsCount();

    WebElement getFreshElement();

    Object executeScript(String script, Object... args);

    Boolean exists();

    void invalidate();

    Boolean isDisplayed();

    String getTextContent();

    String getInnerHTML();

    String getOuterHTML();

    String getTagName();

    String getAllText();

    String getAttribute(String attributeName);

    String getCssValueOf(String propertyName);

    void fireEvent(String eventName);

    void highlight(String highlightScript, String resetScript, int interval, int times);

    void highlight();

    void click();

    boolean click(int ajaxWaitMills);

    void clickByScript();

    /**
     * Scroll the element into the visible area of the browser window:
     * @param alignTo Optional boolean value that indicates the type of the align:
     *                true - the top of the element will be aligned to the top of the visible area of the scrollable ancestor
     *                false - the bottom of the element will be aligned to the bottom of the visible area of the scrollable ancestor.

     */
    void scrollIntoViewByScript(Boolean alignTo);

    /**
     * Scroll the element into the visible area of the browser window, scroll to the top of the element.
     */
    void scrollIntoViewByScript();

    void scrollIntoView();

    void mouseOver();

    void mouseOverByScript();

    void rightClick();

    Boolean perform(Runnable[] actions, BooleanSupplier evaluation, int retry);

    Boolean perform(Runnable action, BooleanSupplier evaluation, int retry);

    Boolean perform(String instruction);
}

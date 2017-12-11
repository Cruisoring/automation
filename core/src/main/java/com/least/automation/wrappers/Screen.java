package com.least.automation.wrappers;

import com.least.automation.helpers.Executor;
import com.least.automation.helpers.Logger;
import com.least.automation.helpers.Worker;
import com.least.automation.helpers.PlayerPool;

import com.least.automation.interfaces.IUIObject;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class Screen implements SearchContext {
    public static final String DefaultRootCssSelector = "body, form, div, span, table";
    public static final Integer DefaultWaitVisibleMills = 10*1000;
    public static final Integer DefaultWaitGoneMills = 5* 1000;
    public static final By defaultByOfRoot = By.cssSelector(DefaultRootCssSelector);

    public static HashMap<String,HashMap<String,IUIObject>> objectsMapsByName;
    protected static HashMap<Class,Screen> AllScreens;

    private final Worker worker;

    public Worker getWorker() {
        return worker;
    }

    protected final UIObject root;

    public WebElement getRootElement() {
        return root.getElement();
    }

    public WebElement getFreshRootElement() {
        return root.getFreshElement();
    }

    protected final SearchContext parent;
    //protected final By rootLocator;
    public final String framePath;

    protected BooleanSupplier visiblePredicate;
    protected final BooleanSupplier gonePredicate;

    public Screen(SearchContext parent, String framePath, By rootBy, BooleanSupplier isVisible) {
       this.worker = PlayerPool.getPlayer();
       this.parent = parent;
       this.framePath = Worker.mergeFramePath(parent==null?"":parent.framePath, framePath);
       this.root = new UIObject(parent==null? worker : parent, rootBy == null ? defaultByOfRoot : rootBy, null);
       this.visiblePredicate = isVisible == null ? ()->getVisibilityByConvention() : isVisible;
       this.gonePredicate = isVisible == null ? ()->!getVisibilityByConvention() : () -> !isVisible.getAsBoolean();
    }

    public Screen(String framePath, By rootBy) {
       this(null, framePath, rootBy, null);
    }

    public Screen(String framePath) {
       this(framePath, null);
    }

    public Screen(By rootBy, BooleanSupplier isVisible) {
        this(null, null, rootBy, isVisible);
    }

    public Screen(By rootBy) {
        this(null, rootBy);
    }

    public Screen(){
        this(null, null, null, null);
    }

    private Boolean getVisibilityByConvention(){
        worker.waitPageReady(5*1000);
        if (this.framePath != worker.switchTo(framePath)) {
            return false;
        }
        if (root.locator != defaultByOfRoot) {
            return root.isDisplayed();
        }
        String className = this.getClass().getSimpleName();
        if (!className.endsWith("Screen")) {
            return true;
        }

        String screenNameKey = className.substring(0, className.length()-5);
        if (StringUtils.isNotBlank(screenNameKey)) {
            return StringUtils.containsIgnoreCase(worker.driver.getCurrentUrl(), screenNameKey);
        }
        return true;
    }

    public WebElement findElement(By by) {
        List<WebElement> allElements = null;
        try {
            allElements = findElements(by);
            if(allElements == null || allElements.isEmpty())
                return null;

            Optional<WebElement> firstVisible = allElements.stream().filter(WebElement::isDisplayed).findFirst();
                return firstVisible.orElseGet(()->null);
        } catch (Exception ex) {
            Logger.V(ex);
            return null;
        }

    }

    public List<WebElement> findElements(By by){
        List<WebElement> elements = null;
        if(StringUtils.equals(worker.switchTo(this.framePath), this.framePath))
        {
            try {
                elements = getRootElement().findElements(by);
            } catch (Exception ex) {
                //Logger.V(ex);
                //Try again when the Root is not valid
                if (ex instanceof StaleElementReferenceException || ex instanceof ElementNotVisibleException) {
                    elements = getFreshRootElement().findElements(by);
                } else {
                    elements = null;
                }
            }
        }

        return elements;
    }

    public Boolean waitScreenVisible() {
        return waitScreenVisible(DefaultWaitVisibleMills);
    }

    public Boolean waitScreenGone() {
        return waitScreenGone(DefaultWaitGoneMills);
    }

    public Boolean waitScreenVisible(Integer timeoutMills) {
        timeoutMills =(timeoutMills==null  || timeoutMills <= 0) ? DefaultWaitVisibleMills : timeoutMills;

        return Executor.testUntil(visiblePredicate, timeoutMills);
    }

    public Boolean waitScreenGone(Integer timeoutMills) {
        timeoutMills =(timeoutMills==null  || timeoutMills <= 0) ? DefaultWaitGoneMills : timeoutMills;
        return Executor.testUntil(gonePredicate, timeoutMills);
    }

    public Boolean waitPageReady(){
        return getWorker().waitPageReady();
    }

//    @Override
    public void invalidate() {
        root.invalidate();
    }
}

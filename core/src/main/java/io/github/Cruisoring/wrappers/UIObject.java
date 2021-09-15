package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.enums.OnElement;
import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.IUIObject;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.workers.Worker;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base class to wrap WebElement.
 */
public class UIObject implements IUIObject {
    public static final Class UIObjectClass = UIObject.class;

    public static class Collection extends UICollection<UIObject> {
        public Collection(WorkingContext context, By by, Integer index, By childrenBy){
            super(context, by, index, UIObjectClass, childrenBy);
        }

        public Collection(WorkingContext context, By by, By childrenBy){
            this(context, by, 0, childrenBy);
        }

        public Collection(UIObject context, By childrenBy) {
            super(context.parent, context.locator, null, UIObjectClass, childrenBy);
        }
    }

    protected static int DefaultGetElementRetryAttempts = 3;
    protected static final int DefaultElementValidSeconds = 3;
    protected static int DefaultRetryIntervalMills = 100;
    protected static int DefaultWaitEnabledMills = 3*1000;
    protected static int DefaultWaitChangesMills = 30*1000;
    protected static int DefaultWaitBeforeEvaluationMills = 3100;

    protected static Boolean BypassUnnessaryActions = true;
    protected static Boolean AssureShowControlBeforeActions = true;
    protected static Boolean HightlightBeforeActions = true;
    protected static Boolean MouseOverBeforeActions = false;

    public static void sleep(long timeMills) {
        Executor.sleep(timeMills);
    }

    protected final Worker worker;
    public final WorkingContext parent;
    public final By locator;
    public final Integer index;

    protected WebElement element;
    protected LocalDateTime elementValidUntil = LocalDateTime.MIN;

    public UIObject(WorkingContext context, By by, Integer index) {
        if(context == null || by == null) {
            throw new NullPointerException("Context and By must be specified.");
        }
        worker = context.getWorker();
        parent = context;
        locator = by;
        this.index = index;
    }

    public UIObject(WorkingContext context, By by) {
        this(context, by, null);
    }

    public Worker getWorker(){
        return worker;
    }

    @Override
    public void invalidate(){
//        parent.invalidate();
        element = null;
    }

    @Override
    public synchronized WebElement getElement(){

        try {
            if(LocalDateTime.now().isAfter(elementValidUntil) || element == null){
                waitPageReady();
                element = tryGetElement();
                elementValidUntil = LocalDateTime.now().plusSeconds(DefaultElementValidSeconds);
            }
            element.isDisplayed();
        } catch (Exception ex) {
            invalidate();
            element = tryGetElement();
        }
        return element;
    }

    @Override
    public synchronized WebElement getFreshElement(){
        invalidate();
        return getElement();
    }

    public synchronized List<WebElement> getAllElements() {
        try {
            List<WebElement> elements = parent.findElements(locator);
            return elements;
        } catch (Exception e){
            Logger.V(e);
            return new ArrayList<WebElement>();
        }
    }

    public synchronized List<WebElement> getVisibleElements() {
        try {
            List<WebElement> elements = parent.findElements(locator);
            elements = elements.stream().filter(e -> e.isDisplayed())
                    .collect(Collectors.toList());
            return elements;
        } catch (Exception e){
            Logger.V(e);
            return new ArrayList<WebElement>();
        }
    }

    public synchronized Integer getElementsCount() {
        try {
            List<WebElement> elements = parent.findElements(locator);
            return elements.size();
        } catch (Exception e){
            Logger.V(e);
            return 0;
        }
    }

    protected synchronized WebElement tryGetElement() {
        WebElement result = null;

        try {
            List<WebElement> elements = getAllElements();
            int size = elements.size();
            if (size == 0) {
                result = null;
            } else if (index != null) {
                result = elements.get(index < 0 ? (size+index) % size : index);
            } else {
                for (int i = 0; i < size; i++) {
                    if (worker.isVisible(elements.get(i))) {
                        return elements.get(i);
                    }
                }
                return elements.get(0);
            }
        } catch (Exception ex) {
            Logger.V(ex);
        }

        return result;
    }

//    @Override
    public Boolean waitPageReady() {
        return worker.waitPageReady();
    }

    Predicate<Object> alwaysReturnTrue = o -> true;

    @Override
    public Object executeScript(String script, Object... args) {
        return Executor.tryGet(()->{
            JavascriptExecutor executor = worker.driver;
            return executor.executeScript(script, getElement(), args);
        }, DefaultGetElementRetryAttempts, DefaultRetryIntervalMills, alwaysReturnTrue);
    }

    @Override
    public Boolean exists() {
        //worker.waitPageReady(1000);
        WebElement target = getElement();
        return target != null;
    }

    public static final String isHiddenScript = "var style=window.getComputedStyle(arguments[0]); return style.display==='none';";

    public Boolean isHidden() {
        if (!exists())
            return true;
        String result = executeScript(isHiddenScript).toString();
        return Boolean.parseBoolean(result);
    }

    public static final String isVisibleScipt = "var elem=arguments[0]; " +
            "return elem != null && !!( elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length );";

    public Boolean isVisible() {
        try {
            boolean isVisible = worker.isVisible(element) || worker.isVisible(getFreshElement());
            return isVisible;
        } catch (Exception e) {
            WebElement elem = getFreshElement();
            return worker.isVisible(elem);
        }
    }

    public Boolean isDisabled(){
        return worker.isDisabled(getElement());
    }

    @Override
    public Boolean isDisplayed() {
        if (!exists())
            return false;

//        Logger.V("Check if it is visible.");
        Boolean result = isVisible();
        if (!result){
            invalidate();
        }
//        Logger.V("%s: %s Visible.", this, result? "is":"isn't");
        return result;
    }

    public Boolean waitCondition(BooleanSupplier predicate, int waitTimeoutMills) {
        return Executor.testUntil(()-> predicate.getAsBoolean(), waitTimeoutMills);
    }

    public <T extends Object> Boolean waitChanges(Function<UIObject, T> propertyExtractor){
        return waitChanges(propertyExtractor, null, DefaultWaitChangesMills);
    }

    public <T extends Object> Boolean waitChanges(Function<UIObject, T> propertyExtractor, Predicate<T> predicate){
        return waitChanges(propertyExtractor, predicate, DefaultWaitChangesMills);
    }

    public <T extends Object> Boolean waitChanges(Function<UIObject, T> propertyExtractor, Predicate<T> predicate, int waitTimeoutMills){
        T originalProperty = null;
        try {
            originalProperty = propertyExtractor.apply(this);
        }catch(Exception e){}

        if(predicate == null){
            T finalOriginalProperty = originalProperty;
            predicate = t -> !t.equals(finalOriginalProperty);
        }

        Predicate<T> finalPredicate = predicate;
        Boolean result = Executor.testUntil(()-> finalPredicate.test(
                propertyExtractor.apply(this)), waitTimeoutMills);
        invalidate();
        return result;
    }

    public Boolean waitDisplayed(int waitDisplayedMills) {
        return Executor.testUntil(()->
                isDisplayed(),
                waitDisplayedMills);
    }

    public Boolean waitDisplayed(){
        return waitDisplayed(DefaultWaitEnabledMills);
    }

    public Boolean waitGone(int waitGoneMills) {
        return Executor.testUntil(()->
                !isDisplayed(),
                waitGoneMills);
    }

    public Boolean waitGone(){
        return waitGone(DefaultWaitEnabledMills);
    }



    @Override
    public List<WebElement> findElements(By by) {
        try {
            WebElement rootElement = getElement();
            if(rootElement == null)
                return null;
            return rootElement.findElements(by);
        }catch (Exception e){
            Logger.V(e);
            return null;
        }
    }

    @Override
    public WebElement findElement(By by) {
        WebElement rootElement = getElement();
        return rootElement.findElement(by);
    }

    @Override
    public String getTextContent() {
        waitPageReady();
        Object result = executeScript("return arguments[0].textContent;");
        return result == null ? null : result.toString();
    }

    @Override
    public String getInnerHTML() {
        waitPageReady();
        Object result = executeScript("return arguments[0].innerHTML;");
        if(result == null) return  null;

        return result.toString();
    }
    @Override
    public String getOuterHTML() {
        //Logger.D("Before executeScript, element %s null.", element==null? "is":"isn't");
        waitPageReady();
        Object result = executeScript("return arguments[0].outerHTML;");
        return result == null ? null : result.toString();
    }

    @Override
    public String getAllText() {
        String html = getOuterHTML();
        return StringExtensions.extractHtmlText(html);
    }

    public String getFreshAllText(){
        invalidate();
        return getAllText();
    }

    public String getFreshOuterHTML(){
        invalidate();
        return getOuterHTML();
    }

    public String getFreshInnerHTML(){
        invalidate();
        return getInnerHTML();
    }

    public String getTagName() {

        return getElement().getTagName();
    }

    private static final String getAttributeScript = "try{{\n" +
            "return arguments[0].getAttribute('{0}');\n" +
            "}}catch(err){{return null;}}";

    @Override
    public String getAttribute(String attributeName) {
        String script = getAttributeScript.replace("{0}", attributeName);
        Object result = executeScript(script);
        return result == null ? null : result.toString();
    }

    public String getElementClass() {
        return getAttribute("class");
    }

    @Override
    public String getCssValueOf(String propertyName) {
        String result= getElement().getCssValue(propertyName);
        return result;
    }

    private static final String fireEventScript = "var event;\n" +
            "\tif(document.createEvent){\n" +
            "\t\tevent = document.createEvent('HTMLEvents'); // for chrome and firefox\n" +
            "\t\tevent.initEvent('eventName', true, true);\n" +
            "\t\targuments[0].dispatchEvent(event); // for chrome and firefox\n" +
            "\t}else{\n" +
            "\t\tevent = document.createEventObject(); // for InternetExplorer\n" +
            "\t\tevent.eventType = 'eventName';\n" +
            "\t\targuments[0].fireEvent('on' + event.eventType, event); // for InternetExplorer\n" +
            "\t}";

    @Override
    public void fireEvent(String eventName) {
        String eventScript = fireEventScript.replaceAll("eventName", eventName);
        executeScript(eventScript);
    }

    public static String DefaultHighlightScript = "var e =arguments[0]; e.style.color='red';e.style.backgroundColor='yellow';";
    public static String DefaultResetStyleScript = "var e=arguments[0]; e.style.color='';e.style.backgroundColor='';";
    protected static int DefaultHighlightIntervalMills = 100;
    protected static int DefaultHighlightTimes = 1;

    @Override
    public void highlight(String highlightScript, String resetScript, int interval, int times) {
//        try (Logger.Timer timer = Logger.M()) {
            if (getElement() == null) {
                return;
            }

            if (highlightScript == null) {
                highlightScript = DefaultHighlightScript;
            }
            if (resetScript == null) {
                resetScript = DefaultResetStyleScript;
            }
            if(interval <= 0) {
                interval = DefaultHighlightIntervalMills;
            }
            if(times <= 0) {
                times = DefaultHighlightTimes;
            }

            if("img".equalsIgnoreCase(getElement().getTagName()) ||
                    "checkbox".equalsIgnoreCase(getAttribute("type"))){
                highlightScript.replaceAll("arguments[0]", "arguments[0].parentElement");
                resetScript.replaceAll("arguments[0]", "arguments[0].parentElement");
            }

    //        long start = System.currentTimeMillis();
            for(int i=0; i<times; i++) {
                //Logger.V("To execute highlight script.");
                executeScript(highlightScript);
                //Logger.V("After Executing highlight script.");
                sleep(interval);
                //Logger.V("After sleep %d millis.", interval);
                executeScript(resetScript);
    //            long expected = interval * (i+1);
    //            long elapsed = System.currentTimeMillis() - start;
    //            if (((double)(elapsed - expected))/expected > 0.1)
    //                Logger.V("After highlighting %d times, expected elapsed=%dms, actual=%dms.", i+1, expected, elapsed);
    //            sleep(interval);
    //            Logger.V("After sleep %d millis.", interval);
            }
//        }catch (Exception e){
//
//        }
    }

    @Override
    public void highlight(){
        this.highlight(DefaultHighlightScript, DefaultResetStyleScript, DefaultHighlightIntervalMills, DefaultHighlightTimes);
    }

    public void highlight(String highlightScript, int highlightMills){
        this.highlight(highlightScript, DefaultResetStyleScript, highlightMills, DefaultHighlightTimes);
    }

    @Override
    public void clickByScript(){
        executeScript("arguments[0].click();");
        Logger.V("%s is clicked by Script.", this);
    }

    /**
     * Scroll the element into the visible area of the browser window:
     * @param alignTop Optional boolean value that indicates the type of the align:
     *                true - the top of the element will be aligned to the top of the visible area of the scrollable ancestor
     *                false - the bottom of the element will be aligned to the bottom of the visible area of the scrollable ancestor.

     */
    @Override
    public void scrollIntoViewByScript(Boolean alignTop) {
        String script = "arguments[0].scrollIntoView(" + alignTop + ");";
        executeScript(script);
    }

    /**
     * Scroll the element into the visible area of the browser window, scroll to the top of the element.
     */
    @Override
    public void scrollIntoViewByScript() {
        scrollIntoViewByScript(true);
    }

    @Override
    public void scrollIntoView() {
        try {
            scrollIntoViewByScript(false);
        } catch (Exception ex){
            scrollIntoViewByScript(true);
        }
    }

    @Override
    public void mouseOver(){
        Actions builder = new Actions(worker.driver);
        builder.moveToElement(getElement()).build().perform();
    }

    @Override
    public void mouseOverByScript() {
        fireEvent("mouseover");
    }

    @Override
    public void rightClick(){
        WebElement e = getElement();
        if(e == null || !e.isDisplayed()){
            Logger.W("%s is not visible to be right-clicked.", this);
        }
        Actions builder = new Actions(worker.driver);
        builder.contextClick(e).perform();
        Logger.V("%s is right-clicked.", this);
    }

    @Override
    public String toString(){
        String result = String.format("%s(%s%s)",
                this.getClass().getSimpleName(),
                locator.toString().substring(locator.toString().indexOf(":")+1),
                index == null ? "" : "[" + index + "]");

        try {
            if(element != null && StringUtils.isNotEmpty(element.getText())) {
                result = String.format("%s%s = %s",
                        this.getClass().getSimpleName(),
                        index == null?"":"["+index+"]",
                        element.getText());
            }
        } catch (Exception ex) {}
        return result;
    }

    @Override
    public Boolean perform(Runnable[] actions, BooleanSupplier evaluation, int retry) {
        Boolean result = false;
        try (Logger.Timer timer = Logger.M()) {

            if(worker.driverType != DriverType.IE) {
                worker.waitPageReady();
            }

            if(!exists()) {
                Logger.E("Failed to find %s, action aborted.", this);
                return false;
            }
//            Logger.V("End of exists()");

            if (retry == -1) {
                retry = actions.length-1;
            }

            try {
//                Logger.V("Check if element is enabled.");
                if(!getElement().isEnabled() && Executor.testUntil(() -> getElement().isEnabled(), DefaultWaitEnabledMills)) {
//                    Logger.E("%s is still not enabled.", this);
                    return false;
                }
//                Logger.V("Now element is enabled.");

                if(BypassUnnessaryActions && evaluation != null && evaluation.getAsBoolean()) {
//                    Logger.V("Validation passed already, action on %s is bypassed.", this);
                    return true;
                }

//                Logger.V("Before checking if element is shown.");
                if(AssureShowControlBeforeActions) {
                    Executor.tryRun(new Runnable[]{
                            ()->scrollIntoView(),
                            ()->this.scrollIntoViewByScript()});
//                    Logger.V("After element is shown.");

                    if(HightlightBeforeActions) {
                        highlight();
//                        Logger.V("After element is highlighted.");
                    }
                }

                if(MouseOverBeforeActions) {
                    Executor.tryRun(new Runnable[]{
                            ()->mouseOver(),
                            ()->mouseOverByScript()
                    });
                }

//                Logger.V("Before perform solid actions");
                Executor.tryRun(actions, actions.length-1);
//                Logger.V("Action on %s is performed successfully.", this);
                return true;
            } catch (UnhandledAlertException alertEx) {
                try {
                    worker.acceptDialog();
                    if (retry > 0) {
                        return perform(actions, evaluation, retry--);
                    }
                } catch (Exception e) {
                    Logger.W("'%s' happened after UnhandledAlertException", e.getMessage());
                    return false;
                }
            } catch (StaleElementReferenceException ex) {
                Logger.V(ex);
                if (retry > 0) {
                    return perform(actions, evaluation, retry--);
                } else {
                    Logger.W("%s is staled!", this);
                    return false;
                }
            }

            if(evaluation == null) {
                return true;
            }

            if(DefaultWaitBeforeEvaluationMills >= 0) {
                Executor.sleep(DefaultWaitBeforeEvaluationMills);
            }
            return evaluation.getAsBoolean();
        }catch (Exception e){
            return result;
        }
    }

    @Override
    public Boolean perform(Runnable action, BooleanSupplier evaluation, int retry){
        return perform(new Runnable[]{action}, evaluation, retry);
    }

    @Override
    public Boolean perform(String instruction){
        return perform(new Runnable[]{
                () -> this.click(),
                () -> this.clickByScript()
        }, null, -1);
    }

    @Override
    public void click() {
        if (!perform(null)) {
            Logger.W("Failed to click on %s.", this);
            Logger.V("%s: %s", this, getOuterHTML());
        }
    }

    @Override
    public boolean click(int waitReadyMills){
        try {
            Point location = getElement().getLocation();
            perform(new Runnable[]{
                    () -> this.clickByScript(),
                    () -> this.getElement().click()
            }, () -> !this.isVisible() || getElement().getLocation().y != location.y, -1);
            Logger.V("clicked on %s", this);
            if (waitReadyMills < 0)
                waitReadyMills = DefaultWaitChangesMills;
            if (worker.driverType == DriverType.IE) {
                sleep(waitReadyMills);
                return true;
            } else {
                sleep(1000);
                return worker.waitPageReady(waitReadyMills);
            }
        } catch (Exception e){
            Logger.D(e);
            return false;
        }
    }

    public void click(OnElement position) {
        WebElement element = getElement();
        Dimension size = element.getSize();
        int offset = Math.min(size.width, size.height) / 4;
        int x=0, y=0;
        switch (position){
            case top:
                x = size.width/2;
                y = offset;
                break;
            case left:
                x = offset;
                y = size.height/2;
                break;
            case bottom:
                x = size.width/2;
                y = size.height - offset;
                break;
            case right:
                x = size.width -offset;
                y = size.height/2;
                break;
        }
        clickByOffset(x, y);
    }

    public void clickByOffset(int xOffset, int yOffset) {
        Actions builder = new Actions(worker.driver);
        Action action = builder.moveToElement(getElement(), xOffset, yOffset).click().build();
        action.perform();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        UIObject another = (UIObject)obj;
        if (another==null || this.parent != another.parent || this.locator != another.locator) {
            return false;
        }
        if (this.index!=null && another.index != null && this.index != another.index) {
            return false;
        }

        return this.getFreshElement().equals(another.getFreshElement());
    }

    public String asBase64(){
        return this.executeScript(Worker.getImageBase64).toString();
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + (parent == null ? 0 : parent.hashCode());
        result = prime * result + (locator == null ? 0 : locator.hashCode());
        result = prime * result + (index == null ? -1 : index);
        return result;
    }

    public Boolean fail(String logMessage){
        Logger.V(logMessage);
        return false;
    }
    public Boolean succeed(String logMessage){
        Logger.V(logMessage);
        return true;
    }
}

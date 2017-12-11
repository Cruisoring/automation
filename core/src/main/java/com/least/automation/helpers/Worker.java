package com.least.automation.helpers;

import com.least.automation.enums.ReadyState;
import com.least.automation.wrappers.Screen;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class Worker implements AutoCloseable, SearchContext {
    public final static String FrameIndicator = ">";
    public final static String RootFramePath = "";

    public static final String DefaultMatchingHighlightScript = "var e=arguments[0]; e.style.color='teal';e.style.backgroundColor='green';";
    public static final String DefaultContainingHighlightScript = "var e=arguments[0]; e.style.color='olive';e.style.backgroundColor='orange';";
    public static final String DefaultMismatchHighlightScript = "var e=arguments[0]; e.style.color='fuchsia';e.style.backgroundColor='red';";
    public static final boolean AlwaysWaitPageReady = false;
    public static final boolean WaitPageReadyBeforePerforming = false;
    public static final boolean WaitPageReadyAfterPerforming = true;

    public static final EnumSet<ReadyState> pageLoadedStates = EnumSet.of(ReadyState.complete, ReadyState.loaded);
    public static final EnumSet<ReadyState> pageLoadingStates = EnumSet.of(ReadyState.interactive, ReadyState.loading, ReadyState.unknown);

    public static final int DefaultSwitchFrameRetry = 1;
    public static final long DefaultPageReadyTimeoutMills = 30 * 1000;
    public static final long DefaultAlertHandlingTimeoutMills = 10*1000;
    public static final long DefaultIntervalBetweenActions = 300;

    public static String mergeFramePath(String parentFramePath, String framePath) {
        if(!StringUtils.isNotBlank(framePath))
            return parentFramePath;

        framePath = normalizeFramePath(framePath);
        return StringUtils.isNotBlank(parentFramePath) ? String.join(FrameIndicator, parentFramePath, framePath) : framePath;
    }

    public static String normalizeFramePath(String original) {
        return Arrays.stream(getFrames(original))
                .filter(StringUtils::isNotBlank)
                .reduce((s1, s2) -> s1+FrameIndicator+s2).orElse(RootFramePath);
    }
    public static String[] getFrames(String framePath) {
        framePath = StringUtils.isBlank(framePath) ? RootFramePath : framePath;
        return framePath.split("[>&$ ]", 0);
    }

    public final RemoteWebDriver driver;

    private final Map<Class<? extends Screen>, Screen> ScreenSingletons = new HashMap<>();

    public <T extends Screen> T screenOf(Class<T> instanceClass){
        if(ScreenSingletons.containsKey(instanceClass)) {
            return (T) ScreenSingletons.get(instanceClass);
        } else {
            T instance = null;
            try {
                instance = instanceClass.newInstance();
            } catch (InstantiationException e) {
                Logger.V(e);
            } catch (IllegalAccessException e) {
                Logger.V(e);
            }
            ScreenSingletons.put(instanceClass, instance);
            return instance;
        }
    }

    //protected String lastUrl = "";

    protected List<String> currentFrames = new ArrayList<String>();

    public String getCurrentFramePath(){
        return String.join(FrameIndicator, currentFrames);
    }

    public Worker(WebDriver driver) {

        this.driver = (RemoteWebDriver) driver;
    }

    public String gotoUrl(String url) {
        driver.get(url);
        waitPageReady();
        return  driver.getCurrentUrl();
    }

    public String switchTo(String framePath){
        return switchTo(framePath, DefaultSwitchFrameRetry);
    }

    public String switchTo(String framePath, int retry) {
        framePath = framePath == null ? "" : framePath;

        String oldFramePath = getCurrentFramePath();
        if (StringUtils.equalsIgnoreCase(oldFramePath, framePath)) {
            return framePath;
        }
        //Assume the page has been reloaded
        driver.switchTo().defaultContent();
        //lastUrl = driver.getCurrentUrl();
        currentFrames.clear();

        String[] targetFrames = getFrames(framePath);
        int lastIdenticalIndex = -1;
        int max = Math.min(currentFrames.size(), targetFrames.length);
        for(int i=0; i<max; i++){
            if(currentFrames.get(i) == targetFrames[i]){
                lastIdenticalIndex = i;
            } else {
                break;
            }
        }

        while (currentFrames.size()-1 > lastIdenticalIndex) {
            driver.switchTo().parentFrame();
            currentFrames.remove(currentFrames.size()-1);
        }

        for (int i = lastIdenticalIndex + 1; i < targetFrames.length; i ++) {
            String nextFrame = targetFrames[i];
            try {
                if (StringUtils.isNotBlank(nextFrame)){
                    driver.switchTo().frame(nextFrame);
                }
                currentFrames.add(nextFrame);
            } catch (NoSuchFrameException ex) {
                Logger.W("Failed to switch to frame '%s'.", nextFrame);
                if(retry > 0){
                    driver.switchTo().defaultContent();
                    currentFrames.clear();
                    return switchTo(framePath, retry--);
                }

                String currentFramePath = getCurrentFramePath();
                Logger.E("Failed to switch from '%s' to frame of '%s'.", currentFramePath, nextFrame);
                return currentFramePath;
            }
        }

        String currentFramePath = getCurrentFramePath();
        if(!StringUtils.equals(oldFramePath, currentFramePath))
            Logger.I("Switch Browser from '%s' to '%s'.", oldFramePath, currentFramePath);
        //lastUrl = driver.getCurrentUrl();
        return currentFramePath;
    }

    public final static String getReadyStateScript = "if(document && document.readyState)return document.readyState;"
            + "else if(contentDocument && contentDocument.readyState) return contentDocument.readyState;"
            + "else if(document && document.parentWindow) return document.parentWindow.document.readyState;"
            + "else return 'unknown';";

    public ReadyState getReadyState()
    {
        try {
            String stateStr = driver.executeScript(getReadyStateScript).toString();
            return ReadyState.fromString(stateStr);
        } catch (UnhandledAlertException alert) {
            return ReadyState.loaded;
        } catch (Exception ex) {
            Logger.V(ex);
            return ReadyState.unknown;
        }
    }

    protected Boolean waitForReadyState(long timeoutMillis,EnumSet<ReadyState> expectedStates) {
        Boolean isReady = false;
//        try (Logger.Timer timer = Logger.M()) {
                final BooleanSupplier evaluator = () -> expectedStates.contains(getReadyState());
                isReady = Executor.testUntil(evaluator, timeoutMillis);
//        } catch (Exception e) {
//        }
        return isReady;
    }

    public Boolean waitPageReady(long timeoutMills){
        return waitForReadyState(timeoutMills, pageLoadedStates);
    }

    public Boolean waitPageReady(){
        return waitForReadyState(DefaultPageReadyTimeoutMills, pageLoadedStates);
    }

//    @Override
    public void invalidate() {
    }

    @Override
    public void close() throws Exception {
        if (driver == null) {
            return;
        }
        try {
            Set<String> windows = driver.getWindowHandles();
            if (windows.size() == 0) {
                return;
            }
            driver.executeScript("window.onbeforeunload = function(e){};");
            String driverDesc = driver.toString();
            driver.close();
            Logger.V("Driver '%s' is closed successfully.", driverDesc);
        } catch(Exception ex) {
            Logger.V(ex);
            driver.quit();
        }
    }

    public Boolean closeDialog(Consumer<Alert> alertHandler, long timeoutMills) {
        if (alertHandler == null) {
            alertHandler = alert -> alert.accept();
        }

        LocalDateTime timeout = LocalDateTime.now().plus(Duration.ofMillis(timeoutMills));
        do {
            try {
                Alert alert = driver.switchTo().alert();
                alertHandler.accept(alert);
                return true;
            } catch (NoAlertPresentException noAlert) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Logger.V(e);
                }
            }
        }while (LocalDateTime.now().isBefore(timeout));
        return false;
    }

    public boolean acceptDialog() {
        return closeDialog(Alert::accept, DefaultAlertHandlingTimeoutMills);
    }

    public boolean dismissDialog() {
        return closeDialog(Alert::dismiss, DefaultAlertHandlingTimeoutMills);
    }

    public String getElementAttribute(WebElement element, String attributeName) {
        try{
            if(element == null)
                throw new NullPointerException("Element is null");
            if(StringUtils.isBlank(attributeName))
                throw  new IllegalArgumentException("Attribute Name cannot be null or empty.");
            return element.getAttribute(attributeName);
        } catch (Exception ex) {
            Logger.W(ex.getMessage());
            return null;
        }
    }

    public Worker getPlayer() {
        return this;
    }

    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    public WebElement findElement(By by) {
        return driver.findElement(by);
    }

    public static final String isVisibleScipt = "var e=arguments[0].getBoundingClientRect(); return e.width>0 && e.height>0;";
//            "var elem=arguments[0]; " +
//            "return elem != null && !!( elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length );";

    public Boolean isVisible(WebElement e) {
        try {
            if (e == null) return false;
            JavascriptExecutor executor = driver;
            String result = executor.executeScript(isVisibleScipt, e).toString();
            return Boolean.parseBoolean(result);
        } catch (Exception ex) {
            Logger.V("%s with %s", ex.getClass().getSimpleName(), e.getTagName());
            return false;
        }
    }

    public static final String isDisabledScript = "var e=arguments[0];" +
            "if(e.getAttribute('disabled')!==null)return true;" +
            "var clazz=e.getAttribute('class');" +
            "return clazz!=null && clazz.includes('disabled');";

    public Boolean isDisabled(WebElement e) {
        try {
            if (e == null) return true;
            JavascriptExecutor executor = driver;
            String result = executor.executeScript(isDisabledScript, e).toString();
            return Boolean.parseBoolean(result);
        } catch (Exception ex) {
            Logger.V("%s with %s", ex.getClass().getSimpleName(), e.getTagName());
            return true;
        }
    }

    public Boolean isEnabled(WebElement e) {
        return !isDisabled(e);
    }
}

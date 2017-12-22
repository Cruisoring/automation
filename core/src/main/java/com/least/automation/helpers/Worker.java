package com.least.automation.helpers;

import com.least.automation.enums.DriverType;
import com.least.automation.enums.ReadyState;
import com.least.automation.interfaces.WorkingContext;
import com.least.automation.wrappers.Screen;
import com.least.automation.wrappers.UIObject;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class Worker implements AutoCloseable, WorkingContext {
    public final static String FrameIndicator = ">";
    public final static String RootFramePath = "";
//    public final static By RootBy = By.tagName("html");
//    public final static By BodyBy = By.tagName("body");
//    public final static By HtmlTitleBy = By.tagName("head>title");
//
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
    public static final long DefaultAlertHandlingTimeoutMills = 10 * 1000;
    public static final long DefaultIntervalBetweenActions = 300;

    private static final List<DriverType> driverTypes = new ArrayList<>();
    private static final List<WebDriver> drivers = new ArrayList<>();
    private static int driverCount = 0;

//    static {
//        System.setProperty("webdriver.chrome.driver", "../drivers/chromedriver.exe");
////        System.setProperty("webdriver.ie.driver", "vendor/IEDriverServer.exe");
//    }

    public static Map<String, String> mappedURLs = new HashMap<>();

    private static Worker singleton = null;

    public static Worker getAvailable(DriverType... type) {
        if (singleton == null) {
            singleton = getChromePlayer(null);
        }
        return singleton;
    }

    private static Worker getIEPlayer(DesiredCapabilities desiredCapabilities) {
        if (desiredCapabilities == null) {
            System.setProperty("webdriver.ie.driver.loglevel", "TRACE");
            System.setProperty("webdriver.ie.driver.logfile", "C:/Projects/logme.txt");
            DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
            caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
            caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            //caps.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
            WebDriver driver = new InternetExplorerDriver(caps);
        }
        WebDriver driver = new InternetExplorerDriver(desiredCapabilities);
        return new Worker(driver);
    }

    private static Worker getChromePlayer(ChromeOptions options) {
        if (options == null) {
            options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-web-security");
//            options.addArguments("--no-proxy-server");

            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);

            options.setExperimentalOption("prefs", prefs);
        }
        ChromeDriver chrome = new ChromeDriver(options);
        return new Worker(chrome);
    }

    public static String mergeFramePath(String parentFramePath, String framePath) {
        if (!StringUtils.isNotBlank(framePath))
            return parentFramePath;

        framePath = normalizeFramePath(framePath);
        return StringUtils.isNotBlank(parentFramePath) ? String.join(FrameIndicator, parentFramePath, framePath) : framePath;
    }

    public static String normalizeFramePath(String original) {
        return Arrays.stream(getFrames(original))
                .filter(StringUtils::isNotBlank)
                .reduce((s1, s2) -> s1 + FrameIndicator + s2).orElse(RootFramePath);
    }

    public static String[] getFrames(String framePath) {
        framePath = StringUtils.isBlank(framePath) ? RootFramePath : framePath;
        return framePath.split("[>&$ ]", 0);
    }

    public final RemoteWebDriver driver;

    public String asBase64() {
        try {
            String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            return String.format("<img src='data:image/png;base64, %s'/>", base64);
        } finally {
            return "";
        }
    }

    public static final String replaceImageAsBase64Script = "if (typeof window.replaceImage === 'undefined') {\n" +
            "\tvar canvas = canvas || document.createElement('canvas');\n" +
            "\tvar ctx = ctx || canvas.getContext('2d');\n" +
            "\twindow.replaceImage = function(e){\n" +
            "\t\tif(e.tagName == 'IMG'){\n" +
            "\t\t\tcanvas.width = e.width;\n" +
            "\t\t\tcanvas.height = e.height;\n" +
            "\t\t\tctx.drawImage(e, 0, 0);\n" +
            "\t\t\te.src = canvas.toDataURL();\n" +
            "\t\t\treturn true;\n" +
            "\t\t}else {\n" +
            "\t\t\treturn false;\n" +
            "\t\t}\n" +
            "\t}\n" +
            "}\n" +
            "replaceImage(arguments[0]);";

    public static final String getImageBase64 = "var e = arguments[0];\n" +
            "var canvas = document.createElement('canvas');\n" +
            "canvas.width = e.width;\n" +
            "canvas.height = e.height;\n" +
            "canvas.getContext('2d').drawImage(e, 0, 0);\n" +
            "return canvas.toDataURL();";

    public boolean replaceImageAsBase64(UIObject image) {
        return (boolean) image.executeScript(replaceImageAsBase64Script);
    }

    public final Map<Class<? extends Screen>, Screen> screens = new HashMap<>();

    public <T extends Screen> T getScreen(Class<T> screenClass) {
        if (screens.containsKey(screenClass)) {
            return (T) screens.get(screenClass);
        }
        T instance = (T) Screen.getScreenFactory(screenClass).apply(this);
        if (instance != null) {
            screens.put(screenClass, instance);
        }
        return instance;
    }

    protected List<String> currentFrames = new ArrayList<String>();

    public String getCurrentFramePath() {
        return String.join(FrameIndicator, currentFrames);
    }

    public Worker getWorker() {
        return this;
    }

    protected Worker(WebDriver driver) {
        this.driver = (RemoteWebDriver) driver;
    }

    public String gotoUrl(String url) {
        driver.get(url);
        waitPageReady(60*1000);
        String currentUrl = driver.getCurrentUrl();
        Logger.I("get to: " + currentUrl);
        return currentUrl;
    }

    public String switchTo(String framePath) {
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
        for (int i = 0; i < max; i++) {
            if (currentFrames.get(i) == targetFrames[i]) {
                lastIdenticalIndex = i;
            } else {
                break;
            }
        }

        while (currentFrames.size() - 1 > lastIdenticalIndex) {
            driver.switchTo().parentFrame();
            currentFrames.remove(currentFrames.size() - 1);
        }

        for (int i = lastIdenticalIndex + 1; i < targetFrames.length; i++) {
            String nextFrame = targetFrames[i];
            try {
                if (StringUtils.isNotBlank(nextFrame)) {
                    driver.switchTo().frame(nextFrame);
                }
                currentFrames.add(nextFrame);
            } catch (NoSuchFrameException ex) {
                Logger.W("Failed to switch to frame '%s'.", nextFrame);
                if (retry > 0) {
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
        if (!StringUtils.equals(oldFramePath, currentFramePath))
            Logger.I("Switch Browser from '%s' to '%s'.", oldFramePath, currentFramePath);
        //lastUrl = driver.getCurrentUrl();
        return currentFramePath;
    }

    public final static String getReadyStateScript = "if(document && document.readyState)return document.readyState;"
            + "else if(contentDocument && contentDocument.readyState) return contentDocument.readyState;"
            + "else if(document && document.parentWindow) return document.parentWindow.document.readyState;"
            + "else return 'unknown';";

    public ReadyState getReadyState() {
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

    protected Boolean waitForReadyState(long timeoutMillis, EnumSet<ReadyState> expectedStates) {
        Boolean isReady = false;
//        try (Logger.Timer timer = Logger.M()) {
        final BooleanSupplier evaluator = () ->
                expectedStates.contains(getReadyState()) || isAjaxDone();
        isReady = Executor.testUntil(evaluator, timeoutMillis);
//        } catch (Exception e) {
//        }
        return isReady;
    }

    public Boolean waitPageReady(long timeoutMills) {
        return waitForReadyState(timeoutMills, pageLoadedStates);
    }

    public Boolean waitPageReady() {
        return waitForReadyState(DefaultPageReadyTimeoutMills, pageLoadedStates);
    }

    public final static String isAjaxDoneScript = "return jQuery.active == 0";

    public Boolean isAjaxDone() {
        Boolean result = (Boolean) driver.executeScript(isAjaxDoneScript);
        return result;
    }

    public Boolean waitAjaxDone(long timeoutMills) {
        return Executor.testUntil(() -> isAjaxDone(), timeoutMills);
    }

    public Boolean waitAjaxDone() {
        return waitAjaxDone(DefaultPageReadyTimeoutMills);
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
        } catch (Exception ex) {
            Logger.W(ex);
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
        } while (LocalDateTime.now().isBefore(timeout));
        return false;
    }

    public boolean acceptDialog() {
        return closeDialog(Alert::accept, DefaultAlertHandlingTimeoutMills);
    }

    public boolean dismissDialog() {
        return closeDialog(Alert::dismiss, DefaultAlertHandlingTimeoutMills);
    }

    public String getElementAttribute(WebElement element, String attributeName) {
        try {
            if (element == null)
                throw new NullPointerException("Element is null");
            if (StringUtils.isBlank(attributeName))
                throw new IllegalArgumentException("Attribute Name cannot be null or empty.");
            return element.getAttribute(attributeName);
        } catch (Exception ex) {
            Logger.W(ex.getMessage());
            return null;
        }
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

}

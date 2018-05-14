package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.helpers.*;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.cruisoring.function.FunctionThrowable;
import io.github.cruisoring.repository.TupleRepository2;
import io.github.cruisoring.tuple.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Screen implements WorkingContext {
    public static final String className = Screen.class.getName();

    public static final String DefaultRootCssSelector = "body, form, div, span, table";
    public static final Integer DefaultWaitVisibleMills = 10*1000;
    public static final Integer DefaultWaitGoneMills = 5* 1000;
    public static final By defaultByOfRoot = By.cssSelector(DefaultRootCssSelector);

    public static Predicate<Worker> predicateByConvention(){
        Class clazz = ClassHelper.getCallerClass(s -> !StringUtils.equalsIgnoreCase(s.getClassName(), className));
        String className = clazz.getSimpleName();
        String urlExpected = className.replaceAll("Screen", "");
        return w ->{
            String url = w.driver.getCurrentUrl();
            boolean result = StringExtensions.containsAllIgnoreCase(url, urlExpected);
            Logger.I("URL '%s' %s contains '%s'", url, result ? "":"doesn't", urlExpected);
            return result;
        };
    }

    public static TupleRepository2.TupleKeys1<Class<? extends Screen>, FunctionThrowable<Worker, ? extends Screen>, Boolean>
            screenRepository =
            TupleRepository2.fromKeys1(screenClass -> {
                Constructor constructor = screenClass.getDeclaredConstructor(Worker.class);
                constructor.setAccessible(true);
                FunctionThrowable<Worker, ? extends Screen> factory = worker -> (Screen) constructor.newInstance(worker);
                return Tuple.create(factory, false);
    });

//    public static Map<Class<? extends Screen>, Function<Worker, ? extends Screen>> screenFactories = new HashMap<>();
//
////    public static HashMap<String,HashMap<String,IUIObject>> objectsMapsByName;
//    public static <T extends Screen> Function<Worker, ? extends Screen> getScreenFactory(Class<T> screenClass){
//        if (screenFactories.containsKey(screenClass)){
//            return screenFactories.get(screenClass);
//        }
//
//        Function<Worker, ? extends Screen> factory = null;
//        try {
//            Constructor con = screenClass.getDeclaredConstructor(Worker.class);
//            con.setAccessible(true);
//            factory = worker -> {
//                try {
//                    return (Screen) con.newInstance(worker);
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            };
//            screenFactories.put(screenClass, factory);
//            return factory;
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    protected final Worker worker;

    protected final UIObject root;

    public WebElement getRootElement() {
        return root.getElement();
    }

    public WebElement getFreshRootElement() {
        return root.getFreshElement();
    }

    protected final Screen parent;
    //protected final By rootLocator;
    public final String framePath;

    protected Predicate<Worker> visiblePredicate;

    public Screen(Worker worker, Screen parent, String framePath, By rootBy, Predicate<Worker> isVisible) {
       this.worker = worker;
       this.parent = parent;
       this.framePath = Worker.mergeFramePath(parent==null?"":parent.framePath, framePath);
       this.root = new UIObject(parent==null? worker : parent, rootBy == null ? defaultByOfRoot : rootBy, null);
       this.visiblePredicate = isVisible == null ? predicateByConvention() : isVisible;
    }

    public Screen(Worker worker, String framePath, By rootBy) {
       this(worker, null, framePath, rootBy, null);
    }

    public Screen(Worker worker, String framePath) {
       this(worker, framePath, null);
    }

    public Screen(Worker worker, By rootBy, Predicate<Worker> isVisible) {
        this(worker, null, null, rootBy, isVisible);
    }

    public Screen(Worker worker, By rootBy) {
        this(worker, null, rootBy);
    }

    protected Screen(Worker worker){
        this(worker, null, null, null, null);
    }

    public Worker getWorker(){
        return worker;
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

    public Boolean waitAjaxDone(Integer timeoutMills) {
        return worker.waitAjaxDone(timeoutMills);
    }
    public Boolean waitAjaxDone() {
        return worker.waitAjaxDone();
    }

    public Boolean waitScreenGone() {
        return waitScreenGone(DefaultWaitGoneMills);
    }

    public Boolean waitScreenVisible(Integer timeoutMills) {
        timeoutMills =(timeoutMills==null  || timeoutMills <= 0) ? DefaultWaitVisibleMills : timeoutMills;

        LocalDateTime start = LocalDateTime.now();
        boolean result = Executor.testUntil(()->visiblePredicate.test(worker), timeoutMills);
        Duration duration = Duration.between(start, LocalDateTime.now());
        Logger.I("%s %s visible after %s", this.getClass().getSimpleName(), result?"is":"isn't", duration);
        return result;
    }

    public Boolean waitScreenGone(Integer timeoutMills) {
        timeoutMills =(timeoutMills==null  || timeoutMills <= 0) ? DefaultWaitGoneMills : timeoutMills;
        LocalDateTime start = LocalDateTime.now();
        boolean result = Executor.testUntil(()-> !visiblePredicate.test(worker), timeoutMills);
        Duration duration = Duration.between(start, LocalDateTime.now());
        Logger.I("%s %s gone after %s", this.getClass().getSimpleName(), result?"is":"isn't", duration);
        return result;
    }

    public Boolean waitPageReady(){
        return worker.waitPageReady();
    }


//    @Override
    public void invalidate() {
        root.invalidate();
    }
}

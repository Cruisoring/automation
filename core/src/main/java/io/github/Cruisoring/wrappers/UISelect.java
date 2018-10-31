package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.interfaces.WorkingContext;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.IntStream;

public class UISelect extends UIObject {
    public static final Class UISelectClass = UISelect.class;
    public static final By defaultSelectLocator = By.cssSelector("select");
    public final static String OptionTagName = "option";
    protected static Boolean DefaultExpandBeforeSelect = false;
    protected static long DefaultWaitAfterExpanding = 500;

    public static class Collection extends UICollection<UISelect> {
        public Collection(WorkingContext context, By by, Integer index, By childrenBy){
            super(context, by, index, UISelectClass, childrenBy);
        }

        public Collection(WorkingContext context, By by, By childrenBy){
            this(context, by, 0, childrenBy);
        }

        public Collection(UIObject context, By childrenBy) {
            super(context.parent, context.locator, null, UISelectClass, childrenBy);
        }
    }

    public UISelect(WorkingContext context, By by, Integer index) {
        super(context, by, index);
    }

    public UISelect(WorkingContext context, By by) {
        this(context, by, null);
    }

    public UISelect(WorkingContext context) {
        this(context, defaultSelectLocator);
    }

    public WebElement getSelectedOption() {
        List<WebElement> options = findElements(By.tagName(OptionTagName));
        WebElement selected = options.stream().filter(x -> x.isSelected()).findFirst().orElse(null);
        return selected;
    }

    public String[] getOptionArray(Function<WebElement, String> converter) {
        List<WebElement> options = findElements(By.tagName(OptionTagName));
        return options.stream()
                .map(o -> converter.apply(o))
                .toArray(size -> new String[size]);
    }

    public String[] getOptionTexts() {
        return getOptionArray(e -> StringEscapeUtils.escapeHtml4(e.getText()));
    }

    public String[] getOptionValues() {
        return getOptionArray(e -> StringEscapeUtils.escapeHtml4(e.getAttribute("value")));
    }

    public void expandByClick(){
        Dimension size = getElement().getSize();
        int yOffset = size.getHeight() / 2;
        int xOffset = size.getWidth() - yOffset;

        Actions builder = new Actions(worker.driver);
        builder.moveToElement(getFreshElement(), xOffset, yOffset).clickAndHold().perform();
        sleep(DefaultWaitAfterExpanding);
        builder.moveToElement(getElement()).release().perform();
    }

    public Boolean selectOption(By optionBy, Boolean expandBeforeSelect) {
        worker.waitPageReady();

        UIObject optionToSelect = new UIObject(this, optionBy);
        if (!optionToSelect.exists()) {
            Logger.W("Failed to locate the option with: " + optionBy);
            return false;
        }

        Runnable action = () -> {
            if (expandBeforeSelect) {
                expandByClick();
                optionToSelect.getElement().click();
                //In case Selected Option is not updated
                sleep(100);
                getElement().click();
            } else {
                optionToSelect.getElement().click();
            }
        };

        Boolean result = super.perform(action, null, 1);
        if(result) {
            Logger.D("Option '%s' of %s is selected.", optionBy, this);
        }
        return result;
    }

    public Boolean selectOption(By optionBy) {
        return selectOption(optionBy, DefaultExpandBeforeSelect);
    }

    public Boolean selectOption(int index, Boolean expandBeforeSelect) {
        if (index < 0) {
            throw new InvalidArgumentException("index must be greater or equal to 0.");
        }
        String locator = String.format("%s:nth-of-type(%d)", OptionTagName, index+1);
        By optionBy = By.cssSelector(locator);
        return selectOption(optionBy, expandBeforeSelect);
    }

    public Boolean selectOption(int index) {
        return selectOption(index, DefaultExpandBeforeSelect);
    }

    BiPredicate<String, String> equals = (s, key) -> s.trim().equals(key.trim());
    BiPredicate<String, String> equalsIgnoreCase = (s, key) -> StringUtils.equalsIgnoreCase(s.trim(), key.trim());
    BiPredicate<String, String> contains = (s, key) -> s.trim().contains(key.trim());
    BiPredicate<String, String> containsIgnoreCase = (s, key) -> StringUtils.containsIgnoreCase(s, key.trim());
    BiPredicate<String, String>[] predicates = new BiPredicate[] {equals, equalsIgnoreCase, contains, containsIgnoreCase};

    public Boolean selectOption(String key, Boolean expandBeforeSelect) {
        Boolean result = false;
        try(Logger.Timer timer = Logger.M()) {
                worker.waitPageReady();

                String innerHtml = getInnerHTML();
                if (!StringUtils.containsIgnoreCase(innerHtml, key)){
                    return false;
                }

                String[] optionTexts = getOptionTexts();
                String[] optionValues = getOptionValues();
                String[][] optionSearchContexts = new String[][]{optionTexts, optionValues};

                OptionalInt index = null;
                for (String[] context: optionSearchContexts) {
                    for (BiPredicate<String, String> predicate: predicates) {
                        index = IntStream.range(0, context.length)
                                .filter(i -> predicate.test(optionTexts[i], key))
                                .findFirst();
                        if (index.isPresent())
                            return selectOption(index.getAsInt(), expandBeforeSelect);
                    }
                }
        } catch (Exception e){

        }
        return result;
    }

    public Boolean selectOption(String key) {
        Boolean result = selectOption(key, DefaultExpandBeforeSelect);
        return result;
    }

    @Override
    public Boolean perform(String key) {
        return selectOption(key);
    }

}

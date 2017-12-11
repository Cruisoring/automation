package com.least.automation.wrappers;

/**
 * Created by wiljia on 2/08/2017.
 */

import com.least.automation.helpers.Executor;
import com.least.automation.helpers.StringExtensions;
import com.objectui.interfaces.Creatable;
import com.objectui.interfaces.IContext;
import com.objectui.utilities.Executor;
import com.objectui.utilities.StringExtensions;
import org.openqa.selenium.By;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class  UICollection<T extends UIObject> extends UIObject {
    public final static String defaultChildTextSeperator = "    ";
    public final static int defaultWaitChildrenMills = 5*1000;

    protected final Creatable factory;

    public UICollection(IContext context, By by, Integer index, Creatable<? extends UIObject> childFactory) {
        super(context, by, index);
        if (childFactory == null) {
            throw new NullPointerException("Factory method must be provided for UICollection!");
        }
        factory = childFactory;
    }

    public UICollection(IContext context, By by, Integer index, By childrenBy){
        this(context, by, index, (c, i)-> new UIObject(c, childrenBy, i));
    }

    public UICollection(IContext context, By by, By childrenBy) {
        this(context, by, null, childrenBy);
    }

    public UICollection(UIObject context, By childrenBy) {
        this(context, context.locator, childrenBy);
    }

    protected List<T> children = null;

    public List<T> getChildren() {

        return Executor.tryGet(()-> {
            if (children == null || children.size()==0) {
                UIObject aChild = factory.create(this, null);
                int childCount = Executor.tryGet(()->aChild.getElementsCount(),
                        defaultWaitChildrenMills/DefaultRetryIntervalMills,
                        DefaultRetryIntervalMills, i -> i>0);

                children=IntStream.range(0, childCount)
                        .mapToObj(i -> (T)factory.create(this, i))
                        .collect(Collectors.toList());
//                T last = children.get(childCount-1);
//                last.isDisplayed();
            }
            return children;
        });
    }

    public int size(){
        return getChildren().size();
    }

//    @Override
//    public void invalidate(){
//        super.invalidate();
//        children = null;
//    }

    public List<T> getFreshChildren() {
        invalidate();
        return getChildren();
    }

    public List<String> valuesOf(Function<T, String> getValue) {
        List<T> allChildren = getChildren();
        List<String> values = allChildren.stream().map(getValue).collect(Collectors.toList());
        return values;
    }

    public List<String> textContents(){
        return valuesOf(c -> c.getTextContent());
    }

    public T get(int index) {
        T child = (T)factory.create(this, index);
        return child;
    }

    public T get(Predicate<T> predicate) {
        List<T> all = getChildren();
        T result = all
                .stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
        return result;
    }

    public int indexOf(Predicate<T> predicate) {
        List<T> all = getChildren();
        return IntStream.range(0, all.size())
                .filter(i -> predicate.test(children.get(i)))
                .findFirst().orElse(-1);
    }

//    BiPredicate<T, String> equals = (s, key) -> s.trim().equals(key.trim());
//    BiPredicate<T, String> equalsIgnoreCase = (s, key) -> StringUtils.equalsIgnoreCase(s.trim(), key.trim());
//    BiPredicate<T, String> contains = (s, key) -> s.trim().contains(key.trim());
//    BiPredicate<T, String> containsIgnoreCase = (s, key) -> StringUtils.containsIgnoreCase(s, key.trim());
//    BiPredicate<T, String>[] predicates = new BiPredicate[] {equals, equalsIgnoreCase, contains, containsIgnoreCase};
//
    public T get(String keyword) {
        T result = get(t -> t.getTextContent().equals(keyword));
        if (result == null){
            result = get(t -> t.getAllText().contains(keyword));
        }
        return result;
    }

    public T get(Object... keys) {
        return get(t -> t.getAllText(), keys);
    }

    public T get(Function<T, String> extractor, Object... keys) {
        T result = get(t-> StringExtensions.containsAll(extractor.apply(t), keys));
        return result;
    }

//    @Override
//    public String getAllText() {
//        return getAllText(defaultChildTextSeperator);
//    }

//    public String getAllText(String seperator) {
//        List<String> childrenTexts = valuesOf(c -> c.getAllText());
//        return StringUtils.join(childrenTexts, seperator);
//    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        UICollection<T> another = (UICollection<T>) obj;
        if (another==null || this.parent != another.parent || this.locator != another.locator) {
            return false;
        }
        if (this.index!=null && another.index != null && this.index != another.index) {
            return false;
        }

        return this.getFreshElement().equals(another.getFreshElement());
    }

    public Boolean click(int colIndex) {
        return Executor.testUntil(()->{
            T cell = get(colIndex);
            if(cell != null) {
                cell.click();
                return true;
            }
            return false;
        }, 3000L);
    }

    public Boolean enterText(int colIndex, String text) {
        T cell = get(colIndex);
        if (cell != null) {
            UIEdit edit = new UIEdit(cell);
            edit.enterText(text);
            return true;
        }
        return false;
    }

}

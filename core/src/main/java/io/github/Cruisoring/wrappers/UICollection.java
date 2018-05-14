package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.Creatable;
import io.github.Cruisoring.interfaces.WorkingContext;
import org.openqa.selenium.By;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class  UICollection<T extends UIObject> extends UIObject {
    public final static Class UICollectionClass = UICollection.class;
    public final static String defaultChildTextSeperator = "    ";
    public final static int defaultWaitChildrenMills = 5*1000;

    public static String getClassesToken(Class... paramClasses){
        if(Arrays.stream(paramClasses).anyMatch(c -> c.isPrimitive()))
            throw new IllegalArgumentException("Primitive types are not expected");
        String result =  Arrays.stream(paramClasses).map(c -> c.getSimpleName()).collect(Collectors.joining("-"));
        return result;
    }

    public static final String commonConstrcutorKey = getClassesToken(WorkingContext.class, By.class, Integer.class);
    public static final String alternativeConstrcutorKey = getClassesToken(WorkingContext.class, By.class);
    public static final Map<String, Creatable> creatableMap = new HashMap<>();

    public static <T extends UIObject> Creatable<T> getChildFactory(Class<? extends UIObject> childClass, final By childrenBy){
        String creatableKey = String.format("%s(%s)", childClass.getName(), childrenBy.toString());
        if(creatableMap.containsKey(creatableKey))
            return (Creatable<T>) creatableMap.get(creatableKey);

        Creatable<T> creatable = null;
        try {
            Map<String, Constructor> constructorMap = Arrays.stream(childClass.getConstructors())
                    .collect(Collectors.toMap(
                            c -> getClassesToken(c.getParameterTypes()),
                            c -> c
                    ));

            if(constructorMap.containsKey(commonConstrcutorKey)){
                final Constructor constructor = constructorMap.get(commonConstrcutorKey);
                creatable = (context, index) -> {
                    try {
                        Object newInstance = constructor.newInstance(context, childrenBy, index);
                        return (T) newInstance;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return null;
                };
            } else if (constructorMap.containsKey(alternativeConstrcutorKey)){
                final Constructor constructor = constructorMap.get(alternativeConstrcutorKey);
                creatable = ((context, index) -> {
                    try {
                        return (T) constructor.newInstance(context, childrenBy);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            }

            if(creatable != null){
                creatableMap.put(creatableKey, creatable);
            }
            return creatable;
        }catch (Exception ex){
            Logger.E(ex);
            return null;
        }
    }

    protected final Creatable<T> factory;

    public UICollection(WorkingContext context, By by, Integer index, Creatable<T> childFactory) {
        super(context, by, index);
        if (childFactory == null) {
            throw new NullPointerException("Factory method must be provided for UICollection!");
        }
        factory = childFactory;
    }

    public UICollection(WorkingContext context, By by, Integer index, Class<? extends UIObject> childClass, By childrenBy){
        super(context, by, index);

        factory = getChildFactory(childClass, childrenBy);
    }

    public UICollection(UIObject context, By childrenBy) {
        this(context.parent, context.locator, null, UIObject.UIObjectClass, childrenBy);
    }

    protected final List<T> children = new ArrayList<>();

    public List<T> getChildren() {

        return Executor.tryGet(()-> {
            if (children.size()==0) {
                T aChild = factory.create(this, null);
                int childCount = Executor.tryGet(()->aChild.getElementsCount(),
                        defaultWaitChildrenMills/DefaultRetryIntervalMills,
                        DefaultRetryIntervalMills, i -> i>0);

                IntStream.range(0, childCount)
                        .forEach(i -> children.add((T)factory.create(this, i)));
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

    public <V> T get(Function<T, V> valueExtractor, Predicate<V> valuePredicate){
        final List<T> all = getChildren();
        int index = IntStream.range(0, all.size())
                .filter(i -> valuePredicate.test(valueExtractor.apply(all.get(i))))
                .findFirst().orElse(-1);
        return index < 0 ? null : all.get(index);
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

    public Boolean clickChild(int colIndex) {
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

    @Override
    public void invalidate(){
        super.invalidate();
        children.clear();
    }
}

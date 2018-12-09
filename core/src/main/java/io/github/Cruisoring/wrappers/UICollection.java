package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.MapHelper;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.Creatable;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.function.FunctionThrowable;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class  UICollection<T extends UIObject> extends UIObject {
    public final static Class UICollectionClass = UICollection.class;
    public final static String defaultChildTextSeperator = "    ";
    public final static int defaultWaitChildrenMills = 5*1000;
    public final static int DefaultChildrenValidSeconds = 10;

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
            }else {
                String constructors = constructorMap.keySet().stream().collect(Collectors.joining(", "));
                Logger.E("Constructor of %s is not enough: %s", childClass.getName(), constructors);
                String message = String.format("%s shall define at least %s or %s", childClass.getName(), commonConstrcutorKey, alternativeConstrcutorKey);
                throw new IllegalStateException(message);
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

    public UICollection(WorkingContext context, By by, Integer containerIndex, Creatable<T> childFactory) {
        super(context, by, containerIndex);
        if (childFactory == null) {
            throw new NullPointerException("Factory method must be provided for UICollection!");
        }
        factory = childFactory;
    }

    public UICollection(WorkingContext context, By by, Integer containerIndex, Class<? extends UIObject> childClass, By childrenBy){
        this(context, by, containerIndex,
                getChildFactory(childClass, childrenBy));
    }

    public UICollection(WorkingContext context, By by, Integer containerIndex, By childrenBy){
        this(context, by, containerIndex, UIObject.class, childrenBy);
    }

    public UICollection(WorkingContext context, By by, By childrenBy){
        this(context, by, 0,
                getChildFactory(UIObject.UIObjectClass, childrenBy));
    }


    public UICollection(UIObject context, By childrenBy) {
        this(context.parent, context.locator, childrenBy);
    }

    protected final List<T> children = new ArrayList<>();
    private LocalDateTime childrenValidUntil = LocalDateTime.MIN;

    private List<T> tryGetChildren() {
        T aChild = factory.create(this, null);
        int childCount = Executor.tryGet(()->aChild.getElementsCount(),
                defaultWaitChildrenMills/DefaultRetryIntervalMills,
                DefaultRetryIntervalMills, i -> i>0);

        List<T> list = new ArrayList<>();
        for(int i = 0; i<childCount; i++){
            T child = (T)factory.create(this, i);
            list.add(child);
        }

        return list;
    }

    public List<T> getChildren() {
        waitPageReady();
        if(LocalDateTime.now().isBefore(childrenValidUntil) && !children.isEmpty()){
            //Test if the last child is still valid
            T last = children.get(children.size()-1);
            //Assume the UICollection is still valid if last is displayed
            if(last.isDisplayed()){
                return children;
            }
        }

        children.clear();
        children.addAll(tryGetChildren());
        childrenValidUntil = LocalDateTime.now().plusSeconds(DefaultChildrenValidSeconds);
        if(children == null){
            Logger.D("Children of %s is null.", this);
        }
        return children;
    }

    public List<String> getChildrenHTMLs(){
        List<T> children = getChildren();
        List<String> outerHTMLs = children.stream()
                .map(child -> child.getOuterHTML())
                .collect(Collectors.toList());
        return outerHTMLs;
    }

    public int size(){
        return getChildren().size();
    }

    public List<T> getFreshChildren() {
        invalidate();
        return getChildren();
    }

    public List<String> valuesOf(FunctionThrowable<T, String> getValue) {
        List<T> allChildren = getChildren();
        List<String> values = allChildren.parallelStream()
                .map(getValue.orElse(null)).collect(Collectors.toList());
        return values;
    }

    public List<String> asClasses(){
        invalidate();
        return valuesOf(UIObject::getElementClass);
    }

    public List<String> asTexts(){
        invalidate();
        return valuesOf(UIObject::getAllText);
    }

    public T get(int index) {
        T child = (T)factory.create(this, index);
        return child;
    }

    public T getFirst(){
        int size = size();
        return size == 0 ? null : get(0);
    }

    public T getLast(){
        int size = size();
        return size == 0 ? null : get(-1);
    }

    public T get(FunctionThrowable<T, String> valueExtractor, Predicate<String>... predicates) {
        List<T> all = getChildren();
        int size = all.size();
        //Lazy to handle potential exceptions by keep nulls
        List<Lazy<String>> allStrings = all.stream()
                .map(t -> new Lazy<String>(() -> valueExtractor.apply(t)))
                .collect(Collectors.toList());

        for(Predicate<String> predicate : predicates) {
            for (int i = 0; i < size; i++) {
                String text = allStrings.get(i).getValue();
                if(predicate.test(text))
                    return all.get(i);
            }
        }

        return null;
    }

    public T get(Pattern childPattern, String text){
        List<String> childrenTexts = StringExtensions.getTexts(this.getOuterHTML(), childPattern, true);
        String key = MapHelper.bestMatchedKey(childrenTexts, text);
        if(key == null)
            return null;
        int index = childrenTexts.indexOf(key);
        return get(index);
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
        T result = get(t -> t.getElement().getText(),
                text -> StringUtils.equals(keyword, text)
                , text -> StringUtils.equalsIgnoreCase(keyword, text)
                , text -> StringUtils.containsIgnoreCase(text, keyword)
        );
        return result;
    }

    public T get(Object... keys) {
        return get(t -> t.getAllText(), keys);
    }

    public T get(Function<T, String> extractor, Object... keys) {
        List<T> all = getChildren();
        int size = all.size();
        for (int i = 0; i < size; i++) {
            T t = all.get(i);
            String text = extractor.apply(t);
            if(StringExtensions.containsAll(text, keys))
                return t;
        }

        return null;
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

package io.github.Cruisoring.helpers;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ClassHelper {
    public static final String className = ClassHelper.class.getName();
    public static final Predicate<StackTraceElement> callerOnlyPredicate = s -> !StringUtils.equalsIgnoreCase(s.getClassName(), className);

    public static Class<?> getCallerClass(){
        return getCallerClass(callerOnlyPredicate);
    }

    public static Class<?> getCallerClass(Predicate<StackTraceElement> predicate){
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        int index = ArrayUtils.indexOf(stElements, s -> !callerOnlyPredicate.test(s));
        index = ArrayUtils.indexOf(stElements, index, callerOnlyPredicate);
        if(predicate != null)
            index = ArrayUtils.indexOf(stElements, index, predicate);

        if(index == -1)
            return null;

        String classname = stElements[index].getClassName();
        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }


    public static Class<?> findSubClassParaterType(Class<?> subClass, Class<?> classOfInterest, int parameterIndex) {
        Map<Type, Type> typeMap = new HashMap<Type, Type>();
        while (classOfInterest != subClass.getSuperclass()) {
            extractTypeArguments(typeMap, subClass);
            subClass = subClass.getSuperclass();
            if (subClass == null) throw new IllegalArgumentException();
        }

        ParameterizedType parameterizedType = (ParameterizedType) subClass.getGenericSuperclass();
        Type actualType = parameterizedType.getActualTypeArguments()[parameterIndex];
        if (typeMap.containsKey(actualType)) {
            actualType = typeMap.get(actualType);
        }

        if (actualType instanceof Class) {
            return (Class<?>) actualType;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * retreiving generic type arguments
     * @see "https://www.javacodegeeks.com/2013/12/advanced-java-generics-retreiving-generic-type-arguments.html"
     * @param instance
     * @param classOfInterest
     * @param parameterIndex
     * @return
     */
    public static Class<?> findSubClassParameterType(Object instance, Class<?> classOfInterest, int parameterIndex) {
        Map<Type, Type> typeMap = new HashMap<Type, Type>();
        Class<?> instanceClass = instance.getClass();
        while (classOfInterest != instanceClass.getSuperclass()) {
            extractTypeArguments(typeMap, instanceClass);
            instanceClass = instanceClass.getSuperclass();
            if (instanceClass == null) throw new IllegalArgumentException();
        }

        ParameterizedType parameterizedType = (ParameterizedType) instanceClass.getGenericSuperclass();
        Type actualType = parameterizedType.getActualTypeArguments()[parameterIndex];
        if (typeMap.containsKey(actualType)) {
            actualType = typeMap.get(actualType);
        }

        if (actualType instanceof Class) {
            return (Class<?>) actualType;
        } else if (actualType instanceof TypeVariable) {
            return browseNestedTypes(instance, (TypeVariable<?>) actualType);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static void extractTypeArguments(Map<Type, Type> typeMap, Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return;
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] typeParameter = ((Class<?>) parameterizedType.getRawType()).getTypeParameters();
        Type[] actualTypeArgument = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < typeParameter.length; i++) {
            if(typeMap.containsKey(actualTypeArgument[i])) {
                actualTypeArgument[i] = typeMap.get(actualTypeArgument[i]);
            }
            typeMap.put(typeParameter[i], actualTypeArgument[i]);
        }
    }

    private static Class<?> browseNestedTypes(Object instance, TypeVariable<?> actualType) {
        Class<?> instanceClass = instance.getClass();
        List<Class<?>> nestedOuterTypes = new LinkedList<Class<?>>();
        for (
                Class<?> enclosingClass = instanceClass.getEnclosingClass();
                enclosingClass != null;
                enclosingClass = enclosingClass.getEnclosingClass()) {
            try {
                Field this$0 = instanceClass.getDeclaredField("this$0");
                Object outerInstance = this$0.get(instance);
                Class<?> outerClass = outerInstance.getClass();
                nestedOuterTypes.add(outerClass);
                Map<Type, Type> outerTypeMap = new HashMap<Type, Type>();
                extractTypeArguments(outerTypeMap, outerClass);
                for (Map.Entry<Type, Type> entry : outerTypeMap.entrySet()) {
                    if (!(entry.getKey() instanceof TypeVariable)) {
                        continue;
                    }
                    TypeVariable<?> foundType = (TypeVariable<?>) entry.getKey();
                    if (foundType.getName().equals(actualType.getName())
                            && isInnerClass(foundType.getGenericDeclaration(), actualType.getGenericDeclaration())) {
                        if (entry.getValue() instanceof Class) {
                            return (Class<?>) entry.getValue();
                        }
                        actualType = (TypeVariable<?>) entry.getValue();
                    }
                }
            } catch (NoSuchFieldException e) { /* this should never happen */ } catch (IllegalAccessException e) { /* this might happen */}

        }
        throw new IllegalArgumentException();
    }

    private static boolean isInnerClass(GenericDeclaration outerDeclaration, GenericDeclaration innerDeclaration) {
        if (!(outerDeclaration instanceof Class) || !(innerDeclaration instanceof Class)) {
            throw new IllegalArgumentException();
        }
        Class<?> outerClass = (Class<?>) outerDeclaration;
        Class<?> innerClass = (Class<?>) innerDeclaration;
        while ((innerClass = innerClass.getEnclosingClass()) != null) {
            if (innerClass == outerClass) {
                return true;
            }
        }
        return false;
    }
}

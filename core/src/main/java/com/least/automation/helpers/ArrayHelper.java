package com.least.automation.helpers;

import java.util.function.Predicate;
import java.util.stream.IntStream;

public class ArrayHelper {

    public static <T> int indexOf(T[] array, Predicate<T> predicate){
        return indexOf(array, 0, predicate);
    }

    public static <T> int indexOf(T[] array, int fromIndex, Predicate<T> predicate){
        int maxIndex = array.length -1;
        if (fromIndex < 0 || fromIndex > maxIndex)
            return -1;

        return IntStream.range(fromIndex, maxIndex+1)
                .filter(i -> predicate.test(array[i]))
                .findFirst()
                .orElse(-1);
    }

    public static <T> int lastndexOf(T[] array, Predicate<T> predicate){
        return lastIndexOf(array, 0, predicate);
    }

    public static <T> int lastIndexOf(T[] array, int fromIndex, Predicate<T> predicate){
        int maxIndex = array.length -1;
        if (fromIndex < 0 || fromIndex > maxIndex)
            return -1;

        return IntStream.range(fromIndex, maxIndex+1)
                .map(i -> maxIndex-i)
                .filter(i -> predicate.test(array[i]))
                .findFirst()
                .orElse(-1);
    }
}
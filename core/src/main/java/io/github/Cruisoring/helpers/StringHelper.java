package io.github.Cruisoring.helpers;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

public class StringHelper {
    public static final Function<Object, String[]> defaultToStringForms = o -> new String[]{o.toString()};
    public static final BiPredicate<String, String> contains = (s, k) -> StringUtils.contains(s, k);
    public static final BiPredicate<String, String> containsIgnoreCase = (s, k) -> StringUtils.containsIgnoreCase(s, k);

    public static String asString(Object value) {
        if (value == null)
            return "";
        else if (value instanceof Date)
            return DateTimeHelper.dateString((Date)value);
        else if (value instanceof LocalDate)
            return DateTimeHelper.dateString((LocalDate)value);
        else if (value instanceof LocalDateTime)
            return DateTimeHelper.dateTimeString((LocalDateTime)value);
        else if (value instanceof Duration)
            return DateTimeHelper.durationStringOf((Duration)value);
        else
            return value.toString();
    }



    public static Boolean containsIgnoreCase(Object[] values, Object key){
        if(key == null || values == null || values.length == 0)
            return false;

        return Arrays.stream(values).anyMatch(s ->
                s != null && StringUtils.equalsIgnoreCase(s.toString(), key.toString()));
    }

    private static final Boolean matchAny(BiPredicate<String, String> matcher, String context, String[] keys) {
        if (context == null) return false;
        return Arrays.stream(keys).anyMatch(k -> matcher.test(context, k));
    }

    public static Boolean containsAll(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) return false;

        return Arrays.stream(keys)
                .filter(o -> o != null)
                .allMatch(o -> matchAny(contains, context, toStringForms.apply(o)));
    }

    public static Boolean containsAll(String context, Object... keys) {
        return containsAll(context, defaultToStringForms, keys);
    }

    public static Boolean containsAllIgnoreCase(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) return false;

        return Stream.of(keys)
                .filter(o -> o != null)
                .allMatch(o ->
                        matchAny(containsIgnoreCase, context,
                                toStringForms.apply(o)));
    }

    public static Boolean containsAllIgnoreCase(String context, Object... keys) {
        return containsAllIgnoreCase(context, defaultToStringForms, keys);
    }

    public static Boolean containsAny(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) {
            return false;
        }
        return Arrays.stream(keys).filter(o -> o != null)
                .anyMatch(o -> matchAny(contains, context, toStringForms.apply(o)));
    }

    public static Boolean containsAny(String context, Object... keys) {
        return containsAny(context, defaultToStringForms, keys);
    }

    public static Boolean containsAny(String context, String... keys) {
        return containsAny(context, defaultToStringForms, (Object[]) keys);
    }

    public static Boolean containsAnyIgnoreCase(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) {
            return false;
        }
        return Arrays.stream(keys).filter(o -> o != null).parallel()
                .anyMatch(o -> matchAny(containsIgnoreCase, context, toStringForms.apply(o)));
    }

    public static Boolean containsAnyIgnoreCase(String context, Object... keys) {
        return containsAnyIgnoreCase(context, defaultToStringForms, keys);
    }


}

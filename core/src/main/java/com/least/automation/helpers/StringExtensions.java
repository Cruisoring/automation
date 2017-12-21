package com.least.automation.helpers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StringExtensions {

    public static final Function<Object, String[]> defaultToStringForms = o -> new String[]{o.toString()};
    public static final BiPredicate<String, String> contains = (s, k) -> StringUtils.contains(s, k);
    public static final BiPredicate<String, String> containsIgnoreCase = (s, k) -> StringUtils.containsIgnoreCase(s, k);

    public static final String NewLine = System.getProperty("line.separator");

    public static String extractHtmlText(String html) {
        if (html == null) return null;

        String result = html.replaceAll("\\b(?!value)([-|_|\\w]+)=\\\"[^*]*?\\\"", "");
        result = result.replaceAll("<input\\W+[^>]*value=\\\"([^\"]*)\\\"[^\\>]*>", "$1");
        String unescaped = StringEscapeUtils.unescapeHtml4(result);
        result = unescaped.replaceAll("<[^>]*>", "");
        return result;
    }

    public static List<String> getSegments(String html, Pattern pattern) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = pattern.matcher(html);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    public static final Pattern simpleTableRowPattern = Pattern.compile("<tbody></tbody>");

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

    public static int indexOfAny(String context, int fromIndex, Object... keys) {
        if (keys.length == 0)
            return -1;

        return Arrays.stream(keys).map(k -> context.indexOf(k.toString(), fromIndex))
                .filter(i -> i != -1).min(Integer::min).orElse(-1);
    }

    public static final Pattern escapeCharsPattern = Pattern.compile("(\\[|\\\\|\\^|\\$|\\.|\\||\\?|\\*|\\+|\\(|\\)|\\{|\\})");
    public static String escapeRegexChars(String template){
        Matcher matcher = escapeCharsPattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
            sb.append("\\" + matcher.group(1));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String replaceAll(String template, Map<String, String> tokens) {
        // Create pattern of the format "%(token0|token1|...)%"
        Object[] quotedKeys = tokens.keySet().stream().map(s -> escapeRegexChars(s)).toArray();
        String patternString = String.format("(%s)", StringUtils.join(quotedKeys, '|'));
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
            String replacement= tokens.get(matcher.group(1));
            sb.append(replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static List<String> sortedListOf(Collection<String> strings, Comparator<String> comparator){
        List<String> list = (strings instanceof List) ? (List<String>)strings : new ArrayList<>(strings);
        Collections.sort(list, comparator);
        return list;
    }

    public static List<String> sortedListByLength(Collection<String> strings){
        return sortedListOf(strings, (l, r) -> Integer.compare(l.length(), r.length()));
    }

    public static List<String> sortedListByLengthDesc(Collection<String> strings){
        return sortedListOf(strings, (l, r) -> Integer.compare(r.length(), l.length()));
    }

    public static String firstMatch(String template, Collection<String> keys, BiPredicate<String, String> predicate) {
        if(template == null)
            return null;
        return keys.stream()
                .filter(k -> k != null && predicate.test(template, k))
                .findFirst().orElse(null);
    }

    public static String firstContains(String template, Collection<String> keys) {
        return firstMatch(template, keys, String::contains);
    }

    public static String firstStartsWith(String template, Collection<String> keys) {
//        keys = sortedListByLengthDesc(keys);
        return firstMatch(template, keys, String::startsWith);
    }

    public static String firstEndsWith(String template, Collection<String> keys) {
//        keys = sortedListByLengthDesc(keys);
        return firstMatch(template, keys, String::endsWith);
    }
}

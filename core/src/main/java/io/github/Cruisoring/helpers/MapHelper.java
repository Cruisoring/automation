package io.github.Cruisoring.helpers;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Helper to retrieve Map values with String keys ignoring case.
 */
public class MapHelper {
    /**
     * Get the best matched value of a list of Strings.
     *
     * @param list The list of Strings to be compared.
     * @param key  The key to be matched.
     * @return If there is any value matched exatly with the given key, then return it immediately;
     * or return the first value case-ignored equalled with the given key.
     * Otherwise, return null.
     */
    public static String bestMatchedKey(List<String> list, String key) {
        key = key.trim();
        if (StringUtils.isEmpty(key)) {
            Logger.E("key is not supposed to be empty!");
            return null;
        }

        List<String> matched = new ArrayList();
        List<String> contains = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String v = list.get(i);
            if (StringUtils.equals(v, key))
                return v;
            else if (StringUtils.equalsIgnoreCase(v, key)) {
                matched.add(v);
            } else if (StringUtils.containsIgnoreCase(v, key)) {
                contains.add(v);
            }
        }
        if (matched.size() == 0 && contains.size() == 0) {
            Logger.V("No maching of: %s", key);
            return null;
        } else if (matched.size() == 1) {
            return matched.get(0);
        }

        if (contains.size() == 1) {
            return contains.get(0);
        } else {
            contains.sort((s1, s2) -> s1.length() - s2.length());
            return contains.get(0);
        }
    }

    /**
     * Get the best matched key contained by the text.
     *
     * @param text The text to be searched
     * @param keys The list of keys.
     * @return If there is any case-incensitive key contained by the text, return it immediately.
     * Otherwise, return null.
     */
    public static String containsKeyIgnoreCase(String text, List<String> keys) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(keys);

        int matchedKeyIndex = IntStream.range(0, keys.size())
                .filter(i -> StringUtils.indexOfIgnoreCase(text, keys.get(i)) != -1)
                .findFirst().orElse(-1);

        return matchedKeyIndex == -1 ? null : keys.get(matchedKeyIndex);
    }

    /**
     * Check if the map contains an entry with the given case-insensitive key.
     *
     * @param map Map instance to be checked
     * @param key Case-insensitive key.
     * @param <T> The value of type T.
     * @return True if there is a case-ignored matching, otherwise False.
     */
    public static <T> boolean containsIgnoreCase(Map<String, T> map, String key) {
        List<String> keys = map.keySet().stream().collect(Collectors.toList());
        return bestMatchedKey(keys, key) != null;
    }

    /**
     * Fetch the best matched value of a map identified with String keys.
     *
     * @param map The dictionary with String keys.
     * @param key The String to be used to match with the dictionary String keys.
     * @param <T> The value of type T.
     * @return The value of type T that best matched with the given key.
     */
    public static <T> T getIgnoreCase(Map<String, T> map, String key) {
        Set<String> keySet = map.keySet();
        List<String> keyList = keySet.stream().collect(Collectors.toList());
        String matched = bestMatchedKey(keyList, key);
        if (matched == null) {
//            log.error("No Key of '" + key + "' from: " + String.join(", ", keyList));
            return null;
        }
        return map.get(matched);
    }

    public static <T> T tryGetWithMultipleKeys(Map<String, T> map, String... keys) {
        try {
            String key;
            T value;
            for (int i = 0; i < keys.length; i++) {
                key = keys[i];
                value = getIgnoreCase(map, key);
                if (value != null) {
                    return value;
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static String tryGetWithMultipleKeys(Properties properties, String... acceptableKeys) {
        List<String> propertyKeys = new ArrayList<>(properties.stringPropertyNames());
        List<String> expectedKeys = Arrays.asList(acceptableKeys);
        Optional<String> matched = propertyKeys.stream()
                .filter(key -> expectedKeys.contains(key))
                .findFirst();
        if (matched.isPresent())
            return properties.getProperty(matched.get());

        matched = propertyKeys.stream()
                .filter(key -> expectedKeys.stream().anyMatch(expect -> key.equalsIgnoreCase(expect)))
                .findFirst();
        if (matched.isPresent())
            return properties.getProperty(matched.get());
        else
            return null;
    }

    public static Map<String, String> fromProperties(Properties properties, Map<String, String> map) {
        if (properties == null)
            return map;

        if (map == null) {
            map = new HashMap<String, String>();
        }
        for (String propertyName :
                properties.stringPropertyNames()) {
            map.put(propertyName, properties.getProperty(propertyName));
        }
        return map;
    }

    /**
     * Convert the map to Json format with keys sorted.
     * @param map   Map to be converted.
     * @return      String of Json format.
     */
    public static String asJson(Map<?,?> map){
        String json = "{" + map.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .map(entry -> String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(",")) + "}";
        return json;
    }

    /**
     * Convert the map to Json format with keys sorted.
     * @param map   Map to be converted.
     * @return      String of Json format.
     */
    public static String asFormattedJson(Map<?,?> map){
        String json = "{\n" + map.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .map(entry -> String.format("\t\"%s\": \"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(",\n")) + "\n}";
        return json;
    }
}
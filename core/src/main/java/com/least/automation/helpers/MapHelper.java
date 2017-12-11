package com.least.automation.helpers;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper to retrieve Map values with String keys ignoring case.
 */
public class MapHelper {
    /**
     * Get the best matched value of a list of Strings.
     * @param list  The list of Strings to be compared.
     * @param key   The key to be matched.
     * @return  If there is any value matched exatly with the given key, then return it immediately;
     *  Otherwise, return the first value case-ignored equalled with the given key.
     */
    public static String bestMatchedKey(List<String> list, String key){
        key = key.trim();
        if (StringUtils.isEmpty(key)){
            Logger.E("key is not supposed to be empty!");
            return null;
        }

        String ignoreCased = null;
        for(int i = 0; i < list.size(); i++){
            String v = list.get(i);
            if(StringUtils.equals(v, key))
                return v;
            else if (StringUtils.equalsIgnoreCase(v, key)){
                if(ignoreCased == null){
                    ignoreCased = v;
                } else {
                    String msg = String.format("Multiple values matched with '%s': '%s' or '%s'?", key, ignoreCased, v);
                    Logger.W(msg);
                }
            }
        }
        return ignoreCased;
    }

    /**
     * Fetch the best matched value of a map identified with String keys.
     * @param map   The dictionary with String keys.
     * @param key   The String to be used to match with the dictionary String keys.
     * @param <T>   The value of type T.
     * @return      The value of type T that best matched with the given key.
     */
    public static <T> T getIgnoreCase(Map<String, T> map, String key){
        Set<String> keySet = map.keySet();
        List<String> keyList = keySet.stream().collect(Collectors.toList());
        String matched = bestMatchedKey(keyList, key);
        if(matched == null){
//            Logger.E("No Key of '" + key + "' from: " + String.join(", ", keyList));
            return null;
        }
        return map.get(matched);
    }
}

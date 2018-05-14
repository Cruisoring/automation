package io.github.Cruisoring.helpers;

/**
 * Utility to convert Enum to/from String.
 */
public class EnumHelper {
    public static <E extends Enum<E>> E toEnum(Class<E> e, String id) {
        try {
            E result = Enum.valueOf(e, id);
            return result;
        } catch (IllegalArgumentException ex) {
            Logger.W("Invalid value for enum %s from %s", e.getSimpleName(), id);
            throw ex;
        }
    }
    public static <E extends Enum<E>> E toEnum(String id, E defaultValue) {
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), id);
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }
}

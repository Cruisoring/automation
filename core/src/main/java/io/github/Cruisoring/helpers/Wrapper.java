package io.github.Cruisoring.helpers;

/**
 * Created by wiljia on 31/08/2017.
 */
public class Wrapper <T> {

    public final String name;
    public final T value;

    public Wrapper(T value, String name){
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, value);
    }
}

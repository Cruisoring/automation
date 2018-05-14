package io.github.Cruisoring.interfaces;

@FunctionalInterface
public interface TriConsumer<T,U,S> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param s the third function argument
     */
    void accept(T t, U u, S s) throws Exception;
}

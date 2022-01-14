package mx.kenzie.fern.handler;

import mx.kenzie.fern.FernParser;

public interface ValueHandler<T> {
    boolean matches(final String string);
    
    default T parse(final String string, final FernParser parser) {
        return parse(string);
    }
    
    T parse(final String string);
    
    default boolean isOfType(Object thing) {
        return false;
    }
    
    default String toString(T thing) {
        return thing + "";
    }
    
}

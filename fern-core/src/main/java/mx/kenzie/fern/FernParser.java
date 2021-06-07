package mx.kenzie.fern;

import mx.kenzie.fern.handler.ValueHandler;
import mx.kenzie.fern.parser.Parser;

import java.util.List;

public interface FernParser extends Parser<FernBranch> {
    
    default ValueHandler<?> getHandler(final String value) {
        for (ValueHandler<?> handler : getHandlers()) {
            if (handler.matches(value)) return handler;
        }
        throw new IllegalArgumentException("No handler matches: '" + value + "'");
    }
    
    List<ValueHandler<?>> getHandlers();
    
    default ValueHandler<?> getTypeHandler(final Object object) {
        for (ValueHandler<?> handler : getHandlers()) {
            if (handler.isOfType(object)) return handler;
        }
        throw new IllegalArgumentException("No handler matches: '" + object.getClass() + "'");
    }
    
    void parseMap(final String content, final FernBranch branch);
    
    mx.kenzie.fern.Fern parseElement(final String string);
    
    <T> Query<T> parseQuery(final String string);
    
    boolean matches(final Object object, final String query);
    
    boolean matches(final Object object, final FernBranch query);
    
    default String getIndentationUnit() {
        return "    ";
    }
    
}

package mx.kenzie.fern.handler.logic;

import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.Query;
import mx.kenzie.fern.handler.ValueHandler;

public class JunctionHandler implements ValueHandler<Query<Boolean>> {
    @Override
    public boolean matches(String string) {
        return string.startsWith("∧")
            || string.startsWith("∨")
            || string.startsWith("⊻");
    }
    
    @Override
    public Query<Boolean> parse(String string, FernParser parser) {
        final String value = string.substring(1).trim();
        final Object object = parser.getHandler(value).parse(value, parser);
        if (!(object instanceof Boolean boo)) return thing -> false;
        return switch (string.substring(0, 1)) {
            case "∧" -> thing -> thing && boo;
            case "∨" -> thing -> thing || boo;
            case "⊻" -> thing -> thing ^ boo;
            default -> throw new IllegalStateException();
        };
    }
    
    @Override
    public Query<Boolean> parse(String string) {
        throw new IllegalStateException("Query handler cannot parse input without FernParser instance.");
    }
    
}


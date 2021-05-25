package mx.kenzie.fern.handler.logic;

import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.Query;
import mx.kenzie.fern.handler.ValueHandler;

public class CompareHandler implements ValueHandler<Query<?>> {
    @Override
    public boolean matches(String string) {
        return string.startsWith("<")
            || string.startsWith(">")
            || string.startsWith("=")
            || string.startsWith("≠")
            || string.startsWith("!=");
    }
    
    @Override
    public Query<?> parse(String string, FernParser parser) {
        final String value;
        if (string.startsWith("<=")
            || string.startsWith(">=")
            || string.startsWith("==")
            || string.startsWith("!=")) {
            value = string.substring(2).trim();
            final Object object = parser.getHandler(value).parse(value, parser);
            return switch (string.substring(0, 2)) {
                case "<=" -> thing -> ((Number) thing).doubleValue() <= ((Number) object).doubleValue();
                case ">=" -> thing -> ((Number) thing).doubleValue() >= ((Number) object).doubleValue();
                case "==" -> object::equals;
                case "!=" -> thing -> !object.equals(thing);
                default -> throw new IllegalStateException();
            };
        } else if (string.startsWith("<")
            || string.startsWith(">")
            || string.startsWith("=")
            || string.startsWith("≠")) {
            value = string.substring(1).trim();
            final Object object = parser.getHandler(value).parse(value, parser);
            return switch (string.substring(0, 1)) {
                case "<" -> thing -> ((Number) thing).doubleValue() < ((Number) object).doubleValue();
                case ">" -> thing -> ((Number) thing).doubleValue() > ((Number) object).doubleValue();
                case "=" -> object::equals;
                case "≠" -> thing -> !object.equals(thing);
                default -> throw new IllegalStateException();
            };
        }
        throw new IllegalArgumentException("Value input does not match expected types: '" + string + "'");
    }
    
    @Override
    public Query<?> parse(String string) {
        throw new IllegalStateException("Query handler cannot parse input without FernParser instance.");
    }
    
}


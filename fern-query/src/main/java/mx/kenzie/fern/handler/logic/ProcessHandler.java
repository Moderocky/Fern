package mx.kenzie.fern.handler.logic;

import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.Process;
import mx.kenzie.fern.handler.ValueHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProcessHandler implements ValueHandler<Process<?, ?>> {
    @Override
    public boolean matches(String string) {
        return string.startsWith("∪")
            || string.startsWith("∩");
    }
    
    @Override
    public Process<?, ?> parse(String string, FernParser parser) {
        final String value = string.substring(1).trim();
        final Object object = parser.getHandler(value).parse(value, parser);
        final Checker checker = new Checker(ContainsHandler.convertToCollection(object));
        return switch (string.substring(0, 1)) {
            case "∪" -> checker::union;
            case "∩" -> checker::intersection;
            default -> throw new IllegalStateException();
        };
    }
    
    @Override
    public Process<?, ?> parse(String string) {
        throw new IllegalStateException("Process handler cannot parse input without FernParser instance.");
    }
    
    protected static record Checker(Collection<?> object) {
        
        public Collection<?> union(Object thing) {
            final List<Object> list = new ArrayList<>(ContainsHandler.convertToCollection(thing));
            for (Object o : object) {
                if (!list.contains(o)) list.add(o);
            }
            return list;
        }
        
        public Collection<?> intersection(Object thing) {
            final List<Object> list = new ArrayList<>();
            for (Object o : ContainsHandler.convertToCollection(thing)) {
                if (object.contains(o)) list.add(o);
            }
            return list;
        }
        
    }
    
}


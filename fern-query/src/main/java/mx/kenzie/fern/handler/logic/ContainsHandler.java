package mx.kenzie.fern.handler.logic;

import mx.kenzie.fern.FernList;
import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.Query;
import mx.kenzie.fern.handler.ValueHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ContainsHandler implements ValueHandler<Query<?>> {
    static Collection<Object> convertToCollection(Object object) {
        final Collection<Object> collection;
        if (object instanceof Object[] objects)
            collection = Arrays.asList(objects);
        else if (object instanceof FernList list)
            collection = list.toRawList();
        else if (object instanceof Collection<?> objects)
            collection = new ArrayList<>(objects);
        else collection = Collections.singletonList(object);
        return collection;
    }
    
    @Override
    public boolean matches(String string) {
        return string.startsWith("∈")
            || string.startsWith("∉")
            || string.startsWith("⊃")
            || string.startsWith("⊂");
    }
    
    @Override
    public Query<?> parse(String string, FernParser parser) {
        final String value = string.substring(1).trim();
        final Object object = parser.getHandler(value).parse(value, parser);
        final Checker checker = new Checker(object);
        return switch (string.substring(0, 1)) {
            case "∈" -> checker::isIn;
            case "∉" -> checker::notIn;
            case "⊃" -> checker::superset;
            case "⊂" -> checker::subset;
            default -> throw new IllegalStateException();
        };
    }
    
    @Override
    public Query<?> parse(String string) {
        throw new IllegalStateException("Query handler cannot parse input without FernParser instance.");
    }
    
    protected record Checker(Object object) {
        
        public boolean subset(Object thing) {
            return isSubset(object, thing)
                || isIn(thing);
        }
        
        private boolean isSubset(Object a, Object b) {
            final Collection<?> c1 = convertToCollection(a);
            final Collection<?> c2 = convertToCollection(b);
            return c1.containsAll(c2);
        }
        
        public boolean isIn(Object thing) {
            try {
                if (object instanceof Collection<?> collection)
                    return collection.contains(thing);
                else if (object instanceof Object[] array)
                    return Arrays.asList(array).contains(thing);
                else if (object instanceof String string) {
                    return string.contains(thing + "");
                }
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
            return false;
        }
        
        public boolean superset(Object thing) {
            if (thing instanceof Boolean a && object instanceof Boolean b)
                return b || !a;
            return isSubset(thing, object)
                || new Checker(thing).isIn(object);
        }
        
        public boolean notIn(Object thing) {
            return !isIn(thing);
        }
        
    }
    
}


package mx.kenzie.fern;

import mx.kenzie.fern.handler.PrepareQueryHandler;
import mx.kenzie.fern.handler.logic.CompareHandler;
import mx.kenzie.fern.handler.logic.ContainsHandler;
import mx.kenzie.fern.handler.logic.JunctionHandler;
import mx.kenzie.fern.handler.logic.ProcessHandler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QueriableFernParser extends GenericFernParser {
    
    {
        handlers.add(new PrepareQueryHandler());
        handlers.add(new CompareHandler());
        handlers.add(new ContainsHandler());
        handlers.add(new JunctionHandler());
        handlers.add(new ProcessHandler());
    }
    
    @Override
    public <T> Query<T> parseQuery(final String string) {
        final Object value = getHandler(string).parse(string, this);
        assert value instanceof Query;
        return (Query<T>) value;
    }
    
    @Override
    public boolean matches(final Object object, final String query) {
        final FernBranch tree = this.parse(query);
        return matches(object, tree);
    }
    
    @Override
    public boolean matches(final Object object, final FernBranch tree) {
        final Class<?> type = object.getClass();
        final Map<String, Object> map = new HashMap<>();
        for (final Field field : type.getDeclaredFields()) {
            if (!field.isAccessible()) field.setAccessible(true);
            final Export export = field.getAnnotation(Export.class);
            final String name = export == null ? field.getName() : export.value();
            try {
                map.put(name, field.get(object));
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        for (Map.Entry<String, Fern> entry : tree.entrySet()) {
            final Object value = map.get(entry.getKey());
            if (value == null) return false;
            if (entry.getValue().isBranch()) {
                final boolean result = matches(value, entry.getValue().getAsBranch());
                if (!result) return false;
            } else if (entry.getValue() instanceof FernList list) {
                for (Fern fern : list) {
                    if (!compareFern(value, fern.getAsLeaf())) return false;
                }
            } else {
                final boolean result = compareFern(value, entry.getValue().getAsLeaf());
                if (!result) return false;
            }
        }
        return true;
    }
    
    protected boolean compareFern(final Object value, final FernLeaf<?> leaf) {
        final Object object = leaf.getRawValue();
        if (object instanceof Query query) {
            return query.compare(value);
        }
        return Objects.equals(value, object);
    }
}

package mx.kenzie.fern;

import mx.kenzie.fern.handler.*;
import mx.kenzie.fern.handler.flyover.FlyoverMapHandler;
import mx.kenzie.fern.parser.FlyoverStreamReader;
import mx.kenzie.fern.parser.ParserBase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FlyoverFernParser implements FernParser, ParserBase<InputStream> {
    
    protected final List<ValueHandler<?>> handlers = new ArrayList<>();
    
    {
        handlers.add(new NullHandler());
        handlers.add(new BooleanHandler());
        handlers.add(new StringHandler());
        handlers.add(new FlyoverMapHandler());
        handlers.add(new ListHandler());
        handlers.add(new IntegerHandler());
        handlers.add(new ShortHandler());
        handlers.add(new ByteHandler());
        handlers.add(new LongHandler());
        handlers.add(new DoubleHandler());
        handlers.add(new FloatHandler());
        handlers.add(new ColourHandler());
        handlers.add(new UUIDHandler());
        handlers.add(new ClassHandler());
    }
    
    @Override
    public List<ValueHandler<?>> getHandlers() {
        return handlers;
    }
    
    @Override
    public Fern parseElement(String string) {
        for (ValueHandler<?> handler : handlers) {
            if (handler.matches(string)) {
                final Object value = handler.parse(string, this);
                if (value instanceof Fern fern) return fern;
                return new FernLeaf<>(value, (ValueHandler<Object>) handler);
            }
        }
        throw new IllegalStateException("Unable to parse unknown type: '" + string + "'");
    }    public void parseMap(final InputStream stream, final FernBranch branch) {
        try {
            final FlyoverStreamReader reader = new FlyoverStreamReader(stream);
            do {
                String key;
                do {
                    key = reader.readUntilDeadEnd(false, ')', ' ', '\t').trim();
                    if (key.isEmpty()) break;
                } while (key.isBlank());
                final String value = reader.readUntilNested(true, ',').trim();
                if (key.isBlank() || value.isBlank()) return;
                branch.put(key, this.parseElement(value));
            } while (true);
        } catch (FlyoverStreamReader.DeadEnd ignore) {
            ignore.printStackTrace();
        }
    }
    
    //region Query Handling
    @Override
    public <T> Query<T> parseQuery(String string) {
        throw new IllegalStateException("Lazy parser cannot parse query logic.");
    }
    
    @Override
    public boolean matches(Object object, String query) {
        throw new IllegalStateException("Lazy parser cannot parse query logic.");
    }
    
    @Override
    public boolean matches(Object object, FernBranch query) {
        throw new IllegalStateException("Lazy parser cannot parse query logic.");
    }
    
    //region Object Mapping
    @Override
    public <Type extends Serializable> Type objectMap(FernBranch branch, Class<Type> cls)
        throws IllegalStateException {
        if (!FernUnsafe.isValid()) throw new IllegalStateException("Mapping is unavailable in this environment.");
        final Type target = FernUnsafe.createForSerialisation(cls);
        assert target != null;
        return objectMapWrite(branch, target);
    }
    //endregion
    
    @Override
    public <Type extends Serializable> Type objectMapWrite(FernBranch branch, Type target)
        throws IllegalStateException {
        final List<Field> fields = FernUnsafe.getFields(target.getClass());
        for (final Field field : fields) {
            final Class<?> owner = field.getDeclaringClass();
            if (!branch.containsKey(owner.getSimpleName())) continue;
            final FernBranch sub = branch.get(owner.getSimpleName()).getAsBranch();
            if (!sub.containsKey(field.getName())) continue;
            FernUnsafe.setValue(target, field, sub.get(field.getName()).getRawValue());
        }
        return target;
    }
    
    @Override
    public <Type extends Serializable> FernBranch unMap(Type target) {
        final List<Field> fields = FernUnsafe.getFields(target.getClass());
        final FernBranch branch = new FernBranch();
        for (final Field field : fields) {
            final Class<?> owner = field.getDeclaringClass();
            branch.putIfAbsent(owner.getSimpleName(), new FernBranch());
            final FernBranch sub = branch.get(owner.getSimpleName()).getAsBranch();
            if (!sub.containsKey(field.getName())) continue;
            sub.put(field.getName(), serialise(FernUnsafe.getValue(target, field)));
        }
        return branch;
    }
    
    @Override
    public Fern serialise(Object object) {
        for (ValueHandler<?> handler : handlers) {
            if (!handler.isOfType(object)) continue;
            return new FernLeaf<>(object, (ValueHandler) handler);
        }
        if (!FernUnsafe.isValid()) return null;
        if (!(object instanceof Serializable serializable)) return null;
        return unMap(serializable);
    }
    //endregion
    

    
    @Override
    public FernBranch parse(String string) {
        return parse(new ByteArrayInputStream(string.getBytes()));
    }
    
    @Override
    public FernBranch parse(InputStream resource) {
        final FernBranch root = new FernBranch();
        parseMap(resource, root);
        return root;
    }
    
}

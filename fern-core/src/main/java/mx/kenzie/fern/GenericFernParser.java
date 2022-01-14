package mx.kenzie.fern;

import mx.kenzie.fern.handler.*;
import mx.kenzie.fern.parser.BracketReader;
import mx.kenzie.fern.parser.ParserBase;
import mx.kenzie.fern.parser.error.ParseError;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static mx.kenzie.fern.parser.Parser.*;

public class GenericFernParser implements FernParser, ParserBase<String> {
    
    protected final List<ValueHandler<?>> handlers = new ArrayList<>();
    
    {
        handlers.add(new ListHandler());
        handlers.add(new MapHandler());
        handlers.add(new NullHandler());
        handlers.add(new BooleanHandler());
        handlers.add(new StringHandler());
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
    
    public Fern parseElement(final String string) {
        for (ValueHandler<?> handler : handlers) {
            if (handler.matches(string)) {
                final Object value = handler.parse(string, this);
                if (value instanceof Fern fern) return fern;
                return new FernLeaf<>(value, (ValueHandler<Object>) handler);
            }
        }
        throw new IllegalStateException("Unable to parse unknown type: '" + string + "'");
    }
    
    //region Query Handling
    @Override
    public <T> Query<T> parseQuery(String string) {
        throw new IllegalStateException("Generic parser cannot parse query logic.");
    }
    
    @Override
    public boolean matches(Object object, String query) {
        throw new IllegalStateException("Generic parser cannot parse query logic.");
    }
    
    @Override
    public boolean matches(Object object, FernBranch query) {
        throw new IllegalStateException("Generic parser cannot parse query logic.");
    }
    
    //region Object Mapping
    @Override
    public <Type extends Serializable> Type objectMap(FernBranch branch, Class<Type> cls)
        throws IllegalStateException {
        return internalMap(branch, cls);
    }
    
    protected <Type> Type internalMap(FernBranch branch, Class<Type> cls)
        throws IllegalStateException {
        if (!FernUnsafe.isValid()) throw new IllegalStateException("Mapping is unavailable in this environment.");
        final Type target = FernUnsafe.createForSerialisation(cls);
        assert target != null;
        return internalMapWrite(branch, target);
    }
    @Override
    public <Type extends Serializable> Type objectMapWrite(FernBranch branch, Type target)
        throws IllegalStateException {
        return internalMapWrite(branch, target);
    }
    
    protected <Type> Type internalMapWrite(FernBranch branch, Type target)
        throws IllegalStateException {
        final List<Field> fields = FernUnsafe.getFields(target.getClass());
        for (final Field field : fields) {
            final Class<?> owner = field.getDeclaringClass();
            if (!branch.containsKey(owner.getSimpleName())) continue;
            final FernBranch sub = branch.get(owner.getSimpleName()).getAsBranch();
            if (!sub.containsKey(field.getName())) continue;
            final Fern fern = sub.get(field.getName());
            if (fern.isBranch()) {
                final Class<?> expected = field.getType();
                final Object value = this.internalMap(fern.getAsBranch(), expected);
                FernUnsafe.setValue(target, field, value);
            } else if (fern.isList() && field.getType().isArray()) {
                final Object array = fern.getAsList().arrayConversion(field.getType().getComponentType());
                FernUnsafe.setValue(target, field, array);
            } else {
                FernUnsafe.setValue(target, field, fern.getRawValue());
            }
        }
        return target;
    }
    
    @Override
    public <Type extends Serializable> FernBranch unMap(Type target) {
        return internalUnMap(target);
    }
    
    protected <Type> FernBranch internalUnMap(Type target) {
        final List<Field> fields = FernUnsafe.getFields(target.getClass());
        final FernBranch branch = new FernBranch();
        for (final Field field : fields) {
            final Class<?> owner = field.getDeclaringClass();
            branch.putIfAbsent(owner.getSimpleName(), new FernBranch());
            final FernBranch sub = branch.get(owner.getSimpleName()).getAsBranch();
            sub.put(field.getName(), serialise(FernUnsafe.getValue(target, field)));
        }
        return branch;
    }
    
    @Override
    public Fern serialise(Object object) {
        if (object.getClass().isArray()) {
            final FernList list = new FernList();
            final Object[] objects = (Object[]) object;
            for (Object o : objects) {
                final Fern fern = serialise(o);
                list.add(fern);
            }
            return list;
        }
        for (ValueHandler<?> handler : handlers) {
            if (!handler.isOfType(object)) continue;
            return new FernLeaf<>(object, (ValueHandler) handler);
        }
        if (!FernUnsafe.isValid()) return null;
        return internalUnMap(object);
    }
    //endregion
    
    @Override
    public FernBranch parse(String string) {
        final FernBranch tree = new FernBranch();
        parseMap(burnWhitespace(removeComments(string)), tree);
        return tree;
    }
    
    public void parseMap(final String content, final FernBranch branch) {
        if (content.isBlank()) return;
        final String[] entries = unwrapCommaList(content);
        for (final String entry : entries) {
            parseEntry(entry.trim(), branch);
        }
    }
    
    protected void parseEntry(final String entry, final FernBranch branch) {
        final BracketReader reader = new BracketReader(entry);
        final String key = reader.readUntil(new char[]{' ', '\t'});
        try {
            reader.rotate();
        } catch (RuntimeException ex) {
            throw new ParseError("Missing key/value delimiter in: '" + entry + "'");
        }
        final String remainder = reader.remainingString().trim();
        branch.put(key, parseElement(remainder));
    }
}

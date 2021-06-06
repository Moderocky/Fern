package mx.kenzie.fern;

import mx.kenzie.fern.handler.*;
import mx.kenzie.fern.parser.BracketReader;

import java.util.ArrayList;
import java.util.List;

import static mx.kenzie.fern.parser.Parser.*;

public class GenericFernParser implements FernParser {
    
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
    
    public void parseMap(final String content, final FernBranch branch) {
        final String[] entries = unwrapCommaList(content);
        for (final String entry : entries) {
            parseEntry(entry.trim(), branch);
        }
    }
    
    public Fern parseElement(final String string) {
        for (ValueHandler<?> handler : handlers) {
            if (handler.matches(string)) {
                final Object value = handler.parse(string, this);
                if (value instanceof Fern fern) return fern;
                return new mx.kenzie.fern.FernLeaf<>(value, string);
            }
        }
        throw new IllegalStateException("Unable to parse unknown type: '" + string + "'");
    }
    
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
    
    @Override
    public FernBranch parse(String string) {
        final FernBranch tree = new FernBranch();
        parseMap(burnWhitespace(removeComments(string)), tree);
        return tree;
    }
    
    protected void parseEntry(final String entry, final FernBranch branch) {
        final BracketReader reader = new BracketReader(entry);
        final String key = reader.readUntil(' ');
        reader.rotate();
        final String remainder = reader.remainingString().trim();
        branch.put(key, parseElement(remainder));
//        if (remainder.startsWith("(") && remainder.endsWith(")")) {
//            final FernBranch child = new FernBranch();
//            branch.put(key, child);
//            parseMap(reader.readBracketPairInside('(', ')'), child);
//        } else {
//            branch.put(key, parseElement(remainder));
//        }
    }
}

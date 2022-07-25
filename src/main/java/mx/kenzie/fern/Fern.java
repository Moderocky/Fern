package mx.kenzie.fern;

import mx.kenzie.fern.data.FernMap;
import mx.kenzie.fern.handler.BooleanHandler;
import mx.kenzie.fern.handler.NullHandler;
import mx.kenzie.fern.handler.NumberHandler;
import mx.kenzie.fern.handler.StringHandler;
import mx.kenzie.fern.meta.FernException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Fern implements Closeable {
    
    protected static final Map<Character, Supplier<ValueHandler<?>>> DEFAULT_HANDLERS;
    
    static {
        DEFAULT_HANDLERS = new HashMap<>();
        DEFAULT_HANDLERS.put('"', StringHandler::new);
        DEFAULT_HANDLERS.put('n', NullHandler::new);
        final Supplier<ValueHandler<?>> booleans = BooleanHandler::new;
        DEFAULT_HANDLERS.put('t', booleans);
        DEFAULT_HANDLERS.put('f', booleans);
        final Supplier<ValueHandler<?>> numbers = NumberHandler::new;
        DEFAULT_HANDLERS.put('1', numbers);
        DEFAULT_HANDLERS.put('2', numbers);
        DEFAULT_HANDLERS.put('3', numbers);
        DEFAULT_HANDLERS.put('4', numbers);
        DEFAULT_HANDLERS.put('5', numbers);
        DEFAULT_HANDLERS.put('6', numbers);
        DEFAULT_HANDLERS.put('7', numbers);
        DEFAULT_HANDLERS.put('8', numbers);
        DEFAULT_HANDLERS.put('9', numbers);
        DEFAULT_HANDLERS.put('0', numbers);
    }
    
    protected static final byte END = 1, EXPECT_KEY = 0, EXPECT_VALUE = 3;
    
    protected final Reader reader;
    protected final Writer writer;
    protected final Map<Character, Supplier<ValueHandler<?>>> handlers = new HashMap<>(DEFAULT_HANDLERS);
    
    public Fern(InputStream input, OutputStream output) {
        if (input != null) reader = new BufferedReader(new InputStreamReader(input));
        else reader = null;
        if (output != null) writer = new BufferedWriter(new OutputStreamWriter(output));
        else writer = null;
    }
    
    public FernMap readMap() {
        return this.read(new FernMap());
    }
    
    protected int state;
    protected transient StringBuilder key, value;
    
    public <IMap extends Map<String, Object>> IMap read(IMap map) {
        if (reader == null) throw new FernException("No input provided.");
        this.state = EXPECT_KEY;
        do {
            int x;
            while (((x = this.readChar()) != -1) && Character.isWhitespace(x)) ; // whitespace or end
            if (x == -1 || x == ')') {
                this.state = END;
                break;
            }
            switch (state) {
                case EXPECT_KEY -> {
                    this.key = new StringBuilder();
                    while (true) {
                        this.key.append((char) x);
                        x = this.readChar();
                        if (x == -1) throw new FernException("Reached end of data reading key '" + key + "...'");
                        else if (Character.isWhitespace(x)) break;
                    }
                    if (key.length() < 1) throw new FernException("Found a zero-length key.");
                    this.state = EXPECT_VALUE;
                }
                case EXPECT_VALUE -> {
                    this.value = new StringBuilder();
                    if (x == '(') map.put(key.toString(), this.readMap());
                    else if (x == '[') map.put(key.toString(), this.read(new ArrayList<>()));
                    else {
                        if (!handlers.containsKey((char) x))
                            throw new FernException("Found no handler for value beginning '" + (char) x + "'");
                        final ValueHandler<?> handler = handlers.get((char) x).get();
                        while (true) {
                            final boolean result = handler.accept(value, (char) x);
                            if (!result) break;
                            x = this.readChar();
                            if (x == -1) {
                                this.state = END;
                                break;
                            }
                        }
                        final Object value = handler.result(this.value);
                        map.put(key.toString(), value);
                    }
                    this.key = null;
                    this.value = null;
                    this.state = EXPECT_KEY;
                }
            }
        } while (state != END);
        return map;
    }
    
    public List<Object> readList() {
        return this.readList(false);
    }
    
    public List<Object> readList(boolean outerBrackets) {
        if (outerBrackets) while (this.readChar() != '[') ;
        return this.read(new ArrayList<>());
    }
    
    public <IList extends List<Object>> IList read(IList list) {
        if (reader == null) throw new FernException("No input provided.");
        this.state = EXPECT_VALUE;
        do {
            int x;
            while (((x = this.readChar()) != -1) && Character.isWhitespace(x)) ; // whitespace or end
            if (x == -1 || x == ']') {
                this.state = END;
                break;
            }
            this.value = new StringBuilder();
            if (x == '(') list.add(this.readMap());
            else if (x == '[') list.add(this.read(new ArrayList<>()));
            else {
                if (!handlers.containsKey((char) x))
                    throw new FernException("Found no handler for value beginning '" + (char) x + "'");
                final ValueHandler<?> handler = handlers.get((char) x).get();
                while (true) {
                    final boolean result = handler.accept(value, (char) x);
                    if (!result) break;
                    x = this.readChar();
                    if (x == -1) {
                        this.state = END;
                        break;
                    }
                }
                final Object value = handler.result(this.value);
                list.add(value);
            }
            this.value = null;
            this.state = EXPECT_VALUE;
        } while (state != END);
        return list;
    }
    
    public int readChar() {
        try {
            return reader.read();
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    @Override
    public void close() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
        } catch (Throwable ex) {
            throw new FernException(ex);
        }
    }
    
    public static Fern read(String string) {
        return new Fern(new ByteArrayInputStream(string.getBytes()), null);
    }
}

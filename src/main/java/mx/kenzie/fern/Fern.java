package mx.kenzie.fern;

import mx.kenzie.fern.data.FernMap;
import mx.kenzie.fern.handler.BooleanHandler;
import mx.kenzie.fern.handler.NullHandler;
import mx.kenzie.fern.handler.NumberHandler;
import mx.kenzie.fern.handler.StringHandler;
import mx.kenzie.fern.meta.Any;
import mx.kenzie.fern.meta.FernException;
import mx.kenzie.fern.meta.Name;
import mx.kenzie.fern.meta.Optional;
import sun.reflect.ReflectionFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

public class Fern implements Closeable {
    
    protected static final Map<Character, Supplier<ValueHandler<?>>> DEFAULT_HANDLERS;
    protected static final Set<ValueHandler<?>> DEFAULT_REVERSE;
    protected static final byte END = 0, EXPECT_KEY = 4, EXPECT_VALUE = 8;
    
    static {
        DEFAULT_HANDLERS = new HashMap<>();
        DEFAULT_HANDLERS.put('"', StringHandler::new);
        DEFAULT_HANDLERS.put('n', NullHandler::new);
        final Supplier<ValueHandler<?>> booleans = BooleanHandler::new;
        DEFAULT_HANDLERS.put('t', booleans);
        DEFAULT_HANDLERS.put('f', booleans);
        final Supplier<ValueHandler<?>> numbers = NumberHandler::new;
        DEFAULT_HANDLERS.put('-', numbers);
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
        DEFAULT_REVERSE = new HashSet<>();
        DEFAULT_REVERSE.add(new StringHandler());
        DEFAULT_REVERSE.add(new NullHandler());
        DEFAULT_REVERSE.add(new NumberHandler());
        DEFAULT_REVERSE.add(new BooleanHandler());
    }
    
    protected final Reader reader;
    protected final Writer writer;
    protected final Map<Character, Supplier<ValueHandler<?>>> handlers = new HashMap<>(DEFAULT_HANDLERS);
    protected final Map<String, Supplier<ValueHandler<?>>> specialHandlers = new HashMap<>();
    protected final Map<Class<?>, ValueHandler<?>> specialReverse = new LinkedHashMap<>();
    protected final Set<ValueHandler<?>> reverse = new HashSet<>(DEFAULT_REVERSE);
    protected int state;
    protected transient StringBuilder key, value, comment, identifier;
    protected String indent;
    protected int level;
    
    protected char separator = ' ';
    
    public Fern(InputStream input, OutputStream output) {
        if (input != null) reader = new InputStreamReader(input);
        else reader = null;
        if (output != null) writer = new OutputStreamWriter(output);
        else writer = null;
    }
    
    public Fern(File file) throws IOException {
        this(new FileReader(file), new FileWriter(file));
    }
    
    public Fern(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }
    
    public static Fern in(File file) throws FileNotFoundException {
        return new Fern(new FileReader(file), null);
    }
    
    public static Fern in(String string) {
        return new Fern(new ByteArrayInputStream(string.getBytes()), null);
    }
    
    public static Fern out(File file) throws IOException {
        return new Fern(null, new FileWriter(file));
    }
    
    public static String out(Object object, String indent) {
        final StringWriter writer = new StringWriter();
        try (final Fern fern = new Fern(null, writer)) {
            fern.write(object, object.getClass(), indent);
        }
        return writer.toString();
    }
    
    public static String out(Map<?, ?> map, String indent) {
        final StringWriter writer = new StringWriter();
        try (final Fern fern = new Fern(null, writer)) {
            fern.write(map, indent, 0);
        }
        return writer.toString();
    }
    
    public static void trans(Map<?, ?> map, OutputStream stream) {
        final Fern fern = new Fern(null, new OutputStreamWriter(stream));
        fern.write(map, null, 0);
    }
    
    public static void trans(Object object, OutputStream stream) {
        final Fern fern = new Fern(null, new OutputStreamWriter(stream));
        fern.write(object, null);
    }
    
    public static void trans(List<?> list, OutputStream stream) {
        final Writer writer = new OutputStreamWriter(stream);
        final Fern fern = new Fern(null, writer);
        try {
            writer.append('[').append(' ');
            fern.write(list, null);
            writer.append(' ').append(']');
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    public static void trans(InputStream stream, Map<String, Object> map) {
        final Fern fern = new Fern(new InputStreamReader(stream), null);
        fern.read(map);
    }
    
    public static void trans(InputStream stream, List<Object> list) {
        final Fern fern = new Fern(new InputStreamReader(stream), null);
        list.addAll(fern.readList(true));
    }
    
    public void registerHandler(Class<?> type, Supplier<ValueHandler<?>> handler) {
        this.specialHandlers.put(type.getSimpleName(), handler);
        this.specialReverse.put(type, handler.get());
    }
    
    public void registerHandler(char c, Supplier<ValueHandler<?>> handler) {
        this.handlers.put(c, handler);
        this.reverse.add(handler.get());
    }
    
    public Fern setSeparator(char c) {
        this.separator = c;
        return this;
    }
    
    public FernMap readMap() {
        return this.read(new FernMap());
    }
    
    public <IMap extends Map<String, Object>> IMap read(IMap map) {
        if (reader == null) throw new FernException("No input provided.");
        this.state = EXPECT_KEY;
        int x = 0;
        do {
            if (x == -1 || x == ')') {
                this.state = END;
                break;
            }
            while (((x = this.readChar()) != -1) && Character.isWhitespace(x)) ; // whitespace or end
            if (x == -1 || x == ')') {
                this.state = END;
                break;
            } else if (x == '`') this.state--; // comment shifts down
            switch (state) {
                case (EXPECT_KEY - 1), (EXPECT_VALUE - 1) -> {
                    this.comment = new StringBuilder();
                    boolean escape = false;
                    while (true) {
                        if (!escape) this.comment.append((char) x);
                        x = this.readChar();
                        if (x == -1)
                            throw new FernException("Reached end of data reading comment '" + comment + "...'");
                        else if (x == '\\' && !escape) escape = true;
                        else if (!escape && x == '`') break;
                        else escape = false;
                    }
                    if (comment.length() < 1) throw new FernException("Found a zero-length comment.");
                    this.comment = null;
                    this.state++; // back to where it should be
                }
                case EXPECT_KEY -> {
                    this.key = new StringBuilder();
                    boolean escape = false;
                    while (true) {
                        if (!escape) this.key.append((char) x);
                        x = this.readChar();
                        if (x == -1) throw new FernException("Reached end of data reading key '" + key + "...'");
                        else if (x == '\\' && !escape) escape = true;
                        else if (!escape && Character.isWhitespace(x)) break;
                        else escape = false;
                    }
                    if (key.length() < 1) throw new FernException("Found a zero-length key.");
                    else this.state = EXPECT_VALUE;
                }
                case EXPECT_VALUE -> {
                    final String key = this.key.toString(); // may get overridden
                    final Supplier<ValueHandler<?>> identified;
                    if (x == '<') {
                        this.identifier = new StringBuilder();
                        while ((x = this.readChar()) != '>') this.identifier.append((char) x);
                        do x = this.readChar();
                        while (Character.isWhitespace(x));
                        identified = specialHandlers.get(identifier.toString().trim());
                        this.identifier = null;
                    } else identified = null;
                    this.value = new StringBuilder();
                    final Object parsed;
                    if (identified != null) {
                        final ValueHandler<?> handler = identified.get();
                        while (handler.accept(value, (char) x) && (x = this.readChar()) != -1) ;
                        parsed = handler.result(this.value);
                    } else if (x == '(') parsed = this.read(new FernMap());
                    else if (x == '[') parsed = this.read(new ArrayList<>());
                    else if (handlers.containsKey((char) x)) {
                        final ValueHandler<?> handler = handlers.get((char) x).get();
                        while (handler.accept(value, (char) x) && (x = this.readChar()) != -1) ;
                        parsed = handler.result(this.value);
                    } else throw new FernException("Found no handler for value beginning '" + (char) x + "'");
                    map.put(key, parsed);
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
        int x = 0;
        do {
            if (x == -1 || x == ']') {
                this.state = END;
                break;
            }
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
                while (handler.accept(value, (char) x) && (x = this.readChar()) != -1) ;
                list.add(handler.result(this.value));
            }
            this.value = null;
            this.state = EXPECT_VALUE;
        } while (state != END);
        return list;
    }
    
    public void write(Map<?, ?> map, String indent, int level) {
        this.indent = indent;
        this.level = level;
        this.write(map);
    }
    
    public void write(Map<?, ?> map) {
        if (writer == null) throw new FernException("No output provided.");
        final boolean pretty = indent != null;
        boolean first = true;
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            this.writeEntry(entry, pretty, first);
            first = false;
        }
    }
    
    protected void writeEntry(Map.Entry<?, ?> entry, boolean pretty, boolean first) {
        if (pretty && !first) this.writeString(System.lineSeparator());
        else if (!first) this.writeChar(separator);
        if (pretty) for (int i = 0; i < level; i++) this.writeString(indent);
        this.writeKey(Objects.toString(entry.getKey()));
        this.writeChar(separator);
        final Object value = entry.getValue();
        this.writeValue(value);
    }
    
    public void writeKey(String key) {
        for (final char c : key.toCharArray()) {
            if (Character.isWhitespace(c)) this.writeChar('\\');
            else if (c == '\\') this.writeChar('\\');
            this.writeChar(c);
        }
    }
    
    public void write(List<?> list, String indent, int level) {
        this.indent = indent;
        this.level = level;
        this.write(list);
    }
    
    public void write(List<?> list) {
        if (writer == null) throw new FernException("No output provided.");
        final boolean pretty = indent != null;
        boolean first = true;
        for (final Object value : list) {
            if (pretty && !first) this.writeString(System.lineSeparator());
            if (pretty) for (int i = 0; i < level; i++) this.writeString(indent);
            else if (!first) this.writeChar(separator);
            first = false;
            this.writeValue(value);
        }
    }
    
    @SuppressWarnings({"unchecked", "all"})
    public void writeValue(Object value) {
        final boolean pretty = indent != null;
        for (final ValueHandler handler : reverse) {
            if (!handler.accept(value)) continue;
            this.writeString(handler.undo(value));
            return;
        }
        for (final Map.Entry<Class<?>, ValueHandler<?>> entry : specialReverse.entrySet()) {
            final ValueHandler handler = entry.getValue();
            if (!handler.accept(value)) continue;
            this.writeString('<' + entry.getKey().getSimpleName() + '>');
            this.writeChar(separator);
            this.writeString(handler.undo(value));
            return;
        }
        this.writeBranch(value, pretty);
    }
    
    protected void writeBranch(Object value, boolean pretty) {
        if (value instanceof Map<?, ?> child) {
            this.writeChar('(');
            if (child.isEmpty()) {
                this.writeChar(')');
                return;
            }
            if (!pretty) this.writeChar(separator);
            else this.writeString(System.lineSeparator());
            this.level++;
            this.write(child);
            this.level--;
            if (!pretty) this.writeChar(separator);
            else this.writeString(System.lineSeparator());
            if (pretty) for (int i = 0; i < level; i++) this.writeString(indent);
            this.writeChar(')');
        } else if (value instanceof List<?> child) {
            this.writeChar('[');
            if (child.isEmpty()) {
                this.writeChar(']');
                return;
            }
            if (!pretty) this.writeChar(separator);
            else this.writeString(System.lineSeparator());
            this.level++;
            this.write(child);
            this.level--;
            if (!pretty) this.writeChar(separator);
            else this.writeString(System.lineSeparator());
            if (pretty) for (int i = 0; i < level; i++) this.writeString(indent);
            this.writeChar(']');
        } else throw new FernException("No handler registered for '" + value.getClass().getSimpleName() + "'");
    }
    
    protected boolean canDeconstruct(Object object) {
        for (final ValueHandler<?> handler : specialReverse.values()) if (handler.accept(object)) return true;
        return false;
    }
    
    protected void write(Object object, Class<?> type, Map<String, Object> map) {
        final Set<Field> fields = new HashSet<>();
        fields.addAll(List.of(type.getDeclaredFields()));
        fields.addAll(List.of(type.getFields()));
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if ((modifiers & 0x00000002) != 0) continue;
            if ((modifiers & 0x00000008) != 0) continue;
            if ((modifiers & 0x00000080) != 0) continue;
            if ((modifiers & 0x00001000) != 0) continue;
            if (!field.canAccess(object)) field.trySetAccessible();
            try {
                final Object value = field.get(object);
                if (value == null && field.isAnnotationPresent(Optional.class)) continue;
                final Class<?> expected = field.getType();
                final String key;
                if (field.isAnnotationPresent(Name.class)) key = field.getAnnotation(Name.class).value();
                else key = field.getName();
                if (value == null) map.put(key, null);
                else if (value instanceof String) map.put(key, value);
                else if (value instanceof Number) map.put(key, value);
                else if (value instanceof Boolean) map.put(key, value);
                else if (value instanceof List<?>) map.put(key, value);
                else if (this.canDeconstruct(value)) map.put(key, value);
                else if (value instanceof Enum<?> num) map.put(key, num.name());
                else if (expected.isArray()) {
                    final List<Object> child = new ArrayList<>();
                    map.put(key, child);
                    this.deconstructArray(value, expected.getComponentType(), child, field.isAnnotationPresent(Any.class));
                } else {
                    final Map<String, Object> child = new FernMap();
                    map.put(key, child);
                    final Class<?> target;
                    if (field.isAnnotationPresent(Any.class)) target = value.getClass();
                    else target = expected;
                    this.write(value, target, child);
                }
            } catch (Throwable ex) {
                throw new FernException("Unable to write to object:", ex);
            }
        }
    }
    
    @SuppressWarnings("all")
    public void write(Object object, Class<?> type, String indent) {
        assert object != null : "Object was null.";
        assert object instanceof Class<?> ^ true : "Classes cannot be read from.";
        final Map<String, Object> map = new FernMap();
        this.write(object, type, map);
        this.write(map, indent, 0);
    }
    
    public void write(Object object, String indent) {
        if (object instanceof Map<?, ?> map) this.write(map, indent, 0);
        else if (object instanceof List<?> list) this.write(list, indent, 0);
        else this.write(object, object.getClass(), indent);
    }
    
    public void write(Object object) {
        this.write(object, object.getClass(), (String) null);
    }
    
    @SuppressWarnings("all")
    protected <Type> Type toObject(Type object, Class<?> type, Map<?, ?> map) {
        assert object != null : "Object was null.";
        assert object instanceof Class<?> ^ true : "Classes cannot be written to.";
        final Set<Field> fields = new HashSet<>();
        fields.addAll(List.of(type.getDeclaredFields()));
        fields.addAll(List.of(type.getFields()));
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if ((modifiers & 0x00000002) != 0) continue;
            if ((modifiers & 0x00000008) != 0) continue;
            if ((modifiers & 0x00000080) != 0) continue;
            if ((modifiers & 0x00001000) != 0) continue;
            if (!field.canAccess(object)) field.trySetAccessible();
            final String key;
            if (field.isAnnotationPresent(Name.class)) key = field.getAnnotation(Name.class).value();
            else key = field.getName();
            if (!map.containsKey(key)) continue;
            final Object value = map.get(key);
            final Class<?> expected = field.getType();
            final Object lock;
            if ((modifiers & 0x00000040) != 0) lock = object;
            else lock = new Object();
            try {
                synchronized (lock) {
                    if (expected.isPrimitive()) {
                        if (value instanceof Boolean boo) field.setBoolean(object, boo.booleanValue());
                        else if (value instanceof Number number) {
                            if (expected == byte.class) field.setByte(object, number.byteValue());
                            else if (expected == short.class) field.setShort(object, number.shortValue());
                            else if (expected == int.class) field.setInt(object, number.intValue());
                            else if (expected == long.class) field.setLong(object, number.longValue());
                            else if (expected == double.class) field.setDouble(object, number.doubleValue());
                            else if (expected == float.class) field.setFloat(object, number.floatValue());
                        }
                    } else if (value == null) field.set(object, null);
                    else if (expected.isEnum()) field.set(object, this.makeEnum(expected, value));
                    else if (expected.isAssignableFrom(value.getClass())) field.set(object, value);
                    else if (value instanceof Map<?, ?> child) {
                        final Object sub, existing = field.get(object);
                        if (existing == null) field.set(object, sub = this.createObject(expected));
                        else sub = existing;
                        this.toObject(sub, expected, child);
                    } else if (expected.isArray() && value instanceof List<?> list) {
                        final Object array = this.convertList(expected, list);
                        field.set(object, array);
                    } else throw new FernException("Value of '" + field.getName() + "' (" + object.getClass()
                        .getSimpleName() + ") could not be mapped to type " + expected.getSimpleName());
                }
            } catch (Throwable ex) {
                throw new FernException("Unable to write to object:", ex);
            }
        }
        return object;
    }
    
    @SuppressWarnings("all")
    private Object makeEnum(Class<?> type, Object value) {
        return Enum.valueOf((Class) type, value.toString());
    }
    
    @SuppressWarnings({"all"})
    public <Type> Type toObject(Type object, Class<?> type) {
        assert object != null : "Object was null.";
        assert object instanceof Class<?> ^ true : "Classes cannot be written to.";
        final Map<String, Object> map = this.read(new FernMap());
        return this.toObject(object, type, map);
    }
    
    public <Type> Type toObject(Type object) {
        assert object != null : "Object was null.";
        return this.toObject(object, object.getClass());
    }
    
    public <Type> Type toObject(Class<Type> type) {
        final Type object = this.createObject(type);
        return this.toObject(object, type);
    }
    
    public Object[] toArray() {
        return this.toArray(new Object[0]);
    }
    
    @SuppressWarnings("unchecked")
    public <Component> Component[] toArray(Class<Component> type) {
        return (Component[]) this.toArray(Array.newInstance(type, 0));
    }
    
    @SuppressWarnings({"all"})
    public <Container> Container toArray(Container array) {
        if (array == null) throw new FernException("Provided array was null.");
        final Class<?> type = array.getClass();
        if (!type.isArray()) throw new FernException("Provided object was not an array.");
        final Class<?> component = type.getComponentType();
        final List<?> list = this.readList();
        final Container container;
        if (Array.getLength(array) < 1) container = (Container) Array.newInstance(component, list.size());
        else container = array;
        final Object source = this.convertList(container.getClass(), list);
        final int a = Array.getLength(container), b = Array.getLength(source);
        System.arraycopy(source, 0, container, 0, Math.min(a, b));
        return container;
    }
    
    private Object convertSimple(Object data, Class<?> expected) {
        if (data instanceof List<?> list) return this.convertList(expected, list);
        else if (data instanceof Map<?, ?> map) return this.toObject(this.createObject(expected), expected, map);
        else return data;
    }
    
    private Object convertList(Class<?> type, List<?> list) {
        final Class<?> component = type.getComponentType();
        final Object object = Array.newInstance(component, list.size());
        final Object[] objects = list.toArray();
        if (component.isPrimitive()) {
            if (component == boolean.class) for (int i = 0; i < objects.length; i++) {
                final Object value = objects[i];
                Array.setBoolean(object, i, (boolean) value);
            }
            else if (component == int.class) for (int i = 0; i < objects.length; i++) {
                final Object value = objects[i];
                Array.setInt(object, i, ((Number) value).intValue());
            }
            else if (component == long.class) for (int i = 0; i < objects.length; i++) {
                final Object value = objects[i];
                Array.setLong(object, i, ((Number) value).longValue());
            }
            else if (component == double.class) for (int i = 0; i < objects.length; i++) {
                final Object value = objects[i];
                Array.setDouble(object, i, ((Number) value).doubleValue());
            }
            else if (component == float.class) for (int i = 0; i < objects.length; i++) {
                final Object value = objects[i];
                Array.setFloat(object, i, ((Number) value).floatValue());
            }
        } else {
            final Object[] array = (Object[]) object;
            for (int i = 0; i < objects.length; i++) {
                final Object value = objects[i];
                array[i] = this.convertSimple(value, component);
            }
        }
        return object;
    }
    
    @SuppressWarnings("unchecked")
    private <Type> Constructor<Type> createConstructor0(Class<Type> type) throws NoSuchMethodException {
        final Constructor<?> shift = Object.class.getConstructor();
        return (Constructor<Type>) ReflectionFactory.getReflectionFactory().newConstructorForSerialization(type, shift);
    }
    
    protected <Type> Type createObject(Class<Type> type) {
        try {
            if (type.isLocalClass() || type.getEnclosingClass() != null) {
                final Constructor<Type> constructor = this.createConstructor0(type);
                assert constructor != null;
                return constructor.newInstance();
            } else {
                final Constructor<Type> constructor = type.getDeclaredConstructor();
                final boolean result = constructor.trySetAccessible();
                assert result || constructor.canAccess(null);
                return constructor.newInstance();
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new FernException("Unable to create '" + type.getSimpleName() + "' object.", e);
        }
    }
    
    private Object deconstructSimple(Object value, Class<?> component) {
        if (value == null) return null;
        if (value instanceof String) return value;
        if (value instanceof Number) return value;
        if (value instanceof Boolean) return value;
        if (value.getClass().isArray()) {
            final List<Object> list = new ArrayList<>();
            this.deconstructArray(value, component.getComponentType(), list, false);
            return list;
        }
        final Map<String, Object> map = new FernMap();
        this.write(value, component, map);
        return map;
    }
    
    private void deconstructArray(Object array, Class<?> component, List<Object> list, boolean any) {
        if (component.isPrimitive()) {
            if (array instanceof int[] numbers) for (int number : numbers) list.add(number);
            else if (array instanceof long[] numbers) for (long number : numbers) list.add(number);
            else if (array instanceof double[] numbers) for (double number : numbers) list.add(number);
            else if (array instanceof float[] numbers) for (float number : numbers) list.add(number);
            else if (array instanceof boolean[] numbers) for (boolean number : numbers) list.add(number);
        } else {
            final Object[] objects = (Object[]) array;
            if (any) for (final Object object : objects) list.add(this.deconstructSimple(object, object.getClass()));
            else for (final Object object : objects) list.add(this.deconstructSimple(object, component));
        }
    }
    
    protected void writeChar(char c) {
        try {
            this.writer.write(c);
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    protected void writeString(String string) {
        try {
            this.writer.write(string);
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    protected int readChar() {
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
}

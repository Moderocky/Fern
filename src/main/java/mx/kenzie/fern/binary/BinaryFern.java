package mx.kenzie.fern.binary;

import mx.kenzie.fern.Fern;
import mx.kenzie.fern.data.FernMap;
import mx.kenzie.fern.meta.FernException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BinaryFern extends Fern {
    
    private final InputStream input;
    private final OutputStream output;
    
    protected BinaryFern() {
        this(null, null);
    }
    
    public BinaryFern(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }
    
    public BinaryFern(InputStream input) {
        this(input, null);
    }
    
    public BinaryFern(OutputStream output) {
        this(null, output);
    }
    
    protected void writeEntry(Map.Entry<?, ?> entry) {
        final Object value = entry.getValue();
        final String key = (String) entry.getKey();
        final DataType type = DataType.of(value);
        this.write((byte) type.ordinal());
        this.writeString(key);
        this.write(value, type);
    }
    
    protected void write(Object value, DataType type) {
        try {
            switch (type) {
                case BYTE -> this.write((byte) value);
                case SHORT -> DataType.encodeShort(output, (short) value);
                case INT -> DataType.encodeInt(output, (int) value);
                case LONG -> DataType.encodeLong(output, (long) value);
                case FLOAT -> DataType.encodeInt(output, Float.floatToIntBits((float) value));
                case DOUBLE -> DataType.encodeLong(output, Double.doubleToLongBits((double) value));
                case BOOLEAN -> this.write((boolean) value ? 1 : 0);
                case STRING -> this.writeString((String) value);
                case ARRAY -> this.writeArray(value);
                case MAP -> this.writeMap(value);
            }
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    @Override
    public FernMap readMap() {
        try {
            return this.readMap0();
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    @Override
    public <IMap extends Map<String, Object>> IMap read(IMap map) {
        try {
            final FernMap found = this.readMap0();
            map.putAll(found);
            return map;
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    @Override
    public List<Object> readList() {
        try {
            return this.readArray();
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    @Override
    public <IList extends List<Object>> IList read(IList list) {
        try {
            final List<Object> found = this.readArray();
            list.addAll(found);
            return list;
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    @Override
    public void write(Map<?, ?> map) {
        this.writeMap(map);
    }
    
    @Override
    public void write(List<?> list) {
        try {
            this.writeArray(list);
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    protected void writeString(String string) {
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        final int length = bytes.length;
        if (length > Short.MAX_VALUE) {
            this.write((byte) -2);
            try {
                DataType.encodeInt(output, (short) length);
            } catch (IOException ex) {
                throw new FernException(ex);
            }
        } else if (length > Byte.MAX_VALUE) {
            this.write((byte) -1);
            try {
                DataType.encodeShort(output, (short) length);
            } catch (IOException ex) {
                throw new FernException(ex);
            }
        } else {
            this.write((byte) length);
        }
        this.write(bytes);
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
    
    @SuppressWarnings("unchecked")
    protected void writeMap(Object object) {
        final Map<String, Object> map = (Map<String, Object>) object;
        for (final Map.Entry<String, Object> entry : map.entrySet()) this.writeEntry(entry);
        this.write((byte) DataType.END.ordinal());
    }
    
    protected FernMap readMap0() throws IOException {
        final FernMap map = new FernMap();
        final byte end = (byte) DataType.END.ordinal();
        final DataType[] types = DataType.values();
        for (byte data = this.read(); data != end; data = this.read()) {
            final DataType type = types[data];
            final String key = this.readString();
            final Object value = this.readObject(type);
            map.put(key, value);
        }
        return map;
    }
    
    protected void writeArray(Object object) throws IOException {
        final Object[] array;
        if (object instanceof List<?> list) array = list.toArray();
        else array = (Object[]) object;
        if (array.length == 0) { // empty = simple length
            this.write(0);
            this.write(0);
            return;
        }
        final boolean simple = this.isSimple(array);
        this.write(simple ? 0 : 1);
        DataType.encodeInt(output, array.length);
        if (simple) { // simple length type (datum...)
            final DataType type = DataType.of(array[0]);
            this.write(type.ordinal());
            for (final Object value : array) this.write(value, type);
            return;
        }
        for (final Object value : array) { // simple length (type datum...)
            final DataType type = DataType.of(array[0]);
            this.write(type.ordinal());
            this.write(value, type);
        }
    }
    
    protected List<Object> readArray() throws IOException {
        final boolean simple = this.read() == 0;
        final int length = DataType.decodeInt(input);
        if (length == 0) return new LinkedList<>();
        final List<Object> array = new LinkedList<>();
        final DataType[] types = DataType.values();
        if (simple) {
            final DataType type = types[this.read()];
            for (int i = 0; i < length; i++) array.add(this.readObject(type));
        } else for (int i = 0; i < length; i++) {
            final DataType type = types[this.read()];
            array.add(this.readObject(type));
        }
        return array;
    }
    
    protected Object readObject(DataType type) throws IOException {
        return switch (type) {
            case NULL, END -> null;
            case BYTE -> this.read();
            case SHORT -> DataType.decodeShort(input);
            case INT -> DataType.decodeInt(input);
            case LONG -> DataType.decodeLong(input);
            case FLOAT -> Float.intBitsToFloat(DataType.decodeInt(input));
            case DOUBLE -> Double.longBitsToDouble(DataType.decodeLong(input));
            case BOOLEAN -> this.read() == 1;
            case STRING -> this.readString();
            case ARRAY -> this.readArray();
            case MAP -> this.readMap0();
        };
    }
    
    protected boolean isSimple(Object[] array) {
        var type = (Class<?>) null;
        for (final Object object : array) {
            if (object == null) return false;
            if (type == null) type = object.getClass();
            else if (type != object.getClass()) return false;
        }
        return true;
    }
    
    protected byte read() throws IOException {
        return (byte) input.read();
    }
    
    protected String readString() {
        try {
            final byte sign = this.read();
            final int length = switch (sign) {
                case -2 -> DataType.decodeInt(input);
                case -1 -> DataType.decodeShort(input);
                default -> sign;
            };
            if (length == 0) return "";
            final byte[] bytes = new byte[length];
            final int amount = this.input.read(bytes);
            assert amount == length;
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    protected void write(int b) throws IOException {
        this.output.write((byte) b);
    }
    
    protected void write(byte b) {
        try {
            this.output.write(b);
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
    protected void write(byte[] bytes) {
        try {
            this.output.write(bytes);
        } catch (IOException ex) {
            throw new FernException(ex);
        }
    }
    
}

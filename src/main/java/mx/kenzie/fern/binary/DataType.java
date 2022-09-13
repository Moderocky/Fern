package mx.kenzie.fern.binary;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public enum DataType {
    NULL,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    STRING,
    ARRAY,
    MAP,
    END;
    
    static DataType of(byte type) {
        return values()[type];
    }
    
    static DataType of(Object object) {
        if (object == null) return NULL;
        if (object instanceof Byte) return BYTE;
        if (object instanceof Short) return SHORT;
        if (object instanceof Integer) return INT;
        if (object instanceof Long) return LONG;
        if (object instanceof Float) return FLOAT;
        if (object instanceof Double) return DOUBLE;
        if (object instanceof Boolean) return BOOLEAN;
        if (object instanceof String) return STRING;
        if (object instanceof Map<?, ?>) return MAP;
        if (object.getClass().isArray() || object instanceof Collection<?>) return ARRAY;
        return NULL;
    }
    
    static short decodeShort(InputStream stream) throws IOException {
        final int a = stream.read(), b = stream.read();
        if (b < 0) throw new EOFException();
        return (short) ((a << 8) + b);
    }
    
    static int decodeInt(InputStream stream) throws IOException {
        final int a = stream.read(), b = stream.read(), c = stream.read(), d = stream.read();
        if (d < 0) throw new EOFException();
        return ((a << 24) + (b << 16) + (c << 8) + d);
    }
    
    static long decodeLong(InputStream stream) throws IOException {
        final int a = stream.read(), b = stream.read(), c = stream.read(), d = stream.read();
        final int f = stream.read(), g = stream.read(), h = stream.read(), i = stream.read();
        if (i < 0) throw new EOFException();
        return ((long) a << 56) + ((long) (b & 255) << 48) + ((long) (c & 255) << 40) + ((long) (d & 255) << 32) +
            ((long) (f & 255) << 24) + ((long) (g & 255) << 16) + ((h & 255) << 8) + (i & 255);
    }
    
    static void encodeShort(OutputStream stream, short value) throws IOException {
        stream.write((value >>> 8));
        stream.write(value);
    }
    
    static void encodeInt(OutputStream stream, int value) throws IOException {
        stream.write((byte) (value >>> 24));
        stream.write((byte) (value >>> 16));
        stream.write((byte) (value >>> 8));
        stream.write((byte) value);
    }
    
    static void encodeLong(OutputStream stream, long value) throws IOException {
        stream.write((byte) (value >>> 56));
        stream.write((byte) (value >>> 48));
        stream.write((byte) (value >>> 40));
        stream.write((byte) (value >>> 32));
        stream.write((byte) (value >>> 24));
        stream.write((byte) (value >>> 16));
        stream.write((byte) (value >>> 8));
        stream.write((byte) value);
    }
    
}

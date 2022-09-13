package mx.kenzie.fern.test;

import mx.kenzie.fern.binary.BinaryFern;
import mx.kenzie.fern.data.FernMap;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class BinaryTest {
    
    @Test
    public void basic() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final BinaryFern writer = new BinaryFern(output);
        final Map<String, Object> initial = new FernMap();
        initial.put("string", "hello there");
        initial.put("long string", "hello there hello there hello there hello there hello there hello there");
        initial.put("null", null);
        initial.put("byte", (byte) 1);
        initial.put("short", (short) 32);
        initial.put("int", -40);
        initial.put("long", 1032140L);
        initial.put("float", 13.5F);
        initial.put("double", -10.8D);
        initial.put("boolean", true);
        writer.write(initial);
        final byte[] bytes = output.toByteArray();
        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        final BinaryFern reader = new BinaryFern(input);
        final Map<String, Object> result = reader.readMap();
        assert result != null;
        assert result.size() == initial.size();
        assert result.get("string").equals("hello there");
        assert result.equals(initial);
    }
    
    @Test
    public void object() {
        class Thing {
            int number = 3;
        }
        class Test {
            final Thing thing = new Thing();
            String name;
        }
        final Test test = new Test();
        test.name = "hello";
        test.thing.number = 5;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final BinaryFern writer = new BinaryFern(output);
        writer.write(test);
        final byte[] bytes = output.toByteArray();
        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        final BinaryFern reader = new BinaryFern(input);
        final Test result = reader.toObject(new Test());
        assert result != test;
        assert test.name.equals(result.name);
        assert test.thing.number == result.thing.number;
    }
    
}

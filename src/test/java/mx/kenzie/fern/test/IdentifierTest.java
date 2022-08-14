package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import mx.kenzie.fern.ValueHandler;
import mx.kenzie.fern.data.FernMap;
import org.junit.Test;

import java.io.StringWriter;

public class IdentifierTest {
    
    @Test
    public void test() {
        final String source = "thing <Blob> 100 test <Blob> 50";
        final FernMap map;
        final StringWriter writer = new StringWriter();
        try (final Fern fern = Fern.in(source)) {
            fern.registerHandler(Blob.class, BlobHandler::new);
            map = fern.readMap();
            assert map.get("thing") instanceof Blob blob && blob.b == 100;
            assert map.get("test") instanceof Blob blob && blob.b == 50;
        }
        try (final Fern fern = new Fern(null, writer)) {
            fern.registerHandler(Blob.class, BlobHandler::new);
            fern.write(map);
            assert writer.toString().equals(source) : writer;
        }
    }
    
    @Test
    public void object() {
        final StringWriter writer = new StringWriter();
        class Thing { final Blob blob = new Blob(); }
        try (final Fern fern = new Fern(null, writer)) {
            fern.registerHandler(Blob.class, BlobHandler::new);
            fern.write(new Thing());
        }
        assert writer.toString().equals("blob <Blob> 0"): writer;
    }
    
}

class Blob {
    byte b;
}

class BlobHandler implements ValueHandler<Blob> {
    
    @Override
    public boolean accept(StringBuilder builder, char c) {
        if (!Character.isDigit(c)) return false;
        builder.append(c);
        return true;
    }
    
    @Override
    public Blob result(StringBuilder builder) {
        final Blob blob = new Blob();
        blob.b = Byte.parseByte(builder.toString());
        return blob;
    }
    
    @Override
    public String undo(Blob blob) {
        return blob.b + "";
    }
    
    @Override
    public boolean accept(Object type) {
        return type instanceof Blob;
    }
}

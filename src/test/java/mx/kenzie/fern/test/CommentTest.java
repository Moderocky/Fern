package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import mx.kenzie.fern.data.FernMap;
import org.junit.Test;

public class CommentTest {
    
    @Test
    public void decimals() {
        try (final Fern fern = Fern.in("""
            thing 1D number 2.4 `hello there` byte `general kenobi` 1B short 32S
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("thing") instanceof Double;
            assert map.get("number") instanceof Double;
            assert map.toString().equals("{thing=1.0, number=2.4, byte=1, short=32}") : map;
        }
    }
    
}

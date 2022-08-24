package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import org.junit.Test;

public class EnumTest {
    
    @Test
    public void write() {
        enum Fruit {APPLE, PEAR}
        class Thing {
            final int number = 1;
            final Fruit fruit = Fruit.APPLE;
        }
        final String result = Fern.out(new Thing(), null);
        assert result.equals("number 1 fruit \"APPLE\"") : result;
    }
    
    @Test
    public void read() {
        enum Fruit {APPLE, PEAR}
        class Thing {
            int number;
            Fruit fruit;
        }
        final Thing thing = new Thing();
        try (final Fern fern = Fern.in("number 1 fruit \"APPLE\"")) {
            fern.toObject(thing);
        }
        assert thing.number == 1;
        assert thing.fruit == Fruit.APPLE;
    }
}

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
            final Fruit[] fruits = {Fruit.APPLE, Fruit.PEAR};
        }
        final String result = Fern.out(new Thing(), null);
        assert result.equals("number 1 fruits [ \"APPLE\" \"PEAR\" ] fruit \"APPLE\"") : result;
    }
    
    @Test
    public void read() {
        enum Fruit {APPLE, PEAR}
        class Thing {
            int number;
            Fruit fruit;
            Fruit[] fruits;
        }
        final Thing thing = new Thing();
        try (final Fern fern = Fern.in("number 1 fruits [ \"APPLE\" \"PEAR\" ] fruit \"APPLE\"")) {
            fern.toObject(thing);
        }
        assert thing.number == 1;
        assert thing.fruit == Fruit.APPLE;
        assert thing.fruits != null;
        assert thing.fruits.length == 2;
        assert thing.fruits[0] == Fruit.APPLE;
        assert thing.fruits[1] == Fruit.PEAR;
    }
}

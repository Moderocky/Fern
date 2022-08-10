package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import mx.kenzie.fern.data.FernMap;
import org.junit.Test;

import java.util.Map;

public class WritingTest {
    
    @Test
    public void simple() {
        class Test {
            final String hello = "there", general = "kenobi";
        }
        final String value = Fern.out(new Test(), null);
        assert value.equals("hello \"there\" general \"kenobi\"") : value;
    }
    
    @Test
    public void complex() {
        class Test {
            final double number = 10.455;
            final float test = 2;
            final Object blob = null;
        }
        final String value = Fern.out(new Test(), null);
        assert value.equals("blob null number 10.455D test 2.0F") : value;
    }
    
    @Test
    public void pretty() {
        class Thing {
            final String hello = "there", general = "kenobi";
        }
        class Test {
            final double number = 10.455;
            final float test = 2;
            final Thing first = null, second = new Thing();
        }
        final String value = Fern.out(new Test(), "  ");
        assert value.equals("""
            number 10.455D
            test 2.0F
            second (
              general "kenobi"
              hello "there"
            )
            first null""") : value;
        final String small = Fern.out(new Test(), null);
        assert small.equals("number 10.455D test 2.0F second ( general \"kenobi\" hello \"there\" ) first null") : small;
        final String list = Fern.out(new Object() {
            final int[] numbers = {1, 2, 6, 4};
        }, null);
        assert list.equals("numbers [ 1 2 6 4 ]") : list;
    }
    
    @Test
    public void escapeKey() {
        final Map<String, Object> map = new FernMap();
        map.put("hello there", "general kenobi");
        final String result = Fern.out(map, null);
        assert result.equals("hello\\ there \"general kenobi\"") : result;
        final String value = Fern.in(result).readMap().toString();
        assert value.equals("{hello there=general kenobi}") : value;
    }
    
}

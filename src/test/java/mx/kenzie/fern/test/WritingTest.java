package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import org.junit.Test;

public class WritingTest {
    
    @Test
    public void simple() {
        class Test { final String hello = "there", general = "kenobi"; }
        final String value = Fern.out(new Test(), null);
        assert value.equals("general \"kenobi\" hello \"there\" "): value;
    }
    
    @Test
    public void complex() {
        class Test { final double number = 10.455; final float test = 2; final Object blob = null; }
        final String value = Fern.out(new Test(), null);
        assert value.equals("number 10.455D blob null test 2.0F "): value;
    }
    
}

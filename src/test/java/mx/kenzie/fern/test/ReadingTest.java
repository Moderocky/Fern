package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import mx.kenzie.fern.data.FernMap;
import org.junit.Test;

public class ReadingTest {
    
    @Test
    public void basic() {
        try (final Fern fern = Fern.in("""
            hello "there" general "kenobi"
            """)) {
            final FernMap map = fern.readMap();
            assert map.toString().equals("{hello=there, general=kenobi}") : map;
        }
    }
    
    @Test
    public void integers() {
        try (final Fern fern = Fern.in("""
            thing 1 number 2
            """)) {
            final FernMap map = fern.readMap();
            assert map.toString().equals("{thing=1, number=2}") : map;
        }
    }
    
    @Test
    public void floats() {
        try (final Fern fern = Fern.in("""
            thing 1F number -2.4F
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("thing") instanceof Float;
            assert map.toString().equals("{thing=1.0, number=-2.4}") : map;
        }
    }
    
    @Test
    public void decimals() {
        try (final Fern fern = Fern.in("""
            thing 1D number 2.4 byte 1B short 32S
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("thing") instanceof Double d && d == 1;
            assert map.get("number") instanceof Double d && d == 2.4;
            assert map.get("byte") instanceof Byte b && b == 1;
            assert map.get("short") instanceof Short s && s == 32;
            assert map.toString().equals("{thing=1.0, number=2.4, byte=1, short=32}") : map;
        }
    }
    
    @Test
    public void booleans() {
        try (final Fern fern = Fern.in("""
            true true false false map (value true)
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("true") instanceof Boolean;
            assert map.get("false") instanceof Boolean;
            assert map.toString().equals("{true=true, false=false, map={value=true}}") : map;
        }
    }
    
    @Test
    public void nulls() {
        try (final Fern fern = Fern.in("""
            null null
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("null") == null;
            assert map.toString().equals("{null=null}") : map;
        }
    }
    
    @Test
    public void map() {
        try (final Fern fern = Fern.in("""
            map (hello "there" general "kenobi" number 1F)
            """)) {
            final FernMap map = fern.readMap();
            assert map.getMap("map").get("number") instanceof Float;
            assert map.toString().equals("{map={hello=there, general=kenobi, number=1.0}}") : map;
        }
    }
    
    @Test
    public void list() {
        try (final Fern fern = Fern.in("""
            list [1 2 "hello" "there" 5] number 1
            """)) {
            final FernMap map = fern.readMap();
            assert map.toString().equals("{list=[1, 2, hello, there, 5], number=1}") : map;
        }
    }
    
    @Test
    public void listMaps() {
        try (final Fern fern = Fern.in("""
            empty [] empty () list [(test 1) (test 2)]
            """)) {
            final FernMap map = fern.readMap();
            assert map.toString().equals("{empty={}, list=[{test=1}, {test=2}]}") : map;
        }
    }
    
    @Test
    public void object() {
        class Thing {
            int thing;
            short age;
            String name;
            byte height;
        }
        final Fern fern = Fern.in("thing 10 age 23S height 10B name \"hello\"");
        final Thing thing = fern.toObject(new Thing());
        assert thing.thing == 10;
        assert thing.age == 23;
        assert thing.height == 10;
        assert thing.name.equals("hello");
        fern.close();
    }
    
}

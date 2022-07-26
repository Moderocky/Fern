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
            assert map.toString().equals("{general=kenobi, hello=there}") : map;
        }
    }
    
    @Test
    public void integers() {
        try (final Fern fern = Fern.in("""
            thing 1 number 2
            """)) {
            final FernMap map = fern.readMap();
            assert map.toString().equals("{number=2, thing=1}") : map;
        }
    }
    
    @Test
    public void floats() {
        try (final Fern fern = Fern.in("""
            thing 1F number 2.4F
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("thing") instanceof Float;
            assert map.toString().equals("{number=2.4, thing=1.0}") : map;
        }
    }
    
    @Test
    public void decimals() {
        try (final Fern fern = Fern.in("""
            thing 1D number 2.4 byte 1B short 32S
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("thing") instanceof Double;
            assert map.get("number") instanceof Double;
            assert map.toString().equals("{number=2.4, byte=1, short=32, thing=1.0}") : map;
        }
    }
    
    @Test
    public void booleans() {
        try (final Fern fern = Fern.in("""
            true true false false
            """)) {
            final FernMap map = fern.readMap();
            assert map.get("true") instanceof Boolean;
            assert map.get("false") instanceof Boolean;
            assert map.toString().equals("{true=true, false=false}") : map;
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
            map ( hello "there" general "kenobi" ) number 1
            """)) {
            final FernMap map = fern.readMap();
            assert map.toString().equals("{number=1, map={general=kenobi, hello=there}}") : map;
        }
    }
    
    @Test
    public void list() {
        try (final Fern fern = Fern.in("""
            list [ 1 2 "hello" "there" ] number 1
            """)) {
            final FernMap map = fern.readMap();
            assert map.toString().equals("{number=1, list=[1, 2, hello, there]}") : map;
        }
    }
    
}

package mx.kenzie.fern.test;

import mx.kenzie.fern.FernBranch;
import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.GenericFernParser;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;

public class FernMappingTest {
    
    
    final FernParser parser = new GenericFernParser();
    
    @Test
    public void unmapping() {
        final Alice alice = new Alice();
        alice.alive = true;
        alice.thingy = 66.4;
        final FernBranch branch = parser.unMap(alice);
        assert branch.toRootString(true, parser)
            .equals("Alice (age 23, alive true, name \"Alice\"), Human (thingy 66.4)");
    }
    
    @Test
    public void mapping() {
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("mapping_test.fern"));
        final Alice alice = parser.objectMap(tree, Alice.class);
        assert alice.name.equals("Bill");
        assert alice.alive;
        assert alice.thingy == 33.2;
        assert alice.age == 29;
    }
    
    @Test
    public void mappingOverwrite() {
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("mapping_test.fern"));
        final Alice alice = new Alice();
        assert alice.name.equals("Alice");
        assert alice.age == 23;
        assert alice.thingy == 10;
        final Alice bill = parser.objectMapWrite(tree, alice);
        assert alice == bill;
        assert bill.name.equals("Bill");
        assert bill.alive;
        assert bill.thingy == 33.2;
        assert bill.age == 29;
    }
    
    @Test
    public void complex() {
        final Bob bob = new Bob();
        bob.alive = true;
        bob.thingy = 66.5;
        final FernBranch branch = parser.unMap(bob);
        final Bob rob = parser.objectMap(branch, Bob.class);
        assert rob.bean.things[1].equals("there");
        assert rob.thingy == 66.5;
    }
    
    private static class Bean implements Serializable {
        
        double blob = 10;
        String[] things = {"hello", "there"};
        
    }
    
    private static class Bob extends Alice implements Serializable {
        
        Bean bean = new Bean();
        
    }
    
    private static class Alice extends Human implements Serializable {
        
        final int age;
        String name = "Alice";
        boolean alive;
        
        public Alice() {
            this.age = 23;
        }
        
    }
    
    private static class Human {
        
        double thingy = 10;
        
    }
    
    
}

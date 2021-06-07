package mx.kenzie.fern.test;

import mx.kenzie.fern.FernBranch;
import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.GenericFernParser;
import org.junit.Test;

public class SerialisationTest {
    
    final FernParser parser = new GenericFernParser();
    
    @Test
    public void simple() {
        final String sample = "map (name \"alice\", age 21)";
        final FernBranch branch = parser.parse(sample);
        assert branch.toString().equals("(map (age 21, name \"alice\"))");
        assert branch.toString(0, parser).equals("""
            (
                map (
                    age 21,
                    name "alice"
                )
            )""");
        assert branch.toRootString(true).equals("map (age 21, name \"alice\")");
        assert branch.toRootString().equals("""
            map (
                age 21,
                name "alice"
            )""");
    }
    
    @Test
    public void list() {
        final String sample = "list [1, 2, 3, \"blob\", true]";
        final FernBranch branch = parser.parse(sample);
        assert branch.toString().equals("(list [1, 2, 3, \"blob\", true])");
        assert branch.toString(0, parser).equals("""
            (
                list [
                    1,
                    2,
                    3,
                    "blob",
                    true
                ]
            )""");
        assert branch.toRootString(true).equals(sample);
        assert branch.toRootString().equals("""
            list [
                1,
                2,
                3,
                "blob",
                true
            ]""");
    }
    
    @Test
    public void advanced() {
        final String sample = """
            a_map (
                name "henry",
                age 44,
                surname "james",
                alive true,
                weight 64.3F,
                hat null
            ),
            b_map (
                name "paul",
                age 35,
                surname "bean",
                alive false,
                weight 53.0F,
                hat null
            ),
            boolean true
            """;
        final FernBranch branch = parser.parse(sample);
        assert branch.toString()
            .equals("(a_map (age 44, alive true, hat null, name \"henry\", surname \"james\", weight 64.3F), b_map (age 35, alive false, hat null, name \"paul\", surname \"bean\", weight 53.0F), boolean true)");
        assert branch.toString(0, parser).equals("""
            (
                a_map (
                    age 44,
                    alive true,
                    hat null,
                    name "henry",
                    surname "james",
                    weight 64.3F
                ),
                b_map (
                    age 35,
                    alive false,
                    hat null,
                    name "paul",
                    surname "bean",
                    weight 53.0F
                ),
                boolean true
            )""");
        assert branch.toRootString(true)
            .equals("a_map (age 44, alive true, hat null, name \"henry\", surname \"james\", weight 64.3F), b_map (age 35, alive false, hat null, name \"paul\", surname \"bean\", weight 53.0F), boolean true");
        assert branch.toRootString().equals("""
            a_map (
                age 44,
                alive true,
                hat null,
                name "henry",
                surname "james",
                weight 64.3F
            ),
            b_map (
                age 35,
                alive false,
                hat null,
                name "paul",
                surname "bean",
                weight 53.0F
            ),
            boolean true""");
    }
}

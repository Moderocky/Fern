package test;

import mx.kenzie.fern.Process;
import mx.kenzie.fern.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

public class FernQueryTest {
    
    @Test
    public void basic() {
        final FernParser parser = new QueriableFernParser();
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("basic_queries.fern"));
        assert tree != null;
        assert tree.size() == 1;
        final FernBranch branch = tree.get("root_key").getAsBranch();
        assert branch != null;
        assert branch.size() == 7;
        assert branch.get("damage") instanceof FernLeaf;
        assert branch.get("damage").getAsLeaf(Query.class).isLeaf();
        assert branch.get("damage").<Query<Number>>getAsLeaf().value().compare(16);
        assert branch.get("damage").<Query<Number>>getAsLeaf().value().compare(11);
        assert !branch.get("damage").<Query<Number>>getAsLeaf().value().compare(10);
        assert branch.get("bean").<Query<Number>>getAsLeaf().value().compare(20);
        assert branch.get("bean").<Query<Number>>getAsLeaf().value().compare(25);
        assert !branch.get("bean").<Query<Number>>getAsLeaf().value().compare(19);
        assert branch.get("name").<Query<String>>getAsLeaf().value().compare("hello");
        assert !branch.get("name").<Query<String>>getAsLeaf().value().compare("goodbye");
        assert branch.get("sandwich").<Query<String>>getAsLeaf().value().compare("sandwich");
        assert !branch.get("sandwich").<Query<String>>getAsLeaf().value().compare("blob");
        assert branch.get("flapjack").<Query<String>>getAsLeaf().value().compare("flapjack");
    }
    
    @Test
    public void advanced() {
        final FernParser parser = new QueriableFernParser();
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("advanced_queries.fern"));
        assert tree != null;
        assert tree.get("is_in").<Query<Object>>getAsLeaf().value().compare("hello");
        assert !tree.get("is_in").<Query<Object>>getAsLeaf().value().compare("general");
        assert !tree.get("not_in").<Query<Object>>getAsLeaf().value().compare("hello");
        assert tree.get("not_in").<Query<Object>>getAsLeaf().value().compare("general");
        assert tree.get("subset").<Query<Object>>getAsLeaf().value().compare("hello");
        assert tree.get("subset").<Query<Object>>getAsLeaf().value().compare(new String[]{"hello", "there"});
        assert tree.get("subset").<Query<Object>>getAsLeaf().value().compare(new String[]{"hello", "kenobi"});
        assert !tree.get("subset").<Query<Object>>getAsLeaf().value().compare(new String[]{"bold", "one"});
        assert tree.get("superset").<Query<Object>>getAsLeaf().value().compare(new String[]{"hello", "there"});
        assert tree.get("superset").<Query<Object>>getAsLeaf().value().compare(new String[]{"there", "hello"});
        assert tree.get("superset").<Query<Object>>getAsLeaf().value()
            .compare(new String[]{"hello", "there", "general", "kenobi"});
        assert !tree.get("superset").<Query<Object>>getAsLeaf().value().compare(new String[]{"hello"});
        assert !tree.get("superset").<Query<Object>>getAsLeaf().value().compare("hello");
        assert !tree.get("superset").<Query<Object>>getAsLeaf().value().compare(new String[]{"general", "kenobi"});
    }
    
    @Test
    public void junction() {
        final FernParser parser = new QueriableFernParser();
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("advanced_queries.fern"));
        assert tree != null;
        assert tree.get("and").<Query<Object>>getAsLeaf().value().compare(true);
        assert !tree.get("and").<Query<Object>>getAsLeaf().value().compare(false);
        assert tree.get("or").<Query<Object>>getAsLeaf().value().compare(false);
        assert tree.get("or").<Query<Object>>getAsLeaf().value().compare(true);
        assert tree.get("xor").<Query<Object>>getAsLeaf().value().compare(false);
        assert !tree.get("xor").<Query<Object>>getAsLeaf().value().compare(true);
        assert tree.get("implies").<Query<Object>>getAsLeaf().value().compare(true);
        assert tree.get("implies").<Query<Object>>getAsLeaf().value().compare(false);
        assert !tree.get("not_implies").<Query<Object>>getAsLeaf().value().compare(true);
        assert tree.get("not_implies").<Query<Object>>getAsLeaf().value().compare(false);
    }
    
    @Test
    public void process() {
        final FernParser parser = new QueriableFernParser();
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("advanced_queries.fern"));
        assert tree != null;
        assert tree.get("union").<Process<Object, Collection<?>>>getAsLeaf().value().process("general")
            .size() == 3;
        assert tree.get("union").<Process<Object, Collection<?>>>getAsLeaf().value().process("general")
            .contains("hello");
        assert tree.get("union").<Process<Object, Collection<?>>>getAsLeaf().value().process("general")
            .containsAll(Arrays.asList("hello", "there", "general"));
        assert tree.get("union").<Process<Object, Collection<?>>>getAsLeaf().value()
            .process(Arrays.asList("hello", "there", "general"))
            .size() == 3;
        assert tree.get("union").<Process<Object, Collection<?>>>getAsLeaf().value()
            .process(Arrays.asList("hello", "there", "general", "kenobi"))
            .size() == 4;
        assert tree.get("union").<Process<Object, Collection<?>>>getAsLeaf().value()
            .process(Arrays.asList("general", "kenobi"))
            .size() == 4;
        assert tree.get("union").<Process<Object, Collection<?>>>getAsLeaf().value()
            .process(Arrays.asList("general", "kenobi"))
            .containsAll(Arrays.asList("hello", "there", "general", "kenobi"));
        assert tree.get("intersect").<Process<Object, Collection<?>>>getAsLeaf().value()
            .process(Arrays.asList("general", "kenobi"))
            .size() == 2;
        assert tree.get("intersect").<Process<Object, Collection<?>>>getAsLeaf().value()
            .process(Arrays.asList("general", "kenobi"))
            .contains("kenobi");
        assert !tree.get("intersect").<Process<Object, Collection<?>>>getAsLeaf().value()
            .process(Arrays.asList("general", "kenobi"))
            .contains("hello");
        assert !tree.get("intersect").<Process<Object, Collection<?>>>getAsLeaf().value().process("hello")
            .contains("there");
        assert tree.get("intersect").<Process<Object, Collection<?>>>getAsLeaf().value().process("hello")
            .contains("hello");
        assert tree.get("intersect").<Process<Object, Collection<?>>>getAsLeaf().value().process("hello")
            .size() == 1;
    }
    
    @Test
    public void singleParsing() {
        final FernParser parser = new QueriableFernParser();
        assert !parser.parseQuery("> 10").compare(10);
        assert parser.parseQuery(">= 10").compare(10);
        assert parser.parseQuery("> 10").compare(11);
        assert parser.parseQuery("< 10").compare(9);
        assert parser.parseQuery("∈ \"hello there\"").compare("hello");
    }
    
    @Test
    public void structuredQuery() {
        final Object object = new Object() {
            final String name = "blob";
            final int age = 66;
            final String[] words = {"hello", "there"};
            @Export("foo")
            final long bar = 10;
            final boolean boo = true;
            final Object child = new Object() {
                final String name = "bean";
                final int age = 6;
            };
        };
        final FernParser parser = new QueriableFernParser();
        assert parser.matches(object, """
            name "blob",
            age 66
            """);
        assert parser.matches(object, """
            age [
                > 60,
                < 70,
                = 66,
                != 67,
                == 66
            ]
            """);
        assert parser.matches(object, """
            name ∈ "blobbit",
            age [
                >= 60,
                <= 70
            ],
            foo [= 10L, < 11, > 6]
            """);
        assert parser.matches(object, """
            words [
                ⊃ ["hello"],
                ⊂ ["hello", "there", "general", "kenobi"]
            ],
            foo ∈ [10L, 20L, "blob"]
            """);
        assert parser.matches(object, """
            name = "blob",
            child (
                name [= "bean", ∈ ["bean", "boon"]],
                age [= 6, > 4, <=7]
            )
            """);
        assert !parser.matches(object, """
            name = "blob",
            child (
                name [= "bean", ∈ ["box", "boon"]],
                age [= 6, > 4, <=7]
            )
            """);
    }
    
}

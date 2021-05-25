package mx.kenzie.fern.test;

import mx.kenzie.fern.Fern;
import mx.kenzie.fern.FernBranch;
import mx.kenzie.fern.FernList;
import mx.kenzie.fern.GenericFernParser;
import mx.kenzie.fern.parser.Parser;
import org.junit.Test;

public class FernParsingTest {
    
    final Parser<FernBranch> parser = new GenericFernParser();
    
    @Test
    public void basic() {
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("test_item.fern"));
        assert tree != null;
        assert tree.isBranch();
        assert !tree.isLeaf();
        assert !tree.isEmpty();
        assert tree.size() == 1;
        final Fern fern = tree.get("test_item");
        assert fern != null;
        assert fern.isBranch();
        final FernBranch branch = fern.getAsBranch();
        assert branch != null;
        assert branch.size() == 5;
        final Fern name = branch.get("name");
        assert name.isLeaf();
        assert name.getRawValue().equals("Test Item");
        assert name.getAsLeaf(String.class).value().equals("Test Item");
        assert branch.get("description").isLeaf();
        assert branch.get("description").getRawValue().equals("This is a \n description. \\ \"c");
        assert branch.get("worth").isBranch();
        assert !branch.get("worth").isLeaf();
        assert branch.get("worth").getAsBranch().size() == 2;
        assert branch.get("worth").getAsBranch().get("gold").getRawValue().equals(1);
        assert branch.get("worth").getAsBranch().get("silver").getRawValue().equals(10.0);
        assert branch.get("damage").isLeaf();
        assert !branch.get("damage").isBranch();
        assert branch.get("damage").getAsLeaf(Float.class).value() == 0.8F;
        assert branch.get("numbers").isBranch();
        final FernBranch numbers = branch.get("numbers").getAsBranch();
        assert numbers != null;
        assert numbers.size() == 8;
        assert numbers.get("int").getAsLeaf(Integer.class).value() == 1;
        assert numbers.get("byte").getAsLeaf(Byte.class).value() == 2;
        assert numbers.get("bool").getAsLeaf(Boolean.class).value();
        assert numbers.get("none").getAsLeaf(Void.class).value() == null;
        assert numbers.get("long").getAsLeaf(Long.class).value() == 100;
        assert numbers.get("short").getAsLeaf(Short.class).value() == 4;
        assert numbers.get("float").getAsLeaf(Float.class).value() == 4.0;
        assert numbers.get("double").getAsLeaf(Double.class).value() == -3.0;
    }
    
    @Test
    public void aspects() {
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("mapping_test.fern"));
        assert tree != null;
        assert tree.isBranch();
        assert !tree.isLeaf();
        assert !tree.isEmpty();
        assert tree.size() == 3;
        assert tree.getDirect("root_level_key").equals("value");
        assert tree.<Boolean>getDirect("another_map/boolean");
        assert tree.getDirect("another_map/void") == null;
        assert tree.<Float>getDirect("another_map/float") == 1.3F;
        final FernList a = tree.get("another_map").getAsBranch().get("list_a").getAsList();
        assert a.size() == 3;
        assert a.get(2).getRawValue().equals("value");
        final FernList b = tree.get("another_map").getAsBranch().get("list_b").getAsList();
        assert b.size() == 3;
        assert b.get(1).getRawValue().equals(10);
        assert b.get(2).getRawValue().equals("value");
        final FernList c = tree.get("another_map").getAsBranch().get("list_c").getAsList();
        assert c.size() == 2;
        assert c.get(0).isList();
        assert c.get(0).getAsList().size() == 2;
        assert c.get(0).getAsList().get(1).getRawValue().equals("list");
        assert c.get(1).isBranch();
        assert c.get(1).getAsBranch().size() == 1;
        assert c.get(1).getAsBranch().get("inner").getRawValue().equals("map");
    }
    
    @Test
    public void serialisation() {
        final FernBranch tree = parser.parse(getClass().getClassLoader().getResourceAsStream("mapping_test.fern"));
        final String string = tree.toString();
        assert string
            .equals("(another_map (boolean true,float 1.3F,list_a [\"value\", \"value\", \"value\"],list_b [\"value\", 10, \"value\"],list_c [[\"inner\", \"list\"], (inner \"map\")],void null),root_level_key \"value\",start_map (another_key 10.5,byte_value 2b,key \"value\",map ()))");
        final FernBranch second = parser.parse(string.substring(1, string.length() - 1));
        assert tree.equals(second);
    }
    
}

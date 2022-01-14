package mx.kenzie.fern.test;

import mx.kenzie.fern.FernBranch;
import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.FlyoverFernParser;
import mx.kenzie.fern.GenericFernParser;

import java.io.InputStream;

public class FlyoverParsingTest {
    
    public void simple() {
        final FernParser parser = new FlyoverFernParser();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("flyover.fern");
        assert new GenericFernParser().parse(stream) != null;
        final FernBranch branch = parser.parse(getClass().getClassLoader().getResourceAsStream("flyover.fern"));
        assert branch != null;
        System.out.println(branch.toRootString());
        assert branch.size() == 2;
        assert branch.get("test_map").getAsBranch().size() == 8;
    }
    
}

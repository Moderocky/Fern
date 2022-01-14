package mx.kenzie.fern.handler;

import mx.kenzie.fern.FernBranch;
import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.parser.BracketReader;
import mx.kenzie.fern.parser.ParserBase;

public class MapHandler implements ValueHandler<FernBranch> {
    @Override
    public boolean matches(String string) {
        return string.startsWith("(")
            && string.endsWith(")");
    }
    
    @Override
    public FernBranch parse(String string, FernParser parser) {
        final FernBranch branch = new FernBranch();
        assert parser instanceof ParserBase;
        ((ParserBase<String>) parser).parseMap(new BracketReader(string).readBracketPairInside('(', ')'), branch);
        return branch;
    }
    
    @Override
    public FernBranch parse(String string) {
        throw new IllegalStateException("Map handler cannot parse input without FernParser instance.");
    }
    
    @Override
    public boolean isOfType(Object thing) {
        return thing instanceof FernBranch;
    }
    
    @Override
    public String toString(FernBranch thing) {
        return thing.toString();
    }
}

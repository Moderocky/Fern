package mx.kenzie.fern.handler.flyover;

import mx.kenzie.fern.FernBranch;
import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.handler.MapHandler;
import mx.kenzie.fern.parser.ParserBase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FlyoverMapHandler extends MapHandler {
    @Override
    public FernBranch parse(String string, FernParser parser) {
        final FernBranch branch = new FernBranch();
        assert parser instanceof ParserBase;
        ((ParserBase<InputStream>) parser).parseMap(new ByteArrayInputStream(string.substring(1, string.length() - 1)
            .getBytes()), branch);
        return branch;
    }
}

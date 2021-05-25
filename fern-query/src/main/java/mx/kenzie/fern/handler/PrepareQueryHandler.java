package mx.kenzie.fern.handler;

import mx.kenzie.fern.FernBranch;
import mx.kenzie.fern.QueriableFernParser;

public class PrepareQueryHandler implements ValueHandler<FernBranch> {
    @Override
    public boolean matches(String string) {
        return string.startsWith("query");
    }
    
    @Override
    public FernBranch parse(String string) {
        final String result = string.substring(5).trim();
        return new QueriableFernParser().parse(result);
    }
}

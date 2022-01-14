package mx.kenzie.fern.parser;

import mx.kenzie.fern.FernBranch;

public interface ParserBase<T> {
    
    void parseMap(final T content, final FernBranch branch);
    
}

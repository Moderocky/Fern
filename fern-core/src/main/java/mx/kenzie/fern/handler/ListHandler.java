package mx.kenzie.fern.handler;

import mx.kenzie.fern.FernList;
import mx.kenzie.fern.FernParser;
import mx.kenzie.fern.parser.BracketReader;
import mx.kenzie.fern.parser.Parser;

public class ListHandler implements ValueHandler<FernList> {
    @Override
    public boolean matches(String string) {
        return string.startsWith("->") || (
            string.startsWith("[")
                && string.endsWith("]")
        );
    }
    
    @Override
    public FernList parse(String string, FernParser parser) {
        if (string.startsWith("[") && string.endsWith("]")) {
            final FernList list = new FernList();
            for (String s : Parser.unwrapCommaList(new BracketReader(string).readBracketPairInside('[', ']'))) {
                list.add(parser.parseElement(s.trim()));
            }
            return list;
        } else if (string.startsWith("->")) {
            final FernList list = new FernList();
            final BracketReader reader = new BracketReader(string);
            while (reader.canRead()) {
                final String part = reader.readUntilIgnoreAll("->").trim();
                reader.read(2);
                if (part.isEmpty()) continue;
                list.add(parser.parseElement(part.trim()));
            }
            return list;
        }
        throw new IllegalArgumentException("Unexpected list designator in: '" + string + "'");
    }
    
    @Override
    public FernList parse(String string) {
        throw new IllegalStateException("List handler cannot parse input without FernParser instance.");
    }
    
    @Override
    public boolean isOfType(Object thing) {
        return thing instanceof FernList;
    }
    
}

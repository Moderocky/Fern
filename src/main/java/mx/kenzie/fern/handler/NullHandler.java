package mx.kenzie.fern.handler;

import mx.kenzie.fern.ValueHandler;
import mx.kenzie.fern.meta.FernException;

public class NullHandler implements ValueHandler<Void> {
    
    @Override
    public boolean accept(StringBuilder builder, char c) {
        if (c != 'n' && c != 'u' && c != 'l') return false;
        builder.append(c);
        return true;
    }
    
    @Override
    public Void result(StringBuilder builder) {
        final String string = builder.toString();
        if (string.equals("null")) return null;
        throw new FernException("Unexpected null in the bagging area: '" + string + "'");
    }
    
    @Override
    public String undo(Void boo) {
        return "null";
    }
    
    @Override
    public boolean accept(Object number) {
        return number == null;
    }
}

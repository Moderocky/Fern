package mx.kenzie.fern.handler;

import mx.kenzie.fern.ValueHandler;
import mx.kenzie.fern.meta.FernException;

import java.util.Objects;

public class BooleanHandler implements ValueHandler<Boolean> {
    
    boolean value;
    
    @Override
    public boolean accept(StringBuilder builder, char c) {
        this.value = (c == 't');
        if (Character.isWhitespace(c)) return false;
        builder.append(c);
        return true;
    }
    
    @Override
    public Boolean result(StringBuilder builder) {
        final String string = builder.toString();
        if (string.equals("true")) return true;
        else if (string.equals("false")) return false;
        throw new FernException("Unexpected boolean in the bagging area: '" + string + "'");
    }
    
    @Override
    public String undo(Boolean boo) {
        return Objects.toString(boo);
    }
    
    @Override
    public boolean accept(Object number) {
        return number instanceof Boolean;
    }
}

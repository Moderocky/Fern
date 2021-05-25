package mx.kenzie.fern.handler;

public class NullHandler implements ValueHandler<Void> {
    @Override
    public boolean matches(String string) {
        return string.equalsIgnoreCase("null");
    }
    
    @Override
    public Void parse(String string) {
        return null;
    }
}

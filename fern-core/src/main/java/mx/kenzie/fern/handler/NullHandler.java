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
    
    @Override
    public boolean isOfType(Object thing) {
        return thing == null;
    }
}

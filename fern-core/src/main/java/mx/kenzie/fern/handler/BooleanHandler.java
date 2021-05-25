package mx.kenzie.fern.handler;

public class BooleanHandler implements ValueHandler<Boolean> {
    @Override
    public boolean matches(String string) {
        return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false");
    }
    
    @Override
    public Boolean parse(String string) {
        return Boolean.parseBoolean(string.toLowerCase());
    }
}

package mx.kenzie.fern.handler;

public class LongHandler implements ValueHandler<Long> {
    @Override
    public boolean matches(String string) {
        return string.matches("-?[0-9]{1,19}[lL]?");
    }
    
    @Override
    public Long parse(String string) {
        if (string.endsWith("l") || string.endsWith("L"))
            return Long.parseLong(string.substring(0, string.length() - 1));
        return Long.parseLong(string);
    }
    
    @Override
    public boolean isOfType(Object thing) {
        return thing instanceof Long;
    }
    
    @Override
    public String toString(Long thing) {
        return thing + "L";
    }
    
}

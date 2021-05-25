package mx.kenzie.fern.handler;

public class ShortHandler implements ValueHandler<Short> {
    @Override
    public boolean matches(String string) {
        return string.matches("-?[0-9]{1,19}[sS]?");
    }
    
    @Override
    public Short parse(String string) {
        if (string.endsWith("s") || string.endsWith("S"))
            return Short.parseShort(string.substring(0, string.length() - 1));
        return Short.parseShort(string);
    }
}

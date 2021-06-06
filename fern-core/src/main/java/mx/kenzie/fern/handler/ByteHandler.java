package mx.kenzie.fern.handler;

public class ByteHandler implements ValueHandler<Byte> {
    @Override
    public boolean matches(String string) {
        return string.matches("-?[0-9]{1,3}[bB]");
    }
    
    @Override
    public Byte parse(String string) {
        if (string.endsWith("b") || string.endsWith("B"))
            return Byte.parseByte(string.substring(0, string.length() - 1));
        return Byte.parseByte(string);
    }
    
    @Override
    public String toString(Byte thing) {
        return thing + "B";
    }
    
}

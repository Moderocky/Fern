package mx.kenzie.fern.handler;

public class FloatHandler implements ValueHandler<Float> {
    @Override
    public boolean matches(String string) {
        return string.matches("-?[0-9]+(\\.[0-9]+)?[fF]");
    }
    
    @Override
    public Float parse(String string) {
        return Float.parseFloat(string.substring(0, string.length() - 1));
    }
    
    @Override
    public String toString(Float thing) {
        return thing + "F";
    }
}

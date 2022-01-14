package mx.kenzie.fern.handler;

public class DoubleHandler implements ValueHandler<Double> {
    @Override
    public boolean matches(String string) {
        return string.matches("-?[0-9]+(\\.[0-9]+)?[dD]")
            || string.matches("-?[0-9]+\\.[0-9]+[dD]?");
    }
    
    @Override
    public Double parse(String string) {
        if (string.endsWith("d") || string.endsWith("D"))
            return Double.parseDouble(string.substring(0, string.length() - 1));
        return Double.parseDouble(string);
    }
    
    @Override
    public boolean isOfType(Object thing) {
        return thing instanceof Double;
    }
    
    @Override
    public String toString(Double thing) {
        return thing + "";
    }
}

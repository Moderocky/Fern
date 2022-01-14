package mx.kenzie.fern.handler;

public class IntegerHandler implements ValueHandler<Integer> {
    @Override
    public boolean matches(String string) {
        return string.matches("-?[0-9]{1,10}");
    }
    
    @Override
    public Integer parse(String string) {
        return Integer.parseInt(string);
    }
    
    @Override
    public boolean isOfType(Object thing) {
        return thing instanceof Integer;
    }
}

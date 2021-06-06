package mx.kenzie.fern.handler;

public class ClassHandler implements ValueHandler<Class<?>> {
    @Override
    public boolean matches(String string) {
        return string.matches("(?:[a-zA-Z_$][a-zA-Z\\d_$]*\\.)+[a-zA-Z_$][a-zA-Z\\d_$]+");
    }
    
    @Override
    public Class<?> parse(String string) {
        try {
            return Class.forName(string);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    @Override
    public String toString(Class<?> thing) {
        return thing.getName();
    }
}

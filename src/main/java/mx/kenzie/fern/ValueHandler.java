package mx.kenzie.fern;

public interface ValueHandler<Type> {
    
    boolean accept(StringBuilder builder, char c);
    
    Type result(StringBuilder builder);
    
    String undo(Type type);
    
    boolean accept(Object type);
    
}

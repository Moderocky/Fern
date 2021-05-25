package mx.kenzie.fern;

public interface Process<T, R> {
    
    R process(T thing);
    
}

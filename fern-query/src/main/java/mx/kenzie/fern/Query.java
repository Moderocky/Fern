package mx.kenzie.fern;

public interface Query<T> {
    boolean compare(T thing);
}

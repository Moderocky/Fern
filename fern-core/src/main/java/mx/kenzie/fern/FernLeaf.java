package mx.kenzie.fern;

import java.util.Objects;

public record FernLeaf<T>(T value, String string) implements Fern {
    
    @Override
    public boolean isLeaf() {
        return true;
    }
    
    @Override
    public <V> FernLeaf<V> getAsLeaf() {
        return (FernLeaf<V>) this;
    }
    
    @Override
    public <V> FernLeaf<V> getAsLeaf(Class<V> type) {
        return (FernLeaf<V>) this;
    }
    
    @Override
    public T getRawValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return string;
    }
    
    public boolean is(Object thing) {
        if (thing instanceof FernLeaf)
            return thing.equals(this);
        return Objects.equals(value, thing);
    }
    
}

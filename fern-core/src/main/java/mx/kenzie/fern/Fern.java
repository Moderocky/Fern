package mx.kenzie.fern;

public interface Fern {
    
    default boolean isList() {
        return false;
    }
    
    default boolean isLeaf() {
        return false;
    }
    
    default mx.kenzie.fern.FernList getAsList() {
        throw new IllegalStateException("Unable to convert element.");
    }
    
    default <V> mx.kenzie.fern.FernLeaf<V> getAsLeaf() {
        throw new IllegalStateException("Unable to convert element.");
    }
    
    default <V> mx.kenzie.fern.FernLeaf<V> getAsLeaf(Class<V> type) {
        throw new IllegalStateException("Unable to convert element.");
    }
    
    default <T> T getDirect(final String path) {
        if (!this.isBranch()) throw new IllegalStateException("Cannot be used outside branches.");
        FernBranch branch = this.getAsBranch();
        for (String s : path.split("/")) {
            Fern fern = branch.get(s);
            if (fern instanceof FernBranch) branch = (FernBranch) fern;
            else {
                return (T) fern.getRawValue();
            }
        }
        return null;
    }
    
    default boolean isBranch() {
        return false;
    }
    
    default FernBranch getAsBranch() {
        throw new IllegalStateException("Unable to convert element.");
    }
    
    Object getRawValue();
    
    default String toString(FernParser parser) {
        return toString();
    }
    
}

package mx.kenzie.fern.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class FernMap extends LinkedHashMap<String, Object> {
    public FernMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    public FernMap(int initialCapacity) {
        super(initialCapacity);
    }
    
    public FernMap() {
    }
    
    public FernMap(Map<? extends String, ?> m) {
        super(m);
    }
    
    public FernMap getMap(String key) {
        return (FernMap) this.get(key);
    }
    
}

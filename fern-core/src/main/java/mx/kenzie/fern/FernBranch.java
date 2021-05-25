package mx.kenzie.fern;

import java.util.*;
import java.util.Map.Entry;

public class FernBranch extends TreeMap<String, mx.kenzie.fern.Fern> implements mx.kenzie.fern.Fern {
    
    @Override
    public boolean isBranch() {
        return true;
    }
    
    @Override
    public FernBranch getAsBranch() {
        return this;
    }
    
    @Override
    public FernBranch getRawValue() {
        return this;
    }
    
    public String toRootString() {
        final String string = this.toString();
        return string.substring(1, string.length() - 1);
    }
    
    @Override
    public String toString() {
        final List<String> strings = new ArrayList<>();
        for (Entry<String, mx.kenzie.fern.Fern> entry : this.entrySet()) {
            strings.add(entry.getKey() + " " + entry.getValue().toString());
        }
        return "(" + String.join(",", strings) + ")";
    }
    
    public Entry<String, mx.kenzie.fern.Fern> getEntry(int index) {
        return (Entry<String, mx.kenzie.fern.Fern>) entrySet().toArray(new Entry[0])[index];
    }
    
    public String[] toLinearArray() {
        final List<String> list = new ArrayList<>();
        for (Entry<String, mx.kenzie.fern.Fern> entry : entrySet()) {
            if (entry.getValue() instanceof FernBranch branch) {
                for (String s : branch.toLinearArray()) {
                    list.add(entry.getKey() + "/" + s);
                }
            } else {
                list.add(entry.getKey());
            }
        }
        return list.toArray(new String[0]);
    }
    
    public Map<String, Object> toLinearMap() {
        final Map<String, Object> map = new HashMap<>();
        for (Entry<String, mx.kenzie.fern.Fern> entry : entrySet()) {
            if (entry.getValue() instanceof FernBranch branch) {
                for (Entry<String, Object> child : branch.toLinearMap().entrySet()) {
                    map.put(entry.getKey() + "/" + child.getKey(), child.getValue());
                }
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
    
}

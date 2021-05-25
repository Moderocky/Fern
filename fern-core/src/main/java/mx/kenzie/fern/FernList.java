package mx.kenzie.fern;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FernList extends ArrayList<Fern> implements Fern {
    
    @Override
    public boolean isList() {
        return true;
    }
    
    @Override
    public FernList getAsList() {
        return this;
    }
    
    @Override
    public Fern[] getRawValue() {
        return this.toArray(new Fern[0]);
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
    @Override
    public boolean contains(Object o) {
        if (o instanceof Fern) return super.contains(o);
        for (Fern fern : this) {
            if (fern instanceof mx.kenzie.fern.FernLeaf<?> leaf
                && Objects.equals(leaf.value(), o)) return true;
        }
        return false;
    }
    
    @Override
    public Fern[] toArray() {
        return getRawValue();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof List<?> list)) return false;
        if (list.size() != size()) return false;
        for (int i = 0; i < this.size(); i++) {
            if (!list.get(i).equals(this.get(i))) return false;
        }
        return true;
    }
    
    public List<Object> toRawList() {
        final List<Object> list = new ArrayList<>();
        for (Fern fern : this) {
            list.add(fern.getRawValue());
        }
        return list;
    }
}

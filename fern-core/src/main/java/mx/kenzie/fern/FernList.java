package mx.kenzie.fern;

import java.lang.reflect.Array;
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
    public String toString(int indent, FernParser parser) {
        if (this.isEmpty()) return "[]";
        final List<String> strings = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        final int child = indent + 1;
        builder.append("[").append("\n");
        for (Fern entry : this) {
            final StringBuilder sub = new StringBuilder();
            for (int i = 0; i < child; i++) {
                sub.append(parser.getIndentationUnit());
            }
            sub.append(entry.toString(child, parser));
            strings.add(sub.toString());
        }
        builder.append(String.join(",\n", strings))
            .append("\n");
        for (int i = 0; i < indent; i++) {
            builder.append(parser.getIndentationUnit());
        }
        builder.append("]");
        return builder.toString();
    }
    
    @Override
    public String toString() {
        if (this.isEmpty()) return "[]";
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
    
    public <Type> Type[] arrayConversion(final Class<Type> cls) {
        final Type[] array = (Type[]) Array.newInstance(cls, this.size());
        int x = 0;
        for (Fern fern : this) {
            array[x] = (Type) fern.getRawValue();
            x++;
        }
        return array;
    }
    
    public <Type> Type[] arrayConversion(final Type[] template) {
        return this.arrayConversion((Class<Type>) template.getClass().getComponentType());
    }
}

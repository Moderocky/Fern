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
    
    @Override
    public String toString(final int indent, final FernParser parser) {
        if (this.isEmpty()) return "()";
        final List<String> strings = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        final int child = indent + 1;
        builder.append("(").append("\n");
        for (Entry<String, mx.kenzie.fern.Fern> entry : this.entrySet()) {
            final StringBuilder sub = new StringBuilder();
            for (int i = 0; i < child; i++) {
                sub.append(parser.getIndentationUnit());
            }
            sub.append(entry.getKey())
                .append(" ")
                .append(entry.getValue().toString(child, parser));
            strings.add(sub.toString());
        }
        builder.append(String.join(",\n", strings))
            .append("\n");
        for (int i = 0; i < indent; i++) {
            builder.append(parser.getIndentationUnit());
        }
        builder.append(")");
        return builder.toString();
    }
    
    public String toRootString() {
        return toRootString(false);
    }
    
    public String toRootString(final boolean compress) {
        if (compress) {
            final String string = this.toString();
            return string.substring(1, string.length() - 1);
        }
        final FernParser parser = new GenericFernParser();
        final List<String> strings = new ArrayList<>();
        for (Entry<String, mx.kenzie.fern.Fern> entry : this.entrySet()) {
            final StringBuilder sub = new StringBuilder();
            sub.append(entry.getKey())
                .append(" ")
                .append(entry.getValue().toString(0, parser));
            strings.add(sub.toString());
        }
        return String.join(",\n", strings);
    }
    
    @Override
    public String toString() {
        if (this.isEmpty()) return "()";
        final List<String> strings = new ArrayList<>();
        for (Entry<String, mx.kenzie.fern.Fern> entry : this.entrySet()) {
            strings.add(entry.getKey() + " " + entry.getValue().toString());
        }
        return "(" + String.join(", ", strings) + ")";
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

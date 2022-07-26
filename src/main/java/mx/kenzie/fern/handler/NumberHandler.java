package mx.kenzie.fern.handler;

import mx.kenzie.fern.ValueHandler;
import mx.kenzie.fern.meta.FernException;

import java.util.Objects;

public class NumberHandler implements ValueHandler<Number> {
    
    boolean tail, decimal, f, l, s, b;
    
    @Override
    public boolean accept(StringBuilder builder, char c) {
        if (c == '.') {
            if (decimal) throw new FernException("Additional decimal point in number " + builder + ".");
            decimal = true;
        } else if (Character.isWhitespace(c)) return false;
        else if (c == 'F' || c == 'f' && !f) this.f = this.tail = true;
        else if (c == 'D' || c == 'd') this.decimal = this.tail = true;
        else if (c == 'L' || c == 'l') this.l = this.tail = true;
        else if (c == 'S' || c == 's') this.s = this.tail = true;
        else if (c == 'B' || c == 'b') this.b = this.tail = true;
        else if (c == ']' || c == ')') return false;
        else if (!Character.isDigit(c) && c != '-')
            throw new FernException("Unexpected character in the bagging area " + builder + " .. " + c);
        builder.append(c);
        return true;
    }
    
    @Override
    public Number result(StringBuilder builder) {
        final String string;
        if (tail) string = builder.substring(0, builder.length() - 1);
        else string = builder.toString();
        if (f) return Float.parseFloat(string);
        if (decimal) return Double.parseDouble(string);
        final long test = Long.parseLong(string);
        if (s) return (short) test;
        if (b) return (byte) test;
        if (!l && test == (int) test) return (int) test;
        return test;
    }
    
    @Override
    public String undo(Number number) {
        if (number instanceof Float value) return value + "F";
        if (number instanceof Double value) return value + "D";
        if (number instanceof Long value) return value + "L";
        if (number instanceof Short value) return value + "S";
        if (number instanceof Byte value) return value + "B";
        else return Objects.toString(number);
    }
    
    @Override
    public boolean accept(Object number) {
        return number instanceof Number;
    }
}

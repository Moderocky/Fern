package mx.kenzie.fern.handler;

import mx.kenzie.fern.ValueHandler;

public class StringHandler implements ValueHandler<String> {
    
    int stage;
    boolean escape;
    
    @Override
    public boolean accept(StringBuilder builder, char c) {
        if (!escape && c == '\\') {
            this.escape = true;
            return true;
        }
        builder.append(c);
        if (c == '"' && !escape) switch (stage) {
            case 0:
                stage++;
                break;
            case 1:
                return false;
        }
        if (escape) this.escape = false;
        return true;
    }
    
    @Override
    public String result(StringBuilder builder) {
        final String string = builder.toString();
        return string.substring(1, string.length() - 1)
            .replace("\\\\", "\\")
            .replace("\\t", "\t")
            .replace("\\b", "\b")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\f", "\f")
            .replace("\\'", "'")
            .replace("\\\"", "\"");
    }
    
    @Override
    public String undo(String s) {
        if (s.length() < 1) return "\"\"";
        return '"' + s.replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\f", "\\f")
            .replace("'", "\\'")
            .replace("\"", "\\\"") + '"';
    }
    
    @Override
    public boolean accept(Object object) {
        return object instanceof String;
    }
}

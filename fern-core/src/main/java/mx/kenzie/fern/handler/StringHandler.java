package mx.kenzie.fern.handler;

public class StringHandler implements ValueHandler<String> {
    @Override
    public boolean matches(String string) {
        return string.startsWith("\"") && string.endsWith("\"");
    }
    
    @Override
    public String parse(String string) {
        final String result = string.substring(1, string.length() - 1);
        if (result.contains("\\")) {
            return result
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\");
        }
        return result;
    }
}

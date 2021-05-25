package mx.kenzie.fern.parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public interface Parser<T> {
    
    Pattern LINE_COMMENT = Pattern.compile("//.+(?=(\\R|$))");
    Pattern BLOCK_COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/");
    
    static String stripQuotation(String quote) {
        final String line = quote.trim();
        if (line.startsWith("\"") && line.endsWith("\""))
            return line.substring(1, line.length() - 1);
        return line;
    }
    
    static String[] unwrapCommaList(String content) {
        mx.kenzie.fern.parser.BracketReader reader = new mx.kenzie.fern.parser.BracketReader(content);
        return reader.splitIgnoreAll(',');
    }
    
    static String burnWhitespace(String content) {
        return new mx.kenzie.fern.parser.BracketReader(content).burnWhitespace();
    }
    
    static String removeComments(String content) {
        return content
            .replaceAll(LINE_COMMENT.pattern(), "")
            .replaceAll(BLOCK_COMMENT.pattern(), "");
    }
    
    default T parse(final InputStream resource) {
        final StringBuilder builder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
            (resource, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                builder.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parse(builder.toString());
    }
    
    T parse(final String string);
    
}

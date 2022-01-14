package mx.kenzie.fern.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FlyoverStreamReader {
    
    protected final InputStream stream;
    protected final InputStreamReader reader;
    
    public FlyoverStreamReader(final InputStream stream) {
        this.stream = stream;
        this.reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
    
    public String readUntil(boolean escape, final char c) {
        final StringBuilder builder = new StringBuilder();
        boolean ignore = false;
        try {
            char s;
            while ((s = (char) reader.read()) != -1) {
                if (escape && s == '\\' && !ignore) ignore = true;
                if (s == c && !ignore) break;
                else if (escape && s != '\\' && ignore) ignore = false;
                builder.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
    
    public String readUntil(boolean escape, final char... cs) {
        Arrays.sort(cs);
        boolean ignore = false;
        final StringBuilder builder = new StringBuilder();
        try {
            char s;
            while ((s = (char) reader.read()) != -1) {
                if (escape && s == '\\' && !ignore) ignore = true;
                if (!ignore && Arrays.binarySearch(cs, s) > -1) break;
                else if (escape && s != '\\' && ignore) ignore = false;
                builder.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
    
    public String readUntilDeadEnd(boolean escape, final char end, final char... cs) {
        Arrays.sort(cs);
        boolean ignore = false;
        final StringBuilder builder = new StringBuilder();
        try {
            char s;
            while ((s = (char) reader.read()) != -1) {
                if (s == end) throw new DeadEnd();
                if (escape && s == '\\' && !ignore) ignore = true;
                if (!ignore && Arrays.binarySearch(cs, s) > -1) break;
                else if (escape && s != '\\' && ignore) ignore = false;
                builder.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
    
    public String readUntilNested(boolean escape, final char c) {
        boolean ignore = false;
        int depth = 0;
        final StringBuilder builder = new StringBuilder();
        try {
            int s;
            while ((s = reader.read()) != -1) {
                if (escape && s == '\\' && !ignore) ignore = true;
                else if (!ignore && (s == ')'
                    || s == ']'
                )) depth--;
                else if (!ignore && (s == '('
                    || s == '['
                )) depth++;
                if (depth < 0) throw new DeadEnd();
                else if (s == c && !ignore && depth < 1) break;
                else if (escape && s != '\\' && ignore) ignore = false;
                builder.append((char) s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
    
    @Override
    protected void finalize() throws Throwable {
        reader.close();
    }
    
    public static class DeadEnd extends Error {
    }
    
}

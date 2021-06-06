package mx.kenzie.fern.parser;

import mx.kenzie.magic.collection.MagicStringList;
import mx.kenzie.magic.magic.StringReader;

import java.util.Arrays;

public class BracketReader extends StringReader {
    
    public BracketReader(String string) {
        this(string.toCharArray());
    }
    
    public BracketReader(char[] chars) {
        super(chars);
    }
    
    public String[] splitIgnoreAll(char c) {
        int depth = 0;
        boolean ignore = false;
        boolean isChonky = false;
        boolean isSilly = false;
        boolean isBracket = false;
        MagicStringList list = new MagicStringList();
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == '"' && !isSilly) isChonky = !isChonky;
            else if (current == '\'' && !isChonky) isSilly = !isSilly;
            else if (current == ')' && !isChonky && !isSilly) {
                depth--;
                if (depth < 1) isBracket = false;
            } else if (current == '(' && !isChonky && !isSilly) {
                depth++;
                isBracket = true;
            } else if (current == ']' && !isChonky && !isSilly) {
                depth--;
                if (depth < 1) isBracket = false;
            } else if (current == '[' && !isChonky && !isSilly) {
                depth++;
                isBracket = true;
            }
            if (current == c && !isBracket && !isChonky && !isSilly) {
                list.add(builder.toString());
                builder = new StringBuilder();
            } else {
                builder.append(current);
            }
        }
        if (!builder.toString().isEmpty()) list.add(builder.toString());
        return list.toArray();
    }
    
    public String burnWhitespace() {
        boolean ignore = false;
        boolean isChonky = false;
        boolean isSilly = false;
        boolean isSpace = false;
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == '"' && !isSilly) isChonky = !isChonky;
            else if (current == '\'' && !isChonky) isSilly = !isSilly;
            if (!isSilly && !isChonky) {
                if (current == '\n') continue;
                else if (current == '\r') continue;
                else if (isSpace && (current == ' '
                    || current == '\t')) continue;
            }
            isSpace = (current == ' '
                || current == '\t');
            builder.append(isSpace ? ' ' : current);
        }
        return builder.toString();
    }
    
    public String readBracketPair(char open, char close) {
        int depth = 0;
        boolean ignore = false;
        super.readUntil(open);
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char current = rotate();
            builder.append(current);
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == close) {
                depth--;
                if (depth < 1) break;
            } else if (current == open) depth++;
        }
        return builder.toString();
    }
    
    public String readBracketPairInside(char open, char close) {
        int depth = 1;
        boolean ignore = false;
        boolean inString = false;
        super.readUntil(open);
        rotate();
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (!inString && current == '"') inString = true;
            else if (inString && current == '"') inString = false;
            else if (current == close && !inString) {
                depth--;
                if (depth < 1) break;
            } else if (current == open && !inString) depth++;
            builder.append(current);
        }
        return builder.toString();
    }
    
    public boolean hasBracketPair(char open, char close) {
        int depth = 0;
        boolean has = false;
        boolean ignore = false;
        super.readUntil(open);
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char current = rotate();
            builder.append(current);
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == close) {
                depth--;
                if (depth < 1) break;
            } else if (current == open) {
                depth++;
                has = true;
            }
        }
        return has && depth == 0;
    }
    
    public String readString(char delimiter) {
        boolean ignore = false;
        super.readUntil(delimiter);
        rotate();
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == delimiter) break;
            builder.append(current);
        }
        return builder.toString();
    }
    
    public String readOutComments() {
        int depth = 0;
        boolean ignore = false;
        boolean finishing = false;
        StringBuilder builder = new StringBuilder();
        builder.append(readUntil('/', '*'));
        char last = ' ';
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            if (finishing) finishing = false;
            else if (current == '\\') ignore = true;
            else if (current == '/' && last == '*') {
                finishing = true;
                depth--;
            } else if (current == '/' && canRead() && current() == '*') depth++;
            if (depth < 1 && !finishing) builder.append(current);
            last = current;
        }
        return builder.toString();
    }
    
    public String readUntil(char c, char next) {
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            char test = this.chars[position];
            if (c == test && hasNext() && next() == next) break;
            builder.append(test);
            position++;
        }
        return builder.toString();
    }
    
    public BracketReader readOut(String string) {
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (!canRead()) return this;
            if (c == current()) rotate();
            else return this;
        }
        return this;
    }
    
    public String readUntil(String next) {
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            builder.append(readUntil(next.charAt(0)));
            if (incoming(next)) return builder.toString();
        }
        return builder.toString();
    }
    
    public boolean incoming(String string) {
        BracketReader reader = this.clone();
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < string.length(); i++) {
                builder.append(reader.rotate());
            }
            return builder.toString().equals(string);
        } catch (RuntimeException ex) {
            return false;
        }
    }
    
    public BracketReader clone() {
        BracketReader reader = new BracketReader(this.chars);
        reader.position = this.position;
        return reader;
    }
    
    public String remainingString() {
        return new String(remaining());
    }
    
    public String readElement(char c) {
        StringBuilder builder = new StringBuilder();
        while (this.canRead() && this.hasNext()) {
            if (this.hasNext() && next() == c) return builder.toString();
            rotate();
        }
        for (builder = new StringBuilder(); this.canRead(); ++this.position) {
            char test = this.chars[this.position];
            if (c == test) {
                break;
            }
            
            builder.append(test);
        }
        
        return builder.toString();
    }
    
    public String readUntil(char... cs) {
        Arrays.sort(cs);
        StringBuilder builder;
        for (builder = new StringBuilder(); this.canRead(); ++this.position) {
            char test = this.chars[this.position];
            if (Arrays.binarySearch(cs, test) > -1) {
                break;
            }
            builder.append(test);
        }
        return builder.toString();
    }
    
    public String readUntilEscape(char... cs) {
        Arrays.sort(cs);
        StringBuilder builder = new StringBuilder();
        for (boolean ignore = false; this.canRead(); ++this.position) {
            char test = this.chars[this.position];
            if (ignore) {
                ignore = false;
            } else if (test == '\\') {
                ignore = true;
            } else if (Arrays.binarySearch(cs, test) > -1) {
                break;
            }
            
            builder.append(test);
        }
        return builder.toString();
    }
    
    public String readUntilIgnoreAll(String next) {
        StringBuilder builder = new StringBuilder();
        int depth = 0;
        boolean ignore = false;
        boolean isChonky = false;
        boolean isSilly = false;
        boolean isBracket = false;
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == '"' && !isSilly) isChonky = !isChonky;
            else if (current == '\'' && !isChonky) isSilly = !isSilly;
            else if (current == ')' && !isChonky && !isSilly) {
                depth--;
                if (depth < 1) isBracket = false;
            } else if (current == '(' && !isChonky && !isSilly) {
                depth++;
                isBracket = true;
            }
            if (current == next.charAt(0) && !isBracket && !isChonky && !isSilly) {
                if (new String(this.remainingInclude()).startsWith(next)) {
                    rotateBack(1);
                    return builder.toString();
                }
            }
            builder.append(current);
        }
        return builder.toString();
    }
    
    public char[] remainingInclude() {
        return Arrays.copyOfRange(this.chars, this.position - 1, this.chars.length);
    }
    
    public String readUntilIgnoreAll(char test) {
        StringBuilder builder = new StringBuilder();
        int depth = 0;
        boolean ignore = false;
        boolean isChonky = false;
        boolean isSilly = false;
        boolean isBracket = false;
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == '"' && !isSilly) isChonky = !isChonky;
            else if (current == '\'' && !isChonky) isSilly = !isSilly;
            else if (current == ')' || current == '}' || current == ']') {
                depth--;
                if (depth < 1) isBracket = false;
            } else if (current == '(' || current == '{' || current == '[') {
                depth++;
                isBracket = true;
            }
            builder.append(current);
            if (current == test && !isBracket && !isChonky && !isSilly) {
                return builder.toString();
            }
        }
        return builder.toString();
    }
    
    public String readUntilReaches(char test) {
        StringBuilder builder = new StringBuilder();
        while (canRead()) {
            builder.append(rotate());
            if (hasNext() && next() == test) {
                builder.append(current());
                break;
            }
        }
        return builder.toString();
    }
    
    public String readUntilBeforeIgnoreAll(char test) {
        StringBuilder builder = new StringBuilder();
        int depth = 0;
        boolean ignore = false;
        boolean isChonky = false;
        boolean isSilly = false;
        boolean isBracket = false;
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == '"' && !isSilly) isChonky = !isChonky;
            else if (current == '\'' && !isChonky) isSilly = !isSilly;
            else if (current == ')' || current == '}' || current == ']') {
                depth--;
                if (depth < 1) isBracket = false;
            } else if (current == '(' || current == '{' || current == '[') {
                depth++;
                isBracket = true;
            }
            if (current == test && !isBracket && !isChonky && !isSilly) {
                rotateBack(1);
                return builder.toString();
            }
            builder.append(current);
        }
        return builder.toString();
    }
    
    public String readStartingIgnoreAll(char test) {
        StringBuilder builder = new StringBuilder();
        int depth = 0;
        boolean allow = true;
        boolean ignore = false;
        boolean isChonky = false;
        boolean isSilly = false;
        boolean isBracket = false;
        while (canRead()) {
            char current = rotate();
            if (ignore) ignore = false;
            else if (current == '\\') ignore = true;
            else if (current == '"' && !isSilly) isChonky = !isChonky;
            else if (current == '\'' && !isChonky) isSilly = !isSilly;
            else if (current == ')' || current == '}' || current == ']') {
                depth--;
                if (depth < 1) isBracket = false;
            } else if (current == '(' || current == '{' || current == '[') {
                depth++;
                isBracket = true;
            }
            check:
            if (current == test && !isBracket && !isChonky && !isSilly) {
                if (allow) {
                    allow = false;
                    break check;
                }
                rotateBack(1);
                return builder.toString();
            }
            builder.append(current);
        }
        return builder.toString();
    }
    
}

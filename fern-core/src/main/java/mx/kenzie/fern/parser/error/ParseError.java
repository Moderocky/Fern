package mx.kenzie.fern.parser.error;

public class ParseError extends Error {
    
    public ParseError() {
        super();
    }
    
    public ParseError(String message) {
        super(message);
    }
    
    public ParseError(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ParseError(Throwable cause) {
        super(cause);
    }
}

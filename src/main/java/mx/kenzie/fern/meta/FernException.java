package mx.kenzie.fern.meta;

public class FernException extends RuntimeException {
    
    public FernException() {
    }
    
    public FernException(String message) {
        super(message);
    }
    
    public FernException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FernException(Throwable cause) {
        super(cause);
    }
    
    public FernException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

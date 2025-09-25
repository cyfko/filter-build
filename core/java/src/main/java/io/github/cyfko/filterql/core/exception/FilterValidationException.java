package io.github.cyfko.filterql.core.exception;

/**
 * Exception thrown when a filter condition cannot be validated or constructed.
 */
public class FilterValidationException extends Exception {
    
    public FilterValidationException(String message) {
        super(message);
    }
    
    public FilterValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

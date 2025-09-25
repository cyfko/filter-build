package io.github.cyfko.filterql.core.exception;

/**
 * Exception thrown when a DSL expression contains syntax errors or invalid references.
 */
public class DSLSyntaxException extends Exception {
    
    public DSLSyntaxException(String message) {
        super(message);
    }
    
    public DSLSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}

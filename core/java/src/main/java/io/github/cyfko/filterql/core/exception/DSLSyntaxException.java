package io.github.cyfko.filterql.core.exception;

/**
 * Exception thrown when a DSL expression contains syntax errors
 * or invalid references.
 * <p>
 * This exception is used to signal syntax validation problems
 * when processing DSL (Domain Specific Language) expressions.
 * </p>
 * <p>
 * It clearly distinguishes errors from parsing or DSL syntax validation from other exception types.
 * </p>
 *
 * @author Frank KOSSI
 * @since 2.0.0
 */
public class DSLSyntaxException extends RuntimeException {

    /**
     * Constructor with an explanatory error message.
     *
     * @param message the message describing the cause of the exception
     */
    public DSLSyntaxException(String message) {
        super(message);
    }

    /**
     * Constructor with an explanatory message and an underlying cause.
     *
     * @param message the message describing the cause of the exception
     * @param cause   the original cause of this exception
     */
    public DSLSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}


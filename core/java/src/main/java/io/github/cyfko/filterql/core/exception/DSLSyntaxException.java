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
 * @since 1.0
 */
public class DSLSyntaxException extends Exception {

    /**
     * Constructeur avec un message explicatif de l'erreur.
     *
     * @param message le message décrivant la cause de l'exception
     */
    public DSLSyntaxException(String message) {
        super(message);
    }

    /**
     * Constructeur avec un message explicatif et une cause sous-jacente.
     *
     * @param message le message décrivant la cause de l'exception
     * @param cause   la cause originelle de cette exception
     */
    public DSLSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}


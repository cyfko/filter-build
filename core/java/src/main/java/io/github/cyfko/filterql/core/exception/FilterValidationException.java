package io.github.cyfko.filterql.core.exception;

/**
 * Exception thrown when a filter condition cannot be validated or constructed.
 * <p>
 * This exception is used to explicitly indicate errors related to filter validation,
 * making it easier to handle specific errors in the filtering pipeline.
 * </p>
 *
 * <p>It extends {@link Exception} and must be handled or propagated by upstream calls.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * if (!filter.isValid()) {
 *     throw new FilterValidationException("The filter contains invalid values");
 * }
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterValidationException extends Exception {

    /**
     * Crée une exception avec un message explicatif.
     *
     * @param message la description de la cause de l'exception
     */
    public FilterValidationException(String message) {
        super(message);
    }

    /**
     * Crée une exception avec un message explicatif et une cause sous-jacente.
     *
     * @param message la description de la cause de l'exception
     * @param cause   la cause originelle de l'exception
     */
    public FilterValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


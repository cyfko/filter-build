package io.github.cyfko.filterql.core.exception;

/**
 * Exception thrown when a filter condition cannot be validated or constructed.
 * <p>
 * This exception is used to explicitly indicate errors related to filter validation,
 * making it easier to handle specific errors in the filtering pipeline.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * if (!filter.isValid()) {
 *     throw new FilterValidationException("The filter contains invalid values");
 * }
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 2.0.0
 */
public class FilterValidationException extends RuntimeException {

    /**
     * Creates an exception with an explanatory message.
     *
     * @param message the description of the cause of the exception
     */
    public FilterValidationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with an explanatory message and an underlying cause.
     *
     * @param message the description of the cause of the exception
     * @param cause   the original cause of the exception
     */
    public FilterValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


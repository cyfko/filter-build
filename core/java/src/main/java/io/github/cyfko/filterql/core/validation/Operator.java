package io.github.cyfko.filterql.core.validation;

/**
 * Enumeration of supported filter operators.
 * <p>
 * Each operator defines its own symbol, code, as well as its validation rules
 * and the types of operations it supports.
 * </p>
 *
 * <p>This enum is used to represent, in a type-safe way, logical or comparison operators
 * in a dynamic filtering system.</p>
 *
 * <pre>{@code
 * Operator op = Operator.fromString("=");
 * if (op != null && op.requiresValue()) {
 *     // handle operators that require a value
 * }
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public enum Operator {

    /** Equality: "=" */
    EQUALS("=", "EQ"),

    /** Not equal: "!=" */
    NOT_EQUALS("!=", "NE"),

    /** Greater than: "&gt;" */
    GREATER_THAN(">", "GT"),

    /** Greater than or equal: "&gt;=" */
    GREATER_THAN_OR_EQUAL(">=", "GTE"),

    /** Less than: "&lt;" */
    LESS_THAN("<", "LT"),

    /** Less than or equal: "&lt;=" */
    LESS_THAN_OR_EQUAL("<=", "LTE"),

    /** Like (pattern): "LIKE" */
    LIKE("LIKE", "LIKE"),

    /** Not like: "NOT LIKE" */
    NOT_LIKE("NOT LIKE", "NOT_LIKE"),

    /** Included in collection: "IN" */
    IN("IN", "IN"),

    /** Not included in collection: "NOT IN" */
    NOT_IN("NOT IN", "NOT_IN"),

    /** Is null: "IS NULL" */
    IS_NULL("IS NULL", "IS_NULL"),

    /** Is not null: "IS NOT NULL" */
    IS_NOT_NULL("IS NOT NULL", "IS_NOT_NULL"),

    /** Between two values: "BETWEEN" */
    BETWEEN("BETWEEN", "BETWEEN"),

    /** Not between two values: "NOT BETWEEN" */
    NOT_BETWEEN("NOT BETWEEN", "NOT_BETWEEN");

    private final String symbol;
    private final String code;

    Operator(String symbol, String code) {
        this.symbol = symbol;
        this.code = code;
    }

    /**
     * Returns the textual symbol of the operator.
     *
     * @return the symbol (e.g., "=", "LIKE")
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the short code used as the operator's identifier.
     *
     * @return the short code (e.g., "EQ", "LIKE")
     */
    public String getCode() {
        return code;
    }

    /**
     * Finds an operator by its symbol or code.
     *
     * @param value symbol or code to search for (case-insensitive)
     * @return the corresponding operator, or {@code null} if not found
     */
    public static Operator fromString(String value) {
        if (value == null) return null;

        String trimmed = value.trim().toUpperCase();

        for (Operator op : values()) {
            if (op.symbol.equalsIgnoreCase(trimmed) || op.code.equalsIgnoreCase(trimmed)) {
                return op;
            }
        }

        return null;
    }

    /**
     * Indicates whether the operator strictly requires an input value.
     *
     * @return {@code true} if the operator takes a value (e.g., =, >, IN),
     *         {@code false} if it is a predicate without a value (e.g., IS NULL)
     */
    public boolean requiresValue() {
        return this != IS_NULL && this != IS_NOT_NULL;
    }

    /**
     * Indicates whether the operator supports multiple values (collection or range).
     *
     * @return {@code true} for IN, NOT_IN, BETWEEN, NOT_BETWEEN; {@code false} otherwise
     */
    public boolean supportsMultipleValues() {
        return this == IN || this == NOT_IN || this == BETWEEN || this == NOT_BETWEEN;
    }
}


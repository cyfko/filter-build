package io.github.cyfko.filterql.core.validation;

/**
 * Enumeration of supported filter operators.
 * <p>
 * Each operator defines its own symbol, code, and the types of operations it supports.
 * This enum follows naming conventions similar to those used in web standards,
 * HTML, and DOM APIs, emphasizing short, descriptive codes.
 * </p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * // Parse operator from symbol or code string
 * Op op = Op.fromString("=");
 * if (op != null && op.requiresValue()) {
 *     // handle operators that require a value
 * }
 *
 * // Check if operator supports multiple values (like IN or RANGE)
 * if (op.supportsMultipleValues()) {
 *     // handle collection or range values
 * }
 * 
 * // Example with filter definitions
 * FilterDefinition<UserPropertyRef> equalFilter = 
 *     new FilterDefinition<>(UserPropertyRef.NAME, Op.EQ, "John");
 * FilterDefinition<UserPropertyRef> likeFilter = 
 *     new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%");
 * FilterDefinition<UserPropertyRef> rangeFilter = 
 *     new FilterDefinition<>(UserPropertyRef.AGE, Op.RANGE, List.of(18, 65));
 * FilterDefinition<UserPropertyRef> nullCheckFilter = 
 *     new FilterDefinition<>(UserPropertyRef.EMAIL, Op.IS_NULL, null);
 * }</pre>
 *
 * <p><strong>Operator Categories and Examples:</strong></p>
 * 
 * <p><em>Comparison Operators:</em></p>
 * <pre>{@code
 * // Equality and inequality
 * user.age == 25          -> Op.EQ
 * user.status != ACTIVE   -> Op.NE
 * 
 * // Numerical comparisons  
 * user.age > 18           -> Op.GT
 * user.age >= 21          -> Op.GTE
 * user.salary < 50000     -> Op.LT
 * user.experience <= 5    -> Op.LTE
 * }</pre>
 * 
 * <p><em>Text Matching:</em></p>
 * <pre>{@code
 * // Pattern matching (SQL LIKE)
 * user.name LIKE 'John%'     -> Op.MATCHES
 * user.email NOT LIKE '%test%' -> Op.NOT_MATCHES
 * }</pre>
 * 
 * <p><em>Collection Operations:</em></p>
 * <pre>{@code
 * // Set membership
 * user.status IN ('ACTIVE', 'PENDING')     -> Op.IN
 * user.role NOT IN ('ADMIN', 'MODERATOR')  -> Op.NOT_IN
 * }</pre>
 * 
 * <p><em>Null Checks:</em></p>
 * <pre>{@code
 * // Null value checks (no value parameter needed)
 * user.deletedAt IS NULL      -> Op.IS_NULL
 * user.email IS NOT NULL      -> Op.NOT_NULL
 * }</pre>
 * 
 * <p><em>Range Operations:</em></p>
 * <pre>{@code
 * // Range checks (requires exactly 2 values)
 * user.age BETWEEN 18 AND 65     -> Op.RANGE
 * user.salary NOT BETWEEN 30000 AND 50000 -> Op.NOT_RANGE
 * }</pre>
 *
 * <p><strong>HTML-style operator mappings:</strong></p>
 * <ul>
 *     <li>EQ / =</li>
 *     <li>NE / !=</li>
 *     <li>GT / &gt;</li>
 *     <li>GTE / &gt;=</li>
 *     <li>LT / &lt;</li>
 *     <li>LTE / &lt;=</li>
 *     <li>MATCHES / LIKE</li>
 *     <li>NOT_MATCHES / NOT LIKE</li>
 *     <li>IN / IN</li>
 *     <li>NOT_IN / NOT IN</li>
 *     <li>IS_NULL / IS NULL</li>
 *     <li>NOT_NULL / IS NOT NULL</li>
 *     <li>RANGE / BETWEEN</li>
 *     <li>NOT_RANGE / NOT BETWEEN</li>
 * </ul>
 *
 * @author Frank KOSSI
 * @since 2.0.0
 */
public enum Op {

    /** Equality operator: "=" */
    EQ("=", "EQ"),

    /** Not equal operator: "!=" */
    NE("!=", "NE"),

    /** Greater than operator: ">" */
    GT(">", "GT"),

    /** Greater than or equal operator: ">=" */
    GTE(">=", "GTE"),

    /** Less than operator: "<" */
    LT("<", "LT"),

    /** Less than or equal operator: "<=" */
    LTE("<=", "LTE"),

    /** Pattern matching operator: "LIKE" */
    MATCHES("LIKE", "MATCHES"),

    /** Negated pattern matching operator: "NOT LIKE" */
    NOT_MATCHES("NOT LIKE", "NOT_MATCHES"),

    /** Inclusion operator for collections: "IN" */
    IN("IN", "IN"),

    /** Negated inclusion operator: "NOT IN" */
    NOT_IN("NOT IN", "NOT_IN"),

    /** Operator for checking NULL: "IS NULL" */
    IS_NULL("IS NULL", "IS_NULL"),

    /** Operator for checking NOT NULL: "IS NOT NULL" */
    NOT_NULL("IS NOT NULL", "NOT_NULL"),

    /** Range operator: "BETWEEN" */
    RANGE("BETWEEN", "RANGE"),

    /** Negated range operator: "NOT BETWEEN" */
    NOT_RANGE("NOT BETWEEN", "NOT_RANGE");

    private final String symbol;
    private final String code;

    Op(String symbol, String code) {
        this.symbol = symbol;
        this.code = code;
    }

    /**
     * Returns the textual symbol of the operator, e.g. "=", "LIKE".
     *
     * @return symbol string
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the short code used as the operator's identifier, e.g. "EQ", "MATCHES".
     *
     * @return code string
     */
    public String getCode() {
        return code;
    }

    /**
     * Finds an {@code Op} by its symbol or code, ignoring case.
     *
     * @param value symbol or code string to search for
     * @return matching {@code Op}, or {@code null} if none found
     */
    public static Op fromString(String value) {
        if (value == null) return null;

        String trimmed = value.trim().toUpperCase();

        for (Op op : values()) {
            if (op.symbol.equalsIgnoreCase(trimmed) || op.code.equalsIgnoreCase(trimmed)) {
                return op;
            }
        }

        return null;
    }

    /**
     * Checks whether the operator requires an input value.
     * <p>
     * Operators like {@link #IS_NULL} and {@link #NOT_NULL} do not take values.
     *
     * @return {@code true} if operator requires a value, {@code false} otherwise
     */
    public boolean requiresValue() {
        return this != IS_NULL && this != NOT_NULL;
    }

    /**
     * Checks whether the operator supports multiple values, e.g. for collections or ranges.
     * Supported operators include {@link #IN}, {@link #NOT_IN}, {@link #RANGE}, and {@link #NOT_RANGE}.
     *
     * @return {@code true} if operator supports multiple values, otherwise {@code false}
     */
    public boolean supportsMultipleValues() {
        return this == IN || this == NOT_IN || this == RANGE || this == NOT_RANGE;
    }
}


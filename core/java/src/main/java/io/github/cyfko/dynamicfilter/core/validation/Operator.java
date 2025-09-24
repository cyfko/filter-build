package io.github.cyfko.dynamicfilter.core.validation;

/**
 * Enumeration of supported filter operators.
 * Each operator defines its validation rules and supported types.
 */
public enum Operator {
    
    EQUALS("=", "EQ"),
    NOT_EQUALS("!=", "NE"),
    GREATER_THAN(">", "GT"),
    GREATER_THAN_OR_EQUAL(">=", "GTE"),
    LESS_THAN("<", "LT"),
    LESS_THAN_OR_EQUAL("<=", "LTE"),
    LIKE("LIKE", "LIKE"),
    NOT_LIKE("NOT LIKE", "NOT_LIKE"),
    IN("IN", "IN"),
    NOT_IN("NOT IN", "NOT_IN"),
    IS_NULL("IS NULL", "IS_NULL"),
    IS_NOT_NULL("IS NOT NULL", "IS_NOT_NULL"),
    BETWEEN("BETWEEN", "BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN", "NOT_BETWEEN");
    
    private final String symbol;
    private final String code;
    
    Operator(String symbol, String code) {
        this.symbol = symbol;
        this.code = code;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Finds an operator by its symbol or code.
     * 
     * @param value The symbol or code to search for
     * @return The matching operator, or null if not found
     */
    public static Operator fromString(String value) {
        if (value == null) return null;
        
        String trimmed = value.trim().toUpperCase();
        
        for (Operator op : values()) {
            if (op.symbol.equalsIgnoreCase(value) || 
                op.code.equalsIgnoreCase(trimmed)) {
                return op;
            }
        }
        
        return null;
    }
    
    /**
     * Checks if this operator requires a value.
     * 
     * @return true if the operator requires a value, false otherwise
     */
    public boolean requiresValue() {
        return this != IS_NULL && this != IS_NOT_NULL;
    }
    
    /**
     * Checks if this operator supports multiple values.
     * 
     * @return true if the operator supports multiple values, false otherwise
     */
    public boolean supportsMultipleValues() {
        return this == IN || this == NOT_IN || this == BETWEEN || this == NOT_BETWEEN;
    }
}

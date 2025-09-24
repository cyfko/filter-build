package io.github.cyfko.dynamicfilter.core.validation;

import java.util.Set;

/**
 * Base enum for property references.
 * Developers should create their own enums extending this enum to define
 * the properties available for their entities.
 * 
 * PropertyRef contains only the logical property definition (type, operators).
 * Each adapter is responsible for interpreting PropertyRef and creating
 * appropriate conditions. This allows maximum flexibility - a single PropertyRef
 * can map to multiple entity fields or complex conditions.
 * 
 * Example usage:
 * <pre>
 * public enum UserPropertyRef extends PropertyRef {
 *     USER_NAME(String.class, Set.of(LIKE, EQ, IN)),
 *     USER_AGE(Integer.class, Set.of(EQ, GT, GTE, LT, LTE, BETWEEN)),
 *     USER_STATUS(String.class, Set.of(EQ, NE, IN));
 *     
 *     UserPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
 *         super(type, supportedOperators);
 *     }
 * }
 * </pre>
 */
public enum PropertyRef {
    
    // This enum serves as a base class and should not be instantiated directly
    // Developers will create their own enums extending this one
    
    // Base enum constant (not used directly)
    BASE(Object.class, Set.of());
    
    private final Class<?> type;
    private final Set<Operator> supportedOperators;
    
    PropertyRef(Class<?> type, Set<Operator> supportedOperators) {
        this.type = type;
        this.supportedOperators = supportedOperators;
    }
    
    /**
     * Gets the type of this property.
     * 
     * @return The property type
     */
    public Class<?> getType() {
        return type;
    }
    
    /**
     * Checks if this property supports the given operator.
     * 
     * @param operator The operator to check
     * @return true if the operator is supported, false otherwise
     */
    public boolean supportsOperator(Operator operator) {
        return supportedOperators.contains(operator);
    }
    
    /**
     * Gets all supported operators for this property.
     * 
     * @return A set of supported operators
     */
    public Set<Operator> getSupportedOperators() {
        return Set.copyOf(supportedOperators);
    }
    
    /**
     * Validates that the given operator is supported by this property.
     * 
     * @param operator The operator to validate
     * @throws IllegalArgumentException if the operator is not supported
     */
    public void validateOperator(Operator operator) {
        if (!supportsOperator(operator)) {
            throw new IllegalArgumentException(
                String.format("Operator '%s' is not supported for property '%s'. Supported operators: %s", 
                            operator, this, getSupportedOperators()));
        }
    }
    
    /**
     * Gets a human-readable description of this property reference.
     * 
     * @return A description string
     */
    public String getDescription() {
        return String.format("%s (%s)", this, getType().getSimpleName());
    }
    
    @Override
    public String toString() {
        return String.format("PropertyRef{type=%s, supportedOperators=%s}", 
                           getType().getSimpleName(), getSupportedOperators());
    }
}

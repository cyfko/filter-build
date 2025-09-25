package io.github.cyfko.dynamicfilter.core.validation;

import java.util.Set;

/**
 * Interface for property references in dynamic filtering.
 * 
 * Developers should create their own enums implementing this interface to define
 * the properties available for their entities.
 * 
 * PropertyRef contains only the logical property definition (type, operators).
 * Each adapter is responsible for interpreting PropertyRef and creating
 * appropriate conditions. This allows maximum flexibility - a single PropertyRef
 * can map to multiple entity fields or complex conditions.
 * 
 * Example usage:
 * <pre>
 * public enum UserPropertyRef implements PropertyRef {
 *     USER_NAME(String.class, Set.of(LIKE, EQ, IN)),
 *     USER_AGE(Integer.class, Set.of(EQ, GT, GTE, LT, LTE, BETWEEN)),
 *     USER_STATUS(String.class, Set.of(EQ, NE, IN));
 *     
 *     private final Class<?> type;
 *     private final Set<Operator> supportedOperators;
 *     
 *     UserPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
 *         this.type = type;
 *         this.supportedOperators = Set.copyOf(supportedOperators);
 *     }
 *     
 *     @Override
 *     public Class<?> getType() { return type; }
 *     
 *     @Override
 *     public Set<Operator> getSupportedOperators() { return supportedOperators; }
 * }
 * </pre>
 */
public interface PropertyRef {
    
    /**
     * Gets the type of this property.
     * 
     * @return The property type
     */
    Class<?> getType();
    
    /**
     * Gets the set of operators supported by this property.
     * The returned set should be immutable.
     * 
     * @return An immutable set of supported operators
     */
    Set<Operator> getSupportedOperators();
    
    /**
     * Checks if this property supports the given operator.
     * 
     * @param operator The operator to check
     * @return true if the operator is supported, false otherwise
     */
    default boolean supportsOperator(Operator operator) {
        return getSupportedOperators().contains(operator);
    }
    
    /**
     * Validates that the given operator is supported by this property.
     * 
     * @param operator The operator to validate
     * @throws IllegalArgumentException if the operator is not supported
     * @throws NullPointerException if the operator is null
     */
    default void validateOperator(Operator operator) {
        if (operator == null) {
            throw new NullPointerException("Operator cannot be null");
        }
        
        if (!supportsOperator(operator)) {
            throw new IllegalArgumentException(
                String.format("Operator %s is not supported for property %s. Supported operators: %s",
                            operator, this, getSupportedOperators()));
        }
    }
}
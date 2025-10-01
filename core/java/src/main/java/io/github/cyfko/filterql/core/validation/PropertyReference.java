package io.github.cyfko.filterql.core.validation;

import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.utils.ClassUtils;

import java.util.Collection;
import java.util.Set;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface representing a property reference in a dynamic filtering context.
 * <p>
 * Developers must create their own enums implementing this interface to define
 * the accessible properties and their characteristics on their entities.
 * </p>
 * <p>
 * {@code PropertyReference} only contains the logical definition of the property (type, supported operators).
 * Each adapter is responsible for interpreting this interface and building the appropriate conditions.
 * This distinction offers great flexibility, allowing for example a single property reference
 * to correspond to multiple fields or complex conditions.
 * </p>
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * public enum UserPropertyRef implements PropertyReference {
 *     USER_NAME(String.class, Set.of(LIKE, EQUALS, IN)),
 *     USER_AGE(Integer.class, Set.of(EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, BETWEEN)),
 *     USER_STATUS(UserStatus.class, Set.of(EQUALS, NOT_EQUALS, IN));
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
 * }
 * </pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public interface PropertyReference {

    /**
     * Returns the Java type of the represented property.
     *
     * @return the Java class representing the property type (e.g., String.class, Integer.class)
     */
    Class<?> getType();

    /**
     * Returns the unmodifiable collection of operators supported by this property.
     *
     * @return an immutable {@link Set} of operators supported by the property
     */
    Set<Op> getSupportedOperators();

    /**
     * Indicates whether the given operator is supported for this property.
     *
     * @param operator the operator to test, not null
     * @return {@code true} if the operator is supported, {@code false} otherwise
     * @throws NullPointerException if {@code operator} is {@code null}
     */
    default boolean supportsOperator(Op operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        return getSupportedOperators().contains(operator);
    }

    /**
     * Validates that the given operator is supported by this property.
     * Throws an {@link FilterValidationException} if not.
     *
     * @param operator the operator to validate, not null
     * @throws FilterValidationException if the operator is not supported
     * @throws NullPointerException     if {@code operator} is {@code null}
     */
    default void validateOperator(Op operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        if (!supportsOperator(operator)) {
            throw new FilterValidationException(
                    String.format("Operator %s is not supported for property %s. Supported operators: %s",
                            operator, this, getSupportedOperators()));
        }
    }

    /**
     * Validates that the given operator is applicable for the provided value,
     * based on the expected property type and the nature of the value.
     * <p>
     * For simple operators (comparisons, like), the value must be assignable to the property type.
     * For null-check operators (IS_NULL, IS_NOT_NULL), no value is required.
     * For operators involving collections or ranges (IN, BETWEEN), the value
     * must be a collection compatible with the expected type.
     * </p>
     *
     * @param operator the operator to validate, not null
     * @param value    the value to which the operator applies, may be null for some operators
     * @throws FilterValidationException if the operator is not applicable for the given value
     * @throws NullPointerException     if {@code operator} is {@code null}
     */
    default void validateOperatorForValue(Op operator, Object value) {
        Objects.requireNonNull(operator, "Operator cannot be null");

        // Validation de l'opérateur pour cette propriété
        validateOperator(operator);

        // Validation spécifique selon le type d'opérateur
        ValidationResult result = validateValueForOperator(operator, value);
        if (!result.isValid()) {
            throw new FilterValidationException(result.getErrorMessage());
        }
    }

    /**
     * Validates the compatibility between an operator and a value.
     * Internal method for fine-grained and extensible validation.
     *
     * @param operator the operator to validate
     * @param value the value to validate
     * @return the validation result
     */
    private ValidationResult validateValueForOperator(Op operator, Object value) {
        return switch (operator) {
            case EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, LIKE,
                 NOT_LIKE -> validateSingleValue(operator, value);
            case IS_NULL, IS_NOT_NULL -> validateNullCheck(operator, value);
            case IN, NOT_IN -> validateCollectionValue(operator, value, false);
            case BETWEEN, NOT_BETWEEN -> validateCollectionValue(operator, value, true);
        };
    }

    /**
     * Validates a single value for comparison operators.
     */
    private ValidationResult validateSingleValue(Op operator, Object value) {
        if (value == null) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a non-null value", operator)
            );
        }

        if (!isCompatibleType(value.getClass(), getType())) {
            return ValidationResult.failure(
                    String.format("Value of type %s is not compatible with property type %s for operator %s",
                            value.getClass().getSimpleName(),
                            getType().getSimpleName(),
                            operator)
            );
        }

        return ValidationResult.success();
    }

    /**
     * Validates null-check operators.
     */
    private ValidationResult validateNullCheck(Op operator, Object value) {
        // Pour IS_NULL et IS_NOT_NULL, la valeur devrait être null ou absente
        // mais on peut être tolérant et accepter toute valeur
        return ValidationResult.success();
    }

    /**
     * Validates a collection value for IN and BETWEEN operators.
     */
    private ValidationResult validateCollectionValue(Op operator, Object value, boolean requiresExactlyTwo) {
        if (value == null) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a non-null collection value", operator)
            );
        }

        if (!(value instanceof Collection<?>)) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a Collection value, got %s",
                            operator, value.getClass().getSimpleName())
            );
        }

        Collection<?> collection = (Collection<?>) value;

        if (collection.isEmpty()) {
            return ValidationResult.failure(
                    String.format("Operator %s requires a non-empty collection", operator)
            );
        }

        if (requiresExactlyTwo && collection.size() != 2) {
            return ValidationResult.failure(
                    String.format("Operator %s requires exactly 2 values for range, got %d",
                            operator, collection.size())
            );
        }

        // Vérifier la compatibilité des types des éléments
        if (!ClassUtils.allCompatible(getType(), collection)) {
            return ValidationResult.failure(
                    String.format("Collection elements are not compatible with property type %s for operator %s",
                            getType().getSimpleName(), operator)
            );
        }

        return ValidationResult.success();
    }

    /**
     * Checks compatibility between two types.
     * Handles primitive types and their wrappers.
     */
    private boolean isCompatibleType(Class<?> valueType, Class<?> expectedType) {
        // Assignabilité directe
        if (expectedType.isAssignableFrom(valueType)) {
            return true;
        }

        // Gestion des primitives et leurs wrappers
        return isPrimitiveCompatible(valueType, expectedType);
    }

    /**
     * Checks compatibility between primitive types and their wrappers.
     */
    private boolean isPrimitiveCompatible(Class<?> valueType, Class<?> expectedType) {
        Map<Class<?>, Class<?>> primitiveToWrapper = Map.of(
                boolean.class, Boolean.class,
                byte.class, Byte.class,
                char.class, Character.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                float.class, Float.class,
                double.class, Double.class
        );

        // Primitive -> Wrapper
        if (primitiveToWrapper.get(expectedType) == valueType) {
            return true;
        }

        // Wrapper -> Primitive
        return primitiveToWrapper.get(valueType) == expectedType;
    }

    /**
     * Checks if this property supports a set of operators.
     * Useful for validating configurations or complex queries.
     *
     * @param operators collection of operators to check, not null
     * @return true if all operators are supported
     * @throws NullPointerException if operators is null
     */
    default boolean supportsAllOperators(Collection<Op> operators) {
        Objects.requireNonNull(operators, "Operators collection cannot be null");
        return getSupportedOperators().containsAll(operators);
    }

    /**
     * Returns the unsupported operators among those provided.
     * Useful for debugging and detailed error messages.
     *
     * @param operators collection of operators to check, not null
     * @return set of unsupported operators (may be empty)
     * @throws NullPointerException if operators is null
     */
    default Set<Op> getUnsupportedOperators(Collection<Op> operators) {
        Objects.requireNonNull(operators, "Operators collection cannot be null");
        return operators.stream()
                .filter(op -> !supportsOperator(op))
                .collect(Collectors.toSet());
    }

    /**
     * Checks if this property is of numeric type.
     * Useful for determining which comparison operators to apply.
     *
     * @return true if the type is a number
     */
    default boolean isNumeric() {
        Class<?> type = getType();
        return Number.class.isAssignableFrom(type) ||
                isPrimitiveNumber(type);
    }

    /**
     * Checks if the type is a numeric primitive.
     */
    private boolean isPrimitiveNumber(Class<?> type) {
        return type == byte.class || type == short.class ||
                type == int.class || type == long.class ||
                type == float.class || type == double.class;
    }

    /**
     * Checks if this property is of textual type.
     * Useful for determining if LIKE operators are applicable.
     *
     * @return true if the type is String or CharSequence
     */
    default boolean isTextual() {
        return CharSequence.class.isAssignableFrom(getType());
    }

    /**
     * Class representing the result of a validation operation.
     * <p>
     * The result can indicate either a successful validation or a failure with an associated error message.
     * This class is used to simply and clearly convey the validation state.
     * </p>
     *
     * <p>Instances are immutable and created via the static methods
     * {@link #success()} and {@link #failure(String)}.</p>
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * ValidationResult result = validateInput(value);
     * if (!result.isValid()) {
     *     System.out.println("Validation error: " + result.getErrorMessage());
     * }
     * }</pre>
     */
    static class ValidationResult {

        private final boolean valid;
        private final String errorMessage;

    /**
     * Private constructor - use via the static creation methods.
     *
     * @param valid        true if validation succeeded, false otherwise
     * @param errorMessage error message in case of failure, or null if valid
     */
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

    /**
     * Creates an instance indicating a successful validation.
     *
     * @return a valid result with no error message
     */
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

    /**
     * Creates an instance indicating a failed validation with an error message.
     *
     * @param errorMessage message explaining the reason for failure
     * @return an invalid result containing the provided error message
     */
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

    /**
     * Indicates whether the validation succeeded.
     *
     * @return true if valid, false otherwise
     */
        public boolean isValid() {
            return valid;
        }

    /**
     * Returns the error message associated with a failed validation.
     *
     * @return error message if invalid, or null if valid
     */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
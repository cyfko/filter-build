package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;

import java.util.Set;

/**
 * Enum PropertyRef pour l'entité de test.
 * Cet enum définit les propriétés disponibles pour le filtrage.
 */
public enum UserPropertyRef implements PropertyReference {
    NAME(String.class, Set.of(
        Op.EQUALS, Op.NOT_EQUALS,
        Op.LIKE, Op.NOT_LIKE,
        Op.IN, Op.NOT_IN,
        Op.IS_NULL, Op.IS_NOT_NULL
    )),
    AGE(Integer.class, Set.of(
        Op.EQUALS, Op.NOT_EQUALS,
        Op.GREATER_THAN, Op.GREATER_THAN_OR_EQUAL,
        Op.LESS_THAN, Op.LESS_THAN_OR_EQUAL,
        Op.IN, Op.NOT_IN,
        Op.IS_NULL, Op.IS_NOT_NULL,
        Op.BETWEEN, Op.NOT_BETWEEN
    )),
    EMAIL(String.class, Set.of(
        Op.EQUALS, Op.NOT_EQUALS,
        Op.LIKE, Op.NOT_LIKE,
        Op.IN, Op.NOT_IN,
        Op.IS_NULL, Op.IS_NOT_NULL
    )),
    ACTIVE(Boolean.class, Set.of(
        Op.EQUALS, Op.NOT_EQUALS,
        Op.IS_NULL, Op.IS_NOT_NULL
    )),
    CREATED_AT(java.time.LocalDateTime.class, Set.of(
        Op.EQUALS, Op.NOT_EQUALS,
        Op.GREATER_THAN, Op.GREATER_THAN_OR_EQUAL,
        Op.LESS_THAN, Op.LESS_THAN_OR_EQUAL,
        Op.IS_NULL, Op.IS_NOT_NULL,
        Op.BETWEEN, Op.NOT_BETWEEN
    ));

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    UserPropertyRef(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = supportedOperators;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Set<Op> getSupportedOperators() {
        return supportedOperators;
    }

    @Override
    public void validateOperator(Op operator) {
        if (!supportedOperators.contains(operator)) {
            throw new IllegalArgumentException("Op " + operator + " not supported for " + this);
        }
    }

    @Override
    public void validateOperatorForValue(Op operator, Object value) {
        validateOperator(operator);
        
        // Validation spécifique pour certains opérateurs
        switch (operator) {
            case IN:
            case NOT_IN:
                if (value == null || !(value instanceof java.util.Collection) || ((java.util.Collection<?>) value).isEmpty()) {
                    throw new IllegalArgumentException("IN/NOT_IN operator requires a non-empty collection");
                }
                break;
            case BETWEEN:
            case NOT_BETWEEN:
                if (value == null || !(value instanceof java.util.Collection)) {
                    throw new IllegalArgumentException("BETWEEN operator requires a collection");
                }
                java.util.Collection<?> betweenValues = (java.util.Collection<?>) value;
                if (betweenValues.size() != 2) {
                    throw new IllegalArgumentException("BETWEEN operator requires exactly 2 values");
                }
                break;
            default:
                // Validation basique du type
                if (value != null && !type.isAssignableFrom(value.getClass())) {
                    throw new IllegalArgumentException("Value type " + value.getClass() + " not compatible with " + type);
                }
                break;
        }
    }
}

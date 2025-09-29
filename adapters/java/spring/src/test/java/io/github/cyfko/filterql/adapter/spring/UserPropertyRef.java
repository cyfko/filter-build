package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.mappings.PathMapping;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

import java.util.Set;

/**
 * Enum PropertyRef pour l'entité de test.
 * Cet enum définit les propriétés disponibles pour le filtrage.
 */
public enum UserPropertyRef implements PropertyRef, PathMapping<User> {
    NAME("name", String.class, Set.of(
        Operator.EQUALS, Operator.NOT_EQUALS,
        Operator.LIKE, Operator.NOT_LIKE,
        Operator.IN, Operator.NOT_IN,
        Operator.IS_NULL, Operator.IS_NOT_NULL
    )),
    AGE("age", Integer.class, Set.of(
        Operator.EQUALS, Operator.NOT_EQUALS,
        Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
        Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL,
        Operator.IN, Operator.NOT_IN,
        Operator.IS_NULL, Operator.IS_NOT_NULL,
        Operator.BETWEEN, Operator.NOT_BETWEEN
    )),
    EMAIL("email", String.class, Set.of(
        Operator.EQUALS, Operator.NOT_EQUALS,
        Operator.LIKE, Operator.NOT_LIKE,
        Operator.IN, Operator.NOT_IN,
        Operator.IS_NULL, Operator.IS_NOT_NULL
    )),
    ACTIVE("active", Boolean.class, Set.of(
        Operator.EQUALS, Operator.NOT_EQUALS,
        Operator.IS_NULL, Operator.IS_NOT_NULL
    )),
    CREATED_AT("createdAt", java.time.LocalDateTime.class, Set.of(
        Operator.EQUALS, Operator.NOT_EQUALS,
        Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
        Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL,
        Operator.IS_NULL, Operator.IS_NOT_NULL,
        Operator.BETWEEN, Operator.NOT_BETWEEN
    ));

    private final String path;
    private final Class<?> type;
    private final Set<Operator> supportedOperators;

    UserPropertyRef(String path, Class<?> type, Set<Operator> supportedOperators) {
        this.path = path;
        this.type = type;
        this.supportedOperators = supportedOperators;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Set<Operator> getSupportedOperators() {
        return supportedOperators;
    }

    @Override
    public void validateOperator(Operator operator) {
        if (!supportedOperators.contains(operator)) {
            throw new IllegalArgumentException("Operator " + operator + " not supported for " + this);
        }
    }

    @Override
    public void validateOperatorForValue(Operator operator, Object value) {
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

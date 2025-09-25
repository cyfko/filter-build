package io.github.cyfko.dynamicfilter.core.validation;

import java.util.Set;

/**
 * Test enum that implements PropertyRef for testing purposes.
 */
public enum DefinedPropertyRef implements PropertyRef {
    USER_NAME(String.class, Set.of(Operator.EQUALS, Operator.LIKE, Operator.IN)),
    USER_AGE(Integer.class, Set.of(Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN, Operator.BETWEEN)),
    USER_EMAIL(String.class, Set.of(Operator.EQUALS, Operator.LIKE)),
    USER_STATUS(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN));

    private final Class<?> type;
    private final Set<Operator> supportedOperators;

    DefinedPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    public Class<?> getType() {
        return type;
    }

    public Set<Operator> getSupportedOperators() {
        return supportedOperators;
    }

    public boolean supportsOperator(Operator operator) {
        return supportedOperators.contains(operator);
    }

    public void validateOperator(Operator operator) {
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

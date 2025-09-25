package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

import java.util.Set;

/**
 * PropertyRef enum for UserEntity testing.
 */
public enum UserPropertyRef implements PropertyRef {
    USER_NAME(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IN, Operator.NOT_IN, Operator.IS_NULL, Operator.IS_NOT_NULL)),
    USER_AGE(Integer.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN, 
                                  Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN_OR_EQUAL, Operator.IS_NULL, Operator.IS_NOT_NULL)),
    USER_EMAIL(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE, Operator.IS_NULL, Operator.IS_NOT_NULL)),
    USER_STATUS(String.class, Set.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN, Operator.NOT_IN, Operator.IS_NULL, Operator.IS_NOT_NULL));
    
    private final Class<?> type;
    private final Set<Operator> supportedOperators;
    
    UserPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
        this.type = type;
        this.supportedOperators = supportedOperators;
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
    public boolean supportsOperator(Operator operator) {
        return supportedOperators.contains(operator);
    }
    
    @Override
    public void validateOperator(Operator operator) {
        if (!supportsOperator(operator)) {
            throw new IllegalArgumentException(
                "Operator '" + operator + "' is not supported for property " + this.name()
            );
        }
    }
}

package io.github.cyfko.filterql.core.validation;

import java.util.Set;

/**
 * Test enum that implements PropertyRef for testing purposes.
 */
public enum DefinedPropertyReference implements PropertyReference {
    USER_NAME(String.class, Set.of(Op.EQUALS, Op.LIKE, Op.IN)),
    USER_AGE(Integer.class, Set.of(Op.EQUALS, Op.GREATER_THAN, Op.LESS_THAN, Op.BETWEEN)),
    USER_EMAIL(String.class, Set.of(Op.EQUALS, Op.LIKE)),
    USER_STATUS(String.class, Set.of(Op.EQUALS, Op.NOT_EQUALS, Op.IN));

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    DefinedPropertyReference(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    public Class<?> getType() {
        return type;
    }

    public Set<Op> getSupportedOperators() {
        return supportedOperators;
    }
}

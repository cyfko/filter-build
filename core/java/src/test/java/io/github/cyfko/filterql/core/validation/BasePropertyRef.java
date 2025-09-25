package io.github.cyfko.filterql.core.validation;

import java.util.Set;

/**
 * Base PropertyRef enum for testing purposes.
 * This enum has minimal functionality (Object type, no operators).
 */
public enum BasePropertyRef implements PropertyRef {
    BASE(Object.class, Set.of());

    private final Class<?> type;
    private final Set<Operator> supportedOperators;

    BasePropertyRef(Class<?> type, Set<Operator> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Set<Operator> getSupportedOperators() {
        return supportedOperators;
    }
}


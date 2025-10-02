package io.github.cyfko.filterql.core.validation;

import java.util.Set;

/**
 * Base PropertyRef enum for testing purposes.
 * This enum has minimal functionality (Object type, no operators).
 */
public enum BasePropertyReference implements PropertyReference {
    BASE(Object.class, Set.of());

    private final Class<?> type;
    private final Set<Op> supportedOperators;

    BasePropertyReference(Class<?> type, Set<Op> supportedOperators) {
        this.type = type;
        this.supportedOperators = Set.copyOf(supportedOperators);
    }

    @Override
    public Class<?> getType() { return type; }

    @Override
    public Set<Op> getSupportedOperators() { return supportedOperators; }
}


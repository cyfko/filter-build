package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;

/**
 * Represents a single filter definition with a property reference,
 * an operator, and a value.
 * <p>
 * The reference property {@code ref} must be an enum implementing {@link PropertyReference}
 * to ensure type safety and performance.
 * </p>
 *
 * @param <P> type of the reference property (enum implementing {@link PropertyReference})
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterDefinition<P extends Enum<P> & PropertyReference> {

    private final P ref;
    private final Op operator;
    private final Object value;

    /**
     * Creates a filter definition with the given property, operator, and value.
     *
     * @param ref       the property reference (enum {@link PropertyReference})
     * @param operator  the comparison or logical operator
     * @param value     the value to use in the filter (may be a collection depending on the operator)
     */
    public FilterDefinition(P ref, Op operator, Object value) {
        this.ref = ref;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Returns the property reference used in this filter.
     *
     * @return the reference property
     */
    public P getRef() {
        return ref;
    }

    /**
     * Returns the operator associated with this filter definition.
     *
     * @return the operator
     */
    public Op getOperator() {
        return operator;
    }

    /**
     * Returns the value applied in this filter.
     * <p>
     * This value may be a simple object or a collection, depending on the operator.
     * </p>
     *
     * @return the filter value
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("FilterDefinition{ref=%s, operator=%s, value=%s}",
                ref, operator, value);
    }
}


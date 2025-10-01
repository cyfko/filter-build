package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;

public class NamedFilter<P extends Enum<P> &PropertyReference> extends FilterDefinition<P> {
    private String name;

    /**
     * Creates a named filter with the given property, operator, and value.
     *
     * @param ref      the property reference (enum {@link PropertyReference})
     * @param operator the comparison or logical operator
     * @param value    the value to use in the filter (may be a collection depending on the operator)
     */
    public NamedFilter(P ref, Op operator, Object value) {
        super(ref, operator, value);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("FilterName{name=%s, ref=%s, operator=%s, value=%s}",
                name,getRef(),getOperator(),getValue());
    }
}

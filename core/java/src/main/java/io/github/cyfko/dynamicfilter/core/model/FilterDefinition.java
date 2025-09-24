package io.github.cyfko.dynamicfilter.core.model;

import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;

/**
 * Represents a single filter definition with property reference, operator, and value.
 * The ref is now type-safe with a specific PropertyRef enum.
 */
public class FilterDefinition<T extends PropertyRef> {
    
    private final T ref;
    private final String operator;
    private final Object value;
    
    public FilterDefinition(T ref, String operator, Object value) {
        this.ref = ref;
        this.operator = operator;
        this.value = value;
    }
    
    public T getRef() {
        return ref;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public Object getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return String.format("FilterDefinition{ref=%s, operator='%s', value=%s}", 
                           ref, operator, value);
    }
}

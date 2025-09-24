package io.github.cyfko.dynamicfilter.core.model;

/**
 * Represents a single filter definition with property reference, operator, and value.
 */
public class FilterDefinition {
    
    private final String ref;
    private final String operator;
    private final Object value;
    
    public FilterDefinition(String ref, String operator, Object value) {
        this.ref = ref;
        this.operator = operator;
        this.value = value;
    }
    
    public String getRef() {
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
        return String.format("FilterDefinition{ref='%s', operator='%s', value=%s}", 
                           ref, operator, value);
    }
}

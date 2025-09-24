package io.github.cyfko.dynamicfilter.core.model;

import io.github.cyfko.dynamicfilter.core.validation.PropertyRef;
import java.util.Map;

/**
 * Represents a complete filter request containing multiple filter definitions
 * and a DSL expression for combining them.
 */
public class FilterRequest<T extends PropertyRef> {
    
    private final Map<String, FilterDefinition<T>> filters;
    private final String combineWith;
    
    public FilterRequest(Map<String, FilterDefinition<T>> filters, String combineWith) {
        this.filters = filters;
        this.combineWith = combineWith;
    }
    
    public Map<String, FilterDefinition<T>> getFilters() {
        return filters;
    }
    
    public String getCombineWith() {
        return combineWith;
    }
    
    @Override
    public String toString() {
        return String.format("FilterRequest{filters=%s, combineWith='%s'}", 
                           filters, combineWith);
    }
}

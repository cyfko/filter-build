package io.github.cyfko.dynamicfilter.core.model;

import java.util.Map;

/**
 * Represents a complete filter request containing multiple filter definitions
 * and a DSL expression for combining them.
 */
public class FilterRequest {
    
    private final Map<String, FilterDefinition> filters;
    private final String combineWith;
    
    public FilterRequest(Map<String, FilterDefinition> filters, String combineWith) {
        this.filters = filters;
        this.combineWith = combineWith;
    }
    
    public Map<String, FilterDefinition> getFilters() {
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

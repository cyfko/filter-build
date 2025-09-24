package io.github.cyfko.dynamicfilter.core;

/**
 * Interface for providing conditions by filter key.
 * The Context acts as a registry that maps filter tokens to their corresponding conditions.
 */
public interface Context {
    
    /**
     * Retrieves the condition associated with the given filter key.
     * 
     * @param filterKey The unique identifier for the filter
     * @return The condition associated with the filter key, or null if not found
     */
    Condition getCondition(String filterKey);
}

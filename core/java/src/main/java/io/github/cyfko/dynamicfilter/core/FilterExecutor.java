package io.github.cyfko.dynamicfilter.core;

import java.util.List;

/**
 * Interface for executing filter conditions against a data source.
 * This is the final step in the filtering pipeline where the condition
 * is applied to actual data.
 */
public interface FilterExecutor {
    
    /**
     * Executes the filtering operation using the provided global condition.
     * 
     * @param <T> The type of entities being filtered
     * @param globalCondition The condition to apply for filtering
     * @param entityClass The class of entities to filter
     * @return A list of entities that match the condition
     */
    <T> List<T> execute(Condition globalCondition, Class<T> entityClass);
}

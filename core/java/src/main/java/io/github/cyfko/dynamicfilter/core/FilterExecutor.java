package io.github.cyfko.dynamicfilter.core;

/**
 * Interface for executing filter conditions against a data source.
 * This is the final step in the filtering pipeline where the condition
 * is applied to actual data.
 *
 * @param <R> type du résultat renvoyé par l'exécuteur
 */
public interface FilterExecutor<R> {
    
    /**
     * Executes the filtering operation using the provided global condition.
     *
     * @param globalCondition The condition to apply for filtering
     * @return An instance of the result R
     */
    R execute(Condition globalCondition);
}

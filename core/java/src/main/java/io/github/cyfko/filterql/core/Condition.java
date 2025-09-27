package io.github.cyfko.filterql.core;

/**
 * Interface representing a filter condition that can be combined with other conditions
 * using logical operators (AND, OR, NOT).
 * 
 * This interface follows the Composite pattern, allowing conditions to be nested
 * and combined to form complex boolean expressions.
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public interface Condition {
    
    /**
     * Creates a new condition representing the logical AND of this condition and another.
     * 
     * @param other The other condition to combine with
     * @return A new condition representing (this AND other)
     */
    Condition and(Condition other);
    
    /**
     * Creates a new condition representing the logical OR of this condition and another.
     * 
     * @param other The other condition to combine with
     * @return A new condition representing (this OR other)
     */
    Condition or(Condition other);
    
    /**
     * Creates a new condition representing the logical negation of this condition.
     * 
     * @return A new condition representing NOT(this)
     */
    Condition not();
}

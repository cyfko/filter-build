package io.github.cyfko.filterql.core;

/**
 * Interface representing a filter condition that can be combined with other conditions
 * using logical operators (AND, OR, NOT).
 * <p>
 * This interface follows the Composite pattern, allowing conditions to be nested
 * and combined to form complex boolean expressions.
 * </p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create individual conditions
 * Condition nameCondition = context.getCondition("nameFilter");
 * Condition ageCondition = context.getCondition("ageFilter");
 * Condition statusCondition = context.getCondition("statusFilter");
 * 
 * // Combine conditions using logical operators
 * Condition complexCondition = nameCondition
 *     .and(ageCondition.or(statusCondition))
 *     .and(someOtherCondition.not());
 * 
 * // This represents: nameFilter AND (ageFilter OR statusFilter) AND NOT(someOtherCondition)
 * }</pre>
 * 
 * <p><strong>Implementation Notes:</strong></p>
 * <ul>
 *   <li>Implementations should be immutable - each operation returns a new condition</li>
 *   <li>The actual execution strategy is left to the implementation (e.g., JPA Criteria, SQL, etc.)</li>
 *   <li>Conditions can be freely combined regardless of their underlying technology</li>
 * </ul>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public interface Condition {
    
    /**
     * Creates a new condition representing the logical AND of this condition and another.
     * <p>
     * The resulting condition will be satisfied only when both this condition 
     * and the other condition are satisfied.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * Condition nameFilter = context.getCondition("name");     // name = "Alice"
     * Condition ageFilter = context.getCondition("age");       // age > 25
     * Condition combined = nameFilter.and(ageFilter);          // name = "Alice" AND age > 25
     * }</pre>
     * 
     * @param other The other condition to combine with this one
     * @return A new condition representing (this AND other)
     * @throws IllegalArgumentException if the other condition is incompatible
     */
    Condition and(Condition other);
    
    /**
     * Creates a new condition representing the logical OR of this condition and another.
     * <p>
     * The resulting condition will be satisfied when either this condition 
     * or the other condition (or both) are satisfied.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * Condition activeFilter = context.getCondition("active");     // status = "ACTIVE"
     * Condition premiumFilter = context.getCondition("premium");   // type = "PREMIUM"
     * Condition combined = activeFilter.or(premiumFilter);         // status = "ACTIVE" OR type = "PREMIUM"
     * }</pre>
     * 
     * @param other The other condition to combine with this one
     * @return A new condition representing (this OR other)
     * @throws IllegalArgumentException if the other condition is incompatible
     */
    Condition or(Condition other);
    
    /**
     * Creates a new condition representing the logical negation of this condition.
     * <p>
     * The resulting condition will be satisfied only when this condition is NOT satisfied.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * Condition deletedFilter = context.getCondition("deleted");  // deleted = true
     * Condition notDeleted = deletedFilter.not();                 // NOT(deleted = true) -> deleted = false
     * }</pre>
     * 
     * @return A new condition representing NOT(this)
     */
    Condition not();
}

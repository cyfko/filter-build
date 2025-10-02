package io.github.cyfko.filterql.core;

import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterDefinition;

/**
 * Interface for providing conditions by filter key and managing filter definitions.
 * <p>
 * The Context acts as a registry that maps filter tokens to their corresponding conditions.
 * It serves as the bridge between filter definitions (property references, operators, values)
 * and the underlying query execution technology (JPA, SQL, etc.).
 * </p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * import io.github.cyfko.filterql.core.model.FilterDefinition;
 * import io.github.cyfko.filterql.core.validation.Op;
 * import io.github.cyfko.filterql.core.domain.PredicateResolver;
 * 
 * // Create a context for User entities
 * Context context = new FilterContext<>(User.class, UserPropertyRef.class, mapping);
 * 
 * // Add filter definitions
 * FilterDefinition<UserPropertyRef> nameFilter = 
 *     new FilterDefinition<>(UserPropertyRef.NAME, Op.LIKE, "John%");
 * FilterDefinition<UserPropertyRef> ageFilter = 
 *     new FilterDefinition<>(UserPropertyRef.AGE, Op.GREATER_THAN, 25);
 * 
 * context.addCondition("nameFilter", nameFilter);
 * context.addCondition("ageFilter", ageFilter);
 * 
 * // Retrieve conditions for combination
 * Condition nameCondition = context.getCondition("nameFilter");
 * Condition ageCondition = context.getCondition("ageFilter");
 * Condition combined = nameCondition.and(ageCondition);
 * 
 * // Convert to executable predicate
 * PredicateResolver<User> resolver = context.toResolver(User.class, combined);
 * }</pre>
 * 
 * <p><strong>Implementation Guidelines:</strong></p>
 * <ul>
 *   <li>Implementations should validate property references and operators</li>
 *   <li>Filter keys must be unique within a context</li>
 *   <li>Conditions should be immutable once created</li>
 *   <li>Type safety should be maintained throughout the conversion process</li>
 * </ul>
 * 
 * @see FilterDefinition
 * @see Condition
 * @see PredicateResolver
 * @author Frank KOSSI
 * @since 1.0
 */
public interface Context {

    /**
     * Adds a condition to the context based on a filter definition.
     * <p>
     * This method transforms a filter definition (property reference, operator, value)
     * into a concrete condition that can be executed against the target data store.
     * The transformation strategy depends on the implementation and the configured
     * mapping functions.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * FilterDefinition<UserPropertyRef> filter = new FilterDefinition<>(
     *     UserPropertyRef.EMAIL, 
     *     Op.LIKE, 
     *     "%@company.com"
     * );
     * 
     * Condition condition = context.addCondition("companyEmailFilter", filter);
     * // The condition can now be retrieved with context.getCondition("companyEmailFilter")
     * }</pre>
     *
     * @param filterKey The unique key to identify this condition within the context
     * @param definition The filter definition containing property, operator, and value
     * @return The created condition for immediate use (same as calling getCondition(filterKey))
     * @throws IllegalArgumentException if the operator is not supported for the property,
     *                                  or if the filter definition is invalid
     * @throws NullPointerException if filterKey or definition is null
     */
    Condition addCondition(String filterKey, FilterDefinition<?> definition);
    
    /**
     * Retrieves the condition associated with the given filter key.
     * <p>
     * Returns the condition that was previously registered with the specified key
     * using {@link #addCondition(String, FilterDefinition)}.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Assuming a condition was previously added
     * context.addCondition("userAgeFilter", ageFilterDefinition);
     * 
     * // Retrieve the condition for use in combinations
     * Condition ageCondition = context.getCondition("userAgeFilter");
     * Condition nameCondition = context.getCondition("userNameFilter");
     * 
     * // Combine conditions
     * Condition combined = ageCondition.and(nameCondition);
     * }</pre>
     * 
     * @param filterKey The unique identifier for the filter condition
     * @return The condition associated with the filter key
     * @throws IllegalArgumentException If no condition is associated with the given filter key
     * @throws NullPointerException if filterKey is null
     */
    Condition getCondition(String filterKey) throws IllegalArgumentException;

    /**
     * Converts a condition into a PredicateResolver for the specified entity type.
     * <p>
     * This method transforms a logical condition tree into an executable predicate resolver
     * that can generate JPA predicates when provided with a JPA context (Root, CriteriaQuery, CriteriaBuilder).
     * This enables the deferred execution pattern where the actual query construction
     * happens only when needed.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Build a complex condition
     * Condition nameCondition = context.getCondition("nameFilter");
     * Condition ageCondition = context.getCondition("ageFilter");
     * Condition combined = nameCondition.and(ageCondition);
     * 
     * // Convert to executable resolver
     * PredicateResolver<User> resolver = context.toResolver(User.class, combined);
     * 
     * // Use in JPA query
     * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
     * CriteriaQuery<User> query = cb.createQuery(User.class);
     * Root<User> root = query.from(User.class);
     * 
     * query.where(resolver.resolve(root, query, cb));
     * List<User> results = entityManager.createQuery(query).getResultList();
     * }</pre>
     * 
     * @param <E> The entity type (e.g., User, Product, Order)
     * @param entityClass The class of the entity type for type safety validation
     * @param condition The condition to transform into a predicate resolver
     * @return A PredicateResolver that can generate JPA predicates
     * @throws IllegalArgumentException If the entityClass doesn't match the context's entity type,
     *                                  or if the condition is incompatible with this context
     * @throws UnsupportedOperationException If the condition cannot be converted to the target technology
     * @throws NullPointerException if entityClass or condition is null
     */
    <E> PredicateResolver<E> toResolver(Class<E> entityClass, Condition condition);
}

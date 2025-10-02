package io.github.cyfko.filterql.core.domain;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Functional interface for resolving a logical condition into a JPA predicate.
 * <p>
 * A {@code PredicateResolver} represents a deferred predicate: it does not
 * directly hold a JPA predicate, but knows how to create one when
 * given the appropriate JPA context (root, query, builder).
 * </p>
 * 
 * <p>This interface bridges FilterQL conditions with JPA Criteria API,
 * enabling type-safe and composable query construction.</p>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Deferred Execution:</strong> Predicates are created only when needed</li>
 *   <li><strong>Type Safety:</strong> Generic entity type ensures compile-time safety</li>
 *   <li><strong>Composability:</strong> Can be combined with other predicates</li>
 *   <li><strong>Framework Integration:</strong> Works with any JPA-compliant ORM</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * 
 * <p><em>Basic Usage:</em></p>
 * <pre>{@code
 * // Create a simple predicate resolver
 * PredicateResolver<User> ageFilter = (root, query, cb) -> 
 *     cb.greaterThan(root.get("age"), 25);
 * 
 * // Use in JPA criteria query
 * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
 * CriteriaQuery<User> query = cb.createQuery(User.class);
 * Root<User> root = query.from(User.class);
 * 
 * query.where(ageFilter.resolve(root, query, cb));
 * List<User> results = entityManager.createQuery(query).getResultList();
 * }</pre>
 * 
 * <p><em>Complex Composition:</em></p>
 * <pre>{@code
 * // Combine multiple resolvers
 * PredicateResolver<User> nameFilter = (root, query, cb) -> 
 *     cb.like(root.get("name"), "John%");
 * PredicateResolver<User> activeFilter = (root, query, cb) -> 
 *     cb.equal(root.get("active"), true);
 * 
 * // Compose into complex condition
 * PredicateResolver<User> combinedFilter = (root, query, cb) -> cb.and(
 *     nameFilter.resolve(root, query, cb),
 *     activeFilter.resolve(root, query, cb)
 * );
 * }</pre>
 * 
 * <p><em>Integration with FilterQL:</em></p>
 * <pre>{@code
 * // FilterQL generates PredicateResolvers automatically
 * FilterResolver resolver = FilterResolver.of(context);
 * FilterRequest<UserPropertyRef> request = FilterRequest.builder()
 *     .filter("nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%"))
 *     .filter("ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 25))
 *     .combineWith("nameFilter & ageFilter")
 *     .build();
 * 
 * // Get executable predicate resolver
 * PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
 * 
 * // Execute query
 * query.where(predicateResolver.resolve(root, query, cb));
 * }</pre>
 * 
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li>Lightweight - no heavyweight object creation until resolution</li>
 *   <li>Cacheable - can be stored and reused across multiple queries</li>
 *   <li>Lazy evaluation - predicates built only when needed</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong></p>
 * <p>PredicateResolver implementations should be stateless and thread-safe.
 * Multiple threads can safely call the resolve method concurrently.</p>
 *
 * @param <E> the entity type this predicate resolver applies to
 * @see jakarta.persistence.criteria.Predicate
 * @see jakarta.persistence.criteria.CriteriaBuilder
 * @see io.github.cyfko.filterql.core.FilterResolver
 * @author Frank KOSSI
 * @since 1.0
 */
@FunctionalInterface
public interface PredicateResolver<E> {

    /**
     * Resolves this condition into a JPA predicate.
     * <p>
     * This method is called during query execution to convert the logical
     * condition into a concrete JPA predicate that can be used in the WHERE clause.
     * </p>
     * 
     * <p><strong>Implementation Guidelines:</strong></p>
     * <ul>
     *   <li>Method should be stateless and thread-safe</li>
     *   <li>Use the provided CriteriaBuilder for predicate construction</li>
     *   <li>Access entity properties through the Root parameter</li>
     *   <li>Handle null values and edge cases gracefully</li>
     * </ul>
     * 
     * <p><strong>Example Implementation:</strong></p>
     * <pre>{@code
     * PredicateResolver<User> resolver = (root, query, cb) -> {
     *     // Simple equality check
     *     return cb.equal(root.get("status"), UserStatus.ACTIVE);
     * };
     * 
     * // Or more complex logic
     * PredicateResolver<User> complexResolver = (root, query, cb) -> {
     *     Predicate nameCondition = cb.like(root.get("name"), "John%");
     *     Predicate ageCondition = cb.greaterThan(root.get("age"), 18);
     *     return cb.and(nameCondition, ageCondition);
     * };
     * }</pre>
     *
     * @param root The root entity in the criteria query, providing access to entity attributes
     * @param query The criteria query being constructed, useful for subqueries and query metadata
     * @param criteriaBuilder The JPA criteria builder for creating predicates and expressions
     * @return A JPA predicate representing this condition, ready for use in WHERE clauses
     * @throws IllegalArgumentException if the resolver cannot create a predicate with the given parameters
     * @throws jakarta.persistence.PersistenceException if there are JPA-related errors during predicate creation
     */
    Predicate resolve(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
}


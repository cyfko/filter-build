package io.github.cyfko.filterql.core.mappings;

import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.PropertyReference;

/**
 * Advanced mapping interface for creating custom predicate resolution strategies.
 * <p>
 * This interface enables the creation of sophisticated filter logic that goes beyond
 * simple property path mapping. It's particularly useful for:
 * </p>
 * <ul>
 *   <li>Complex business rules involving multiple entity properties</li>
 *   <li>Custom search algorithms (full-text search, fuzzy matching, etc.)</li>
 *   <li>Cross-entity filtering with joins and subqueries</li>
 *   <li>Calculated fields and aggregations</li>
 * </ul>
 * 
 * <p><strong>When to Use:</strong></p>
 * <ul>
 *   <li>When simple path mapping (String property names) isn't sufficient</li>
 *   <li>Need to implement custom business logic in filters</li>
 *   <li>Require complex JPA criteria expressions</li>
 *   <li>Want to encapsulate reusable filtering strategies</li>
 * </ul>
 * 
 * <p><strong>Implementation Examples:</strong></p>
 * 
 * <p><em>Example 1: Full Name Search</em></p>
 * <pre>{@code
 * public class FullNameMapping implements PredicateResolverMapping<User, UserPropertyRef> {
 *     @Override
 *     public PredicateResolver<User> resolve() {
 *         return (root, query, cb) -> {
 *             String searchTerm = (String) definition.getValue();
 *             
 *             // Search in both firstName and lastName
 *             return cb.or(
 *                 cb.like(cb.lower(root.get("firstName")), "%" + searchTerm.toLowerCase() + "%"),
 *                 cb.like(cb.lower(root.get("lastName")), "%" + searchTerm.toLowerCase() + "%"),
 *                 cb.like(cb.lower(cb.concat(
 *                     cb.concat(root.get("firstName"), " "), 
 *                     root.get("lastName")
 *                 )), "%" + searchTerm.toLowerCase() + "%")
 *             );
 *         };
 *     }
 * }
 * }</pre>
 * 
 * <p><em>Example 2: Age Range Calculation</em></p>
 * <pre>{@code
 * public class AgeRangeMapping implements PredicateResolverMapping<User, UserPropertyRef> {
 *     @Override
 *     public PredicateResolver<User> resolve() {
 *         return (root, query, cb) -> {
 *             int targetAge = (Integer) definition.getValue();
 *             LocalDate now = LocalDate.now();
 *             LocalDate minBirthDate = now.minusYears(targetAge + 1);
 *             LocalDate maxBirthDate = now.minusYears(targetAge);
 *             
 *             return cb.between(root.get("birthDate"), minBirthDate, maxBirthDate);
 *         };
 *     }
 * }
 * }</pre>
 * 
 * <p><em>Example 3: Related Entity Filter</em></p>
 * <pre>{@code
 * public class ActiveOrdersMapping implements PredicateResolverMapping<Customer, CustomerPropertyRef> {
 *     @Override
 *     public PredicateResolver<Customer> resolve() {
 *         return (root, query, cb) -> {
 *             Integer minOrderCount = (Integer) definition.getValue();
 *             
 *             // Subquery to count active orders
 *             Subquery<Long> subquery = query.subquery(Long.class);
 *             Root<Order> orderRoot = subquery.from(Order.class);
 *             
 *             subquery.select(cb.count(orderRoot))
 *                     .where(
 *                         cb.equal(orderRoot.get("customer"), root),
 *                         cb.equal(orderRoot.get("status"), OrderStatus.ACTIVE)
 *                     );
 *             
 *             return cb.greaterThanOrEqualTo(subquery, minOrderCount.longValue());
 *         };
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>Usage in Mapping Function:</strong></p>
 * <pre>{@code
 * Function<UserPropertyRef, Object> mappingFunction = ref -> switch (ref) {
 *     case NAME -> "name";                           // Simple path
 *     case EMAIL -> "email";                         // Simple path  
 *     case FULL_NAME -> new FullNameMapping();       // Custom mapping
 *     case AGE -> new AgeRangeMapping();              // Custom mapping
 *     case ACTIVE_ORDERS -> new ActiveOrdersMapping(); // Custom mapping
 * };
 * }</pre>
 * 
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *   <li>Keep mappings stateless and thread-safe</li>
 *   <li>Validate input values and provide meaningful error messages</li>
 *   <li>Consider performance implications of complex queries</li>
 *   <li>Use appropriate indexes for custom filter paths</li>
 *   <li>Document business logic and expected value types</li>
 * </ul>
 *
 * <p><strong>Version Compatibility:</strong></p>
 * <p><em>Breaking Change in 3.0.0:</em> The {@code resolve(FilterDefinition)} method has been 
 * simplified to {@code resolve()} to leverage closure capture. Existing implementations 
 * must be updated to use closure-captured filter definitions instead of method parameters.</p>
 *
 * @param <E> the entity type this mapping applies to
 * @param <P> the enum type representing logical property references
 * @see ReferenceMapping
 * @see PredicateResolver
 * @see FilterDefinition
 * @author Frank KOSSI
 * @since 1.0
 */
public interface PredicateResolverMapping<E, P extends Enum<P> & PropertyReference>
        extends ReferenceMapping<E> {

    /**
     * Resolves this mapping into a {@link PredicateResolver}.
     * <p>
     * This method transforms the mapping (with access to the filter definition through closure)
     * into an executable predicate resolver. The resolver can later be used to generate
     * JPA predicates when provided with a criteria context.
     * </p>
     * 
     * <p><strong>Implementation Guidelines:</strong></p>
     * <ul>
     *   <li>Access filter definition through closure capture from mappingBuilder</li>
     *   <li>Validate the captured definition and its components during resolve()</li>
     *   <li>Handle type conversions and operator-specific logic</li>
     *   <li>Return a thread-safe, stateless PredicateResolver</li>
     *   <li>Provide meaningful error messages for invalid inputs</li>
     * </ul>
     * 
     * <p><strong>Example Implementation:</strong></p>
     * <pre>{@code
     * // In mapping function
     * Function<FilterDefinition<UserPropertyRef>, Object> mappingBuilder = definition -> {
     *     return switch (definition.ref()) {
     *         case FULL_NAME -> new PredicateResolverMapping<User, UserPropertyRef>() {
     *             @Override
     *             public PredicateResolver<User> resolve() {
     *                 // Access definition from closure
     *                 if (definition.getOperator() != Op.LIKE) {
     *                     throw new IllegalArgumentException("Only LIKE operator supported for full name search");
     *                 }
     *                 
     *                 String searchValue = (String) definition.getValue();
     *                 if (searchValue == null || searchValue.trim().isEmpty()) {
     *                     throw new IllegalArgumentException("Search value cannot be empty");
     *                 }
     *                 
     *                 // Return predicate resolver
     *                 return (root, query, cb) -> {
     *                     String pattern = "%" + searchValue.toLowerCase() + "%";
     *                     return cb.or(
     *                         cb.like(cb.lower(root.get("firstName")), pattern),
     *                         cb.like(cb.lower(root.get("lastName")), pattern)
     *                     );
     *                 };
     *             }
     *         };
     *     };
     * };
     * }</pre>
     * 
     * @return a {@link PredicateResolver} that can produce a JPA {@link jakarta.persistence.criteria.Predicate}
     * @throws IllegalArgumentException if the captured definition is invalid or incompatible with this mapping
     * @since 3.0.0
     */
    PredicateResolver<E> resolve();
}


package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.mappings.PredicateResolverMapping;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Spring Data JPA implementation of the Context interface.
 * <p>
 * This adapter integrates FilterQL with Spring Data JPA by converting filter definitions
 * into Spring {@link org.springframework.data.jpa.domain.Specification} objects.
 * It supports both simple property path mappings and complex custom specification mappings.
 * </p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Type-safe filter definition to JPA Specification conversion</li>
 *   <li>Flexible mapping strategies (path strings vs. custom specifications)</li>
 *   <li>Full integration with Spring Data JPA repositories</li>
 *   <li>Support for complex nested property paths</li>
 *   <li>Custom business logic through PredicateResolverMapping</li>
 * </ul>
 * 
 * <p><strong>Mapping Strategies:</strong></p>
 * <p>The FilterContext uses a mapping function that can return:</p>
 * <ol>
 *   <li><strong>String:</strong> Simple property path (e.g., "name", "address.city")</li>
 *   <li><strong>PredicateResolverMapping:</strong> Custom specification logic</li>
 * </ol>
 * 
 * <p><strong>Complete Usage Example:</strong></p>
 * <pre>{@code
 * // 1. Define property reference enum
 * public enum UserPropertyRef implements PropertyReference {
 *     NAME(String.class, OperatorUtils.FOR_TEXT),
 *     EMAIL(String.class, OperatorUtils.FOR_TEXT),
 *     AGE(Integer.class, OperatorUtils.FOR_NUMBER),
 *     CITY(String.class, OperatorUtils.FOR_TEXT),
 *     FULL_NAME_SEARCH(String.class, Set.of(Op.MATCHES));
 * 
 *     // Standard PropertyReference implementation...
 * }
 * 
 * // 2. Create mapping function
 * Function<FilterDefinition<UserPropertyRef>, Object> mappingFunction = def -> switch (def.ref()) {
 *     case NAME -> "name";                    // Simple path
 *     case EMAIL -> "email";                  // Simple path
 *     case AGE -> "age";                      // Simple path
 *     case CITY -> "address.city.name";       // Nested path
 *     case FULL_NAME_SEARCH -> new PredicateResolverMapping<User, UserPropertyRef>() {
 *         @Override
 *         public PredicateResolver<User> resolve() {
 *             return (root, query, cb) -> {
 *                 String searchTerm = (String) definition.getValue();
 *                 return cb.or(
 *                     cb.like(root.get("firstName"), "%" + searchTerm + "%"),
 *                     cb.like(root.get("lastName"), "%" + searchTerm + "%")
 *                 );
 *             };
 *         }
 *     };
 * };
 * 
 * // 3. Create and configure context
 * FilterContext<User, UserPropertyRef> context = new FilterContext<>(
 *     User.class, 
 *     UserPropertyRef.class, 
 *     mappingFunction
 * );
 * 
 * // 4. Add filter definitions
 * FilterDefinition<UserPropertyRef> nameFilter = 
 *     new FilterDefinition<>(UserPropertyRef.NAME, Op.MATCHES, "John%");
 * FilterDefinition<UserPropertyRef> ageFilter = 
 *     new FilterDefinition<>(UserPropertyRef.AGE, Op.GT, 25);
 * 
 * context.addCondition("nameFilter", nameFilter);
 * context.addCondition("ageFilter", ageFilter);
 * 
 * // 5. Build complex condition
 * Condition nameCondition = context.getCondition("nameFilter");
 * Condition ageCondition = context.getCondition("ageFilter");
 * Condition combined = nameCondition.and(ageCondition);
 * 
 * // 6. Convert to PredicateResolver and use with Spring Data JPA
 * PredicateResolver<User> resolver = context.toResolver(User.class, combined);
 * 
 * // In your repository or service:
 * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
 * CriteriaQuery<User> query = cb.createQuery(User.class);
 * Root<User> root = query.from(User.class);
 * query.where(resolver.resolve(root, query, cb));
 * List<User> results = entityManager.createQuery(query).getResultList();
 * }</pre>
 * 
 * <p><strong>Thread Safety:</strong></p>
 * <p>FilterContext instances are thread-safe once configured. The mapping function
 * should also be stateless and thread-safe for concurrent use.</p>
 * 
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *   <li>Reuse FilterContext instances across requests</li>
 *   <li>Keep mapping functions stateless and side-effect free</li>
 *   <li>Use meaningful property reference names that match business concepts</li>
 *   <li>Validate entity relationships for nested path mappings</li>
 *   <li>Consider performance implications of complex custom mappings</li>
 * </ul>
 *
 * @param <E> The entity type (e.g., User, Product, Order)
 * @param <P> The property reference enum type implementing {@link PropertyReference}
 * @see Context
 * @see FilterCondition
 * @see PredicateResolverMapping
 * @see org.springframework.data.jpa.domain.Specification
 * @author Frank KOSSI
 * @since 2.0.0
 */
public class FilterContext<E,P extends Enum<P> & PropertyReference> implements Context {
    private final Class<E> entityClass;
    private final Class<P> enumClass;

    // If the object returned by the mapping function is a non-empty String then it is used as a property name
    // If it's an instance of SpecificationMapping<E> then the specification is directly used
    // If it's something else then an IllegalStateException is thrown
    private Function<FilterDefinition<P>, Object> mappingBuilder;

    private final Map<String, FilterCondition<?>> filters;

    /**
     * Constructs a new FilterContext for the specified entity and property reference types.
     * <p>
     * This constructor initializes the context with the required type information and
     * mapping strategy. The mapping function determines how property references are
     * translated into concrete filter implementations.
     * </p>
     * 
     * <p><strong>Mapping Function Requirements:</strong></p>
     * <p>The mapping function must return one of the following types:</p>
     * <ul>
     *   <li><strong>String:</strong> Property path for direct JPA attribute access (e.g., "name", "address.city")</li>
     *   <li><strong>PredicateResolverMapping:</strong> Custom filter logic implementation</li>
     * </ul>
     * 
     * <p><strong>Simple Property Path Examples:</strong></p>
     * <pre>{@code
     * Function<FilterDefinition<UserPropertyRef>, Object> simpleMapping = def -> switch (def.ref()) {
     *     case NAME -> "name";                     // Direct property access
     *     case EMAIL -> "email";                   // Direct property access
     *     case AGE -> "age";                       // Direct property access
     *     case CITY -> "address.city.name";        // Nested property navigation
     *     case ACTIVE -> "status.active";          // Nested boolean property
     * };
     * }</pre>
     * 
     * <p><strong>Custom Mapping Examples:</strong></p>
     * <pre>{@code
     * Function<FilterDefinition<UserPropertyRef>, Object> complexMapping = def -> switch (def.ref()) {
     *     // Simple path mappings
     *     case NAME -> "name";
     *     case EMAIL -> "email";
     *     
     *     // Custom full-text search logic
     *     case FULL_NAME_SEARCH -> new PredicateResolverMapping<User, UserPropertyRef>() {
     *         @Override
     *         public PredicateResolver<User> resolve() {
     *             return (root, query, cb) -> {
     *                 String searchTerm = (String) def.value();
     *                 return cb.or(
     *                     cb.like(cb.lower(root.get("firstName")), "%" + searchTerm.toLowerCase() + "%"),
     *                     cb.like(cb.lower(root.get("lastName")), "%" + searchTerm.toLowerCase() + "%"),
     *                     cb.like(cb.lower(cb.concat(root.get("firstName"), root.get("lastName"))), 
     *                              "%" + searchTerm.toLowerCase() + "%")
     *                 );
     *             };
     *         }
     *     };
     *     
     *     // Age range calculation from birth date
     *     case AGE_RANGE -> new PredicateResolverMapping<User, UserPropertyRef>() {
     *         @Override
     *         public PredicateResolver<User> resolve() {
     *             return (root, query, cb) -> {
     *                 List<Integer> ageRange = (List<Integer>) def.value();
     *                 LocalDate now = LocalDate.now();
     *                 LocalDate maxBirthDate = now.minusYears(ageRange.get(0));
     *                 LocalDate minBirthDate = now.minusYears(ageRange.get(1) + 1);
     *                 return cb.between(root.get("birthDate"), minBirthDate, maxBirthDate);
     *             };
     *         }
     *     };
     * };
     * }</pre>
     * 
     * <p><strong>Spring Boot Configuration Example:</strong></p>
     * <pre>{@code
     * @Configuration
     * public class FilterConfig {
     *     
     *     @Bean
     *     public FilterContext<User, UserPropertyRef> userFilterContext() {
     *         return new FilterContext<>(
     *             User.class,
     *             UserPropertyRef.class,
     *             this::mapUserProperties
     *         );
     *     }
     *     
     *     @Bean
     *     public FilterContext<Product, ProductPropertyRef> productFilterContext() {
     *         return new FilterContext<>(
     *             Product.class,
     *             ProductPropertyRef.class,
     *             this::mapProductProperties
     *         );
     *     }
     *     
     *     private Object mapUserProperties(FilterDefinition<UserPropertyRef> def) {
     *         return switch (def.ref()) {
     *             case NAME -> "name";
     *             case EMAIL -> "email";
     *             case DEPARTMENT -> "department.name";
     *             case FULL_NAME -> new FullNameSearchMapping(def);
     *         };
     *     }
     *     
     *     private Object mapProductProperties(FilterDefinition<ProductPropertyRef> def) {
     *         return switch (def.ref()) {
     *             case NAME -> "name";
     *             case PRICE -> "price";
     *             case CATEGORY -> "category.name";
     *             case IN_STOCK -> new InStockMapping(def);
     *         };
     *     }
     * }
     * }</pre>
     * 
     * <p><strong>Validation and Error Handling:</strong></p>
     * <p>The constructor validates that all parameters are non-null but does not
     * validate the mapping function's behavior. Invalid mappings will be detected
     * when filters are added via {@link #addCondition(String, FilterDefinition)}.</p>
     *
     * @param entityClass The class of the entity this context will filter (e.g., User.class)
     * @param enumClass The class of the property reference enum (e.g., UserPropertyRef.class)
     * @param mappingBuilder The function that maps filter definitions to filter implementations.
     *                      Must return either a String (property path) or PredicateResolverMapping
     * @throws NullPointerException if any parameter is null
     * @see PredicateResolverMapping
     * @see PropertyReference
     */
    public FilterContext(Class<E> entityClass, Class<P> enumClass, Function<FilterDefinition<P>, Object> mappingBuilder) {
        this.entityClass = entityClass;
        this.enumClass = enumClass;
        this.mappingBuilder = mappingBuilder;
        this.filters = new HashMap<>();
    }

    // If the object returned by the mapping function is a non-empty String then it is used as a property name
    // If it's an instance of SpecificationMapping<E> then the specification is directly used
    // If it's something else then an IllegalStateException is thrown
    // This method returns the previous builder used.
    /**
     * Sets the mapping builder function for transforming filter definitions.
     * <p>
     * This method allows dynamic reconfiguration of how property references are mapped
     * to filter implementations. It's useful for scenarios where mapping logic needs
     * to be updated at runtime, such as multi-tenant applications or A/B testing
     * of different filtering strategies.
     * </p>
     * 
     * <p><strong>Thread Safety:</strong> This method is not thread-safe. If called
     * concurrently with filter operations, it may cause inconsistent behavior.
     * Ensure proper synchronization if runtime changes are required.</p>
     * 
     * <p><strong>Usage Examples:</strong></p>
     * <pre>{@code
     * // Switch to a more permissive mapping for admin users
     * Function<FilterDefinition<UserPropertyRef>, Object> adminMapping = def -> switch (def.ref()) {
     *     case NAME -> "name";
     *     case EMAIL -> "email";
     *     case ADMIN_NOTES -> "internalNotes";  // Only available for admins
     * };
     * 
     * Function<FilterDefinition<UserPropertyRef>, Object> oldMapping = 
     *     context.setMappingBuilder(adminMapping);
     * 
     * // Later, restore the previous mapping
     * context.setMappingBuilder(oldMapping);
     * 
     * // Or switch to a debugging mapping that logs all filter attempts
     * Function<FilterDefinition<UserPropertyRef>, Object> debugMapping = def -> {
     *     logger.debug("Mapping property: {} with operator: {} and value: {}", 
     *                  def.ref(), def.operator(), def.value());
     *     return originalMapping.apply(def);
     * };
     * context.setMappingBuilder(debugMapping);
     * }</pre>
     * 
     * <p><strong>Important Notes:</strong></p>
     * <ul>
     *   <li>Changing the mapping builder does not affect already-added conditions</li>
     *   <li>New conditions will use the updated mapping function</li>
     *   <li>The previous builder is returned to enable restoration</li>
     *   <li>The new builder must be non-null</li>
     * </ul>
     * 
     * @param mappingBuilder the new function to transform filter definitions, must not be null
     * @return the previous mapping builder that was set, never null
     * @throws NullPointerException if mappingBuilder is null
     */
    public Function<FilterDefinition<P>, Object> setMappingBuilder(Function<FilterDefinition<P>, Object> mappingBuilder) {
        var prev = this.mappingBuilder;
        this.mappingBuilder = Objects.requireNonNull(mappingBuilder);
        return prev;
    }

    /**
     * Adds a condition to the context based on a filter definition.
     * <p>
     * This method transforms a filter definition into an executable condition by:
     * </p>
     * <ol>
     *   <li>Validating the filter definition against the property reference</li>
     *   <li>Applying the configured mapping function to determine the implementation strategy</li>
     *   <li>Creating either a path-based or custom specification-based condition</li>
     *   <li>Storing the condition with the specified key for later retrieval</li>
     * </ol>
     * 
     * <p><strong>Validation Process:</strong></p>
     * <ul>
     *   <li>Filter key must be non-null and non-empty</li>
     *   <li>Property reference must match the configured enum type</li>
     *   <li>Operator must be supported by the property reference</li>
     *   <li>Value type must be compatible with the operator</li>
     * </ul>
     * 
     * <p><strong>Mapping Resolution:</strong></p>
     * <p>The mapping function can return:</p>
     * <ul>
     *   <li><strong>String:</strong> Treated as a JPA property path (e.g., "name", "address.city")</li>
     *   <li><strong>PredicateResolverMapping:</strong> Custom filter implementation</li>
     * </ul>
     * 
     * <p><strong>Usage Examples:</strong></p>
     * <pre>{@code
     * // Simple path-based filter
     * FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
     *     UserPropertyRef.NAME, 
     *     Op.MATCHES, 
     *     "John%"
     * );
     * Condition nameCondition = context.addCondition("nameFilter", nameFilter);
     * 
     * // Complex range filter
     * FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
     *     UserPropertyRef.AGE, 
     *     Op.RANGE, 
     *     List.of(25, 65)
     * );
     * Condition ageCondition = context.addCondition("ageRange", ageFilter);
     * 
     * // Custom business logic filter
     * FilterDefinition<UserPropertyRef> vipFilter = new FilterDefinition<>(
     *     UserPropertyRef.IS_VIP, 
     *     Op.EQ, 
     *     true
     * );
     * Condition vipCondition = context.addCondition("vipCustomers", vipFilter);
     * }</pre>
     * 
     * <p><strong>Error Scenarios:</strong></p>
     * <pre>{@code
     * // Invalid filter key
     * try {
     *     context.addCondition("", someFilter);
     * } catch (IllegalArgumentException e) {
     *     // "Filter key cannot be null or empty"
     * }
     * 
     * // Wrong enum type
     * try {
     *     context.addCondition("filter", wrongEnumFilter);
     * } catch (IllegalArgumentException e) {
     *     // "Provided definition is for Enum WrongPropertyRef. Expected definition for Enum: UserPropertyRef."
     * }
     * 
     * // Unsupported operator
     * try {
     *     FilterDefinition<UserPropertyRef> invalidFilter = new FilterDefinition<>(
     *         UserPropertyRef.NAME,  // String property
     *         Op.GT,                 // Numeric operator
     *         "value"
     *     );
     *     context.addCondition("invalid", invalidFilter);
     * } catch (FilterValidationException e) {
     *     // "Operator GT is not supported for property NAME..."
     * }
     * }</pre>
     * 
     * <p><strong>Performance Notes:</strong></p>
     * <ul>
     *   <li>Validation is performed immediately upon addition</li>
     *   <li>Mapping function is invoked once per filter</li>
     *   <li>Conditions are cached for efficient retrieval</li>
     *   <li>Duplicate filter keys will overwrite previous conditions</li>
     * </ul>
     *
     * @param filterKey The unique key to identify this condition within the context, must not be null or empty
     * @param definition The filter definition containing property, operator, and value, must not be null
     * @return The created condition for immediate use (same as calling getCondition(filterKey))
     * @throws IllegalArgumentException if filterKey is null/empty, if the property reference enum type doesn't match,
     *                                  or if the mapping function returns an unsupported type
     * @throws FilterValidationException if the operator is not supported for the property,
     *                                  or if the value is incompatible with the operator
     * @see FilterDefinition
     * @see PropertyReference#validateOperatorForValue(io.github.cyfko.filterql.core.validation.Op, Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Condition addCondition(String filterKey, FilterDefinition<?> definition) {
        // Validate filter key
        if (filterKey == null || filterKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Filter key cannot be null or empty");
        }
        
        Enum<?> ref = definition.ref();

        // Ensure definition is for the same property reference enumeration
        if (! enumClass.isAssignableFrom(ref.getClass())) {
            throw new IllegalArgumentException(String.format("Provided definition is for Enum %s. Expected definition for Enum: %s.",
                    ref.getClass().getSimpleName(),
                    enumClass.getSimpleName())
            );
        }

        // Ensure value type compatibility for the given operator
        ((PropertyReference) ref).validateOperatorForValue(definition.operator(), definition.value());

        // Transform definition into a filter condition using the mapping function
        Object mapping = mappingBuilder.apply((FilterDefinition<P>) definition);

        if (mapping instanceof PredicateResolverMapping<?,?>) {
            PredicateResolver<E> resolver = ((PredicateResolverMapping<E,P>) mapping).resolve();
            FilterCondition<E> condition = new FilterCondition<>(resolver::resolve);
            filters.put(filterKey, condition);
            return condition;
        }

        if (mapping instanceof String pathName) {
            Specification<E> spec = getSpecificationFromPath(pathName, definition);
            FilterCondition<E> condition = new FilterCondition<>(spec);
            filters.put(filterKey, condition);
            return condition;
        }

        throw new IllegalArgumentException("Invalid mapping function unsupported yet");
    }

    @Override
    public Condition getCondition(String filterKey) {
        FilterCondition<?> condition = filters.get(filterKey);
        if (condition == null) {
            throw new IllegalArgumentException(filterKey + " not found");
        }
        return condition;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> PredicateResolver<U> toResolver(Class<U> entityClass, Condition condition) {
        if (entityClass != this.entityClass) {
            throw new IllegalArgumentException(String.format("Entity class %s not match. Expected: %s.", entityClass.getSimpleName(), this.entityClass.getSimpleName()));
        }

        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }

        try {
            Specification<U> specification = ((FilterCondition<U>) condition).getSpecification();
            return specification::toPredicate;
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Unable to convert the provided Condition instance of class "+ condition.getClass().getSimpleName() + ". instances not supported.", e);
        }
    }

    /**
     * Utility method to build a Spring Data JPA specification
     * from a {@code PathMapping}-based property reference.
     *
     * @param pathName name of the property path
     * @param definition FilterDefinition on which to operate
     * @param <E>   the entity type
     * @param <P>   the property reference type
     * @return a specification for use in JPA criteria queries
     */
    private static <E, P extends Enum<P> & PropertyReference> Specification<E> getSpecificationFromPath(String pathName, FilterDefinition<P> definition) {
        // Ensure value type compatibility for the given operator
        Objects.requireNonNull(definition);

        return (Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Object value = definition.value();

            // Resolve criteria path from path mapping
            Path<?> path = PathResolverUtils.resolvePath(root, pathName);

            // Switch on supported operators to construct a predicate
            return switch (definition.operator()) {
                case EQ -> cb.equal(path, value);
                case NE -> cb.notEqual(path, value);
                case GT -> cb.gt((Path<Number>) path, (Number) value);
                case GTE -> cb.ge((Path<Number>) path, (Number) value);
                case LT -> cb.lt((Path<Number>) path, (Number) value);
                case LTE -> cb.le((Path<Number>) path, (Number) value);
                case MATCHES -> cb.like((Path<String>) path, (String) value);
                case NOT_MATCHES -> cb.notLike((Path<String>) path, (String) value);
                case IN -> path.in((Collection<?>) value);
                case NOT_IN -> cb.not(path.in((Collection<?>) value));
                case IS_NULL -> cb.isNull(path);
                case NOT_NULL -> cb.isNotNull(path);
                case RANGE -> {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    yield cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]);
                }
                case NOT_RANGE -> {
                    Object[] valuesToCompare = ((Collection<?>) value).toArray();
                    yield cb.not(cb.between((Path<Comparable>) path, (Comparable) valuesToCompare[0], (Comparable) valuesToCompare[1]));
                }
            };
        };
    }
}





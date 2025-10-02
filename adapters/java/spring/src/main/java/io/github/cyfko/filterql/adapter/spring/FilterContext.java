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
 * Function<UserPropertyRef, Object> mappingFunction = ref -> switch (ref) {
 *     case NAME -> "name";                    // Simple path
 *     case EMAIL -> "email";                  // Simple path
 *     case AGE -> "age";                      // Simple path
 *     case CITY -> "address.city.name";       // Nested path
 *     case FULL_NAME_SEARCH -> new PredicateResolverMapping<User, UserPropertyRef>() {
 *         @Override
 *         public PredicateResolver<User> resolve(FilterDefinition<UserPropertyRef> definition) {
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
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Simple mapping function using property paths
     * Function<UserPropertyRef, Object> simpleMapping = ref -> switch (ref) {
     *     case NAME -> "name";
     *     case EMAIL -> "email";
     *     case AGE -> "age";
     * };
     * 
     * FilterContext<User, UserPropertyRef> context = new FilterContext<>(
     *     User.class,
     *     UserPropertyRef.class,
     *     simpleMapping
     * );
     * }</pre>
     *
     * @param entityClass The class of the entity this context will filter (e.g., User.class)
     * @param enumClass The class of the property reference enum (e.g., UserPropertyRef.class)
     * @param mappingBuilder The function that maps property references to filter implementations.
     *                      Must return either a String (property path) or PredicateResolverMapping
     * @throws NullPointerException if any parameter is null
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
    public Function<FilterDefinition<P>, Object> setMappingBuilder(Function<FilterDefinition<P>, Object> mappingBuilder) {
        var prev = this.mappingBuilder;
        this.mappingBuilder = Objects.requireNonNull(mappingBuilder);
        return prev;
    }

    /**
     * Adds a condition to the context.
     *
     * @param filterKey The key to identify this condition
     * @param definition The filter definition containing property, operator, and value
     * @throws IllegalArgumentException if the operator is not supported for the property
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

        Object mapping = mappingBuilder.apply((FilterDefinition<P>) definition);

        if (mapping instanceof PredicateResolverMapping<?,?>) {
            PredicateResolver<E> resolver = ((PredicateResolverMapping<E,P>) mapping).resolve((FilterDefinition<P>) definition);
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
        Objects.requireNonNull(definition).ref().validateOperatorForValue(definition.operator(), definition.value());

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





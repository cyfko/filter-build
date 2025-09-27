package io.github.cyfko.filterql.adapter.jpa.impl;

import io.github.cyfko.filterql.adapter.jpa.ConditionAdapter;
import io.github.cyfko.filterql.adapter.jpa.ContextAdapter;
import io.github.cyfko.filterql.adapter.jpa.FilterExecutor;
import io.github.cyfko.filterql.adapter.jpa.Specification;
import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.impl.DSLParser;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.utils.ClassUtils;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import java.util.*;

/**
 * Basic implementation of {@link FilterExecutor} that uses JPA Criteria API to execute queries.
 * <p>
 * This executor validates the condition, constructs a criteria query using the provided condition,
 * executes the query with the {@link EntityManager}, and returns the filtered results.
 * </p>
 *
 * <p>
 * The {@code findAll} method expects the {@link Condition} to be an instance of {@link ConditionAdapter}
 * which wraps a {@link Specification}. It ensures the condition is compatible with the entity type.
 * </p>
 *
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe. The internal registry uses a {@link ConcurrentHashMap}
 * to store path name specification builders safely across multiple threads.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Setup
 * BasicFilterExecutor executor = new BasicFilterExecutor(entityManager);
 * executor.registerPathNameBuilder(User.class, UserPropertyRef.class,
 *     prop -> switch(prop) {
 *         case USER_NAME -> "name";
 *         case USER_EMAIL -> "email";
 *         case USER_AGE -> "age";
 *     });
 *
 * // Execute query
 * FilterRequest<UserPropertyRef> request = FilterRequest.builder()
 *     .combineWith("A & B")
 *     .filter("A", UserPropertyRef.USER_NAME, Operator.LIKE, "john%")
 *     .filter("B", UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 25)
 *     .build();
 *
 * List<User> results = executor.findAll(User.class, request);
 * }</pre>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public final class BasicFilterExecutor implements FilterExecutor {

    private final EntityManager em;
    private final Map<String, PathNameSpecificationBuilder<?, ?>> registry;

    /**
     * Constructs a new BasicFilterExecutor with the given {@link EntityManager}.
     *
     * @param em the EntityManager to be used for query execution; must not be null
     * @throws NullPointerException if {@code em} is null
     */
    public BasicFilterExecutor(EntityManager em) {
        this.em = Objects.requireNonNull(em, "EntityManager must not be null");
        this.registry = new ConcurrentHashMap<>();
    }

    /**
     * Registers a path name specification builder for a specific entity and property reference combination.
     * <p>
     * This method must be called before executing queries to ensure the executor knows how to map
     * property references to actual entity field paths.
     * </p>
     *
     * @param <E>           the entity type
     * @param <P>           the property reference enum type
     * @param entityClass   the entity class, must not be null
     * @param propertyClass the property reference enum class, must not be null
     * @param builder       the path name specification builder, must not be null
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if a builder is already registered for this combination
     */
    public <E, P extends Enum<P> & PropertyRef> void registerPathNameBuilder(
            Class<E> entityClass,
            Class<P> propertyClass,
            PathNameSpecificationBuilder<E, P> builder) {

        Objects.requireNonNull(entityClass, "Entity class must not be null");
        Objects.requireNonNull(propertyClass, "Property class must not be null");
        Objects.requireNonNull(builder, "Builder must not be null");

        String key = generateRegistryKey(entityClass, propertyClass);
        PathNameSpecificationBuilder<?, ?> existing = registry.putIfAbsent(key, builder);

        if (existing != null) {
            throw new IllegalArgumentException(
                    String.format("PathNameSpecificationBuilder already registered for entity <%s> and property <%s>",
                            entityClass.getSimpleName(), propertyClass.getSimpleName())
            );
        }
    }

    /**
     * Checks if a path name specification builder is registered for the given entity and property combination.
     *
     * @param <E>           the entity type
     * @param <P>           the property reference enum type
     * @param entityClass   the entity class
     * @param propertyClass the property reference enum class
     * @return true if a builder is registered, false otherwise
     */
    public <E, P extends Enum<P> & PropertyRef> boolean isRegistered(
            Class<E> entityClass,
            Class<P> propertyClass) {

        Objects.requireNonNull(entityClass, "Entity class must not be null");
        Objects.requireNonNull(propertyClass, "Property class must not be null");

        String key = generateRegistryKey(entityClass, propertyClass);
        return registry.containsKey(key);
    }

    /**
     * Unregisters the path name specification builder for the given entity and property combination.
     *
     * @param <E>           the entity type
     * @param <P>           the property reference enum type
     * @param entityClass   the entity class
     * @param propertyClass the property reference enum class
     * @return the previously registered builder, or null if none was registered
     */
    public <E, P extends Enum<P> & PropertyRef> PathNameSpecificationBuilder<E, P> unregisterPathNameBuilder(
            Class<E> entityClass,
            Class<P> propertyClass) {

        Objects.requireNonNull(entityClass, "Entity class must not be null");
        Objects.requireNonNull(propertyClass, "Property class must not be null");

        String key = generateRegistryKey(entityClass, propertyClass);
        @SuppressWarnings("unchecked")
        PathNameSpecificationBuilder<E, P> removed = (PathNameSpecificationBuilder<E, P>) registry.remove(key);

        return removed;
    }

    /**
     * Finds all entities of the specified class satisfying the provided condition.
     *
     * @param <E>         the type of entity to retrieve
     * @param <P>         the property reference enum type
     * @param entityClass the class of the entity, must not be null
     * @param request     the filter request containing DSL expression and individual filters, must not be null
     * @return a list of entities matching the condition, never null but may be empty
     * @throws NullPointerException     if entityClass or request is null
     * @throws IllegalStateException    if no path name specification builder is registered for the entity/property combination
     * @throws DSLSyntaxException       if the DSL expression in the request is invalid
     * @throws IllegalArgumentException if the filter conditions are invalid
     */
    @Override
    public <E, P extends Enum<P> & PropertyRef> List<E> findAll(Class<E> entityClass, FilterRequest<P> request) {
        Objects.requireNonNull(entityClass, "Entity class must not be null");
        Objects.requireNonNull(request, "Filter request must not be null");

        // Phase 1: Parse the filter DSL
        DSLParser dslParser = new DSLParser();
        FilterTree filterTree = dslParser.parse(request.getCombineWith());

        // Phase 2: Resolve PathNameSpecificationBuilder
        PathNameSpecificationBuilder<E, P> builder = resolvePathNameBuilder(entityClass, request);

        // Phase 3: Build Context with conditions
        ContextAdapter<E, P> context = new ContextAdapter<>(builder);
        request.getFilters().forEach(context::addCondition);

        // Phase 4: Generate specification from filter tree
        Condition condition = filterTree.generate(context);
        if (!(condition instanceof ConditionAdapter)) {
            throw new IllegalStateException("Generated condition is not a ConditionAdapter instance");
        }

        @SuppressWarnings("unchecked")
        ConditionAdapter<E> conditionAdapter = (ConditionAdapter<E>) condition;
        Specification<E> specification = conditionAdapter.getSpecification();

        // Phase 5: Execute the query
        return executeQuery(entityClass, specification);
    }

    /**
     * Executes the query with the given specification.
     */
    private <E> List<E> executeQuery(Class<E> entityClass, Specification<E> specification) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> query = cb.createQuery(entityClass);
        Root<E> root = query.from(entityClass);

        Predicate predicate = specification.toPredicate(root, query, cb);
        if (predicate != null) {
            query.select(root).where(predicate);
        } else {
            query.select(root);
        }

        TypedQuery<E> typedQuery = em.createQuery(query);
        return typedQuery.getResultList();
    }

    /**
     * Resolves the PathNameSpecificationBuilder for the given request.
     * Attempts to infer the property class from the request if possible.
     */
    @SuppressWarnings("unchecked")
    private <E, P extends Enum<P> & PropertyRef> PathNameSpecificationBuilder<E, P> resolvePathNameBuilder(
            Class<E> entityClass, FilterRequest<P> request) {

        // Try to infer property class from the first filter
        Class<P> propertyClass = BasicFilterExecutor.getPropertyClass(request.getFilters());

        String key = generateRegistryKey(entityClass, propertyClass);
        PathNameSpecificationBuilder<?, ?> builder = registry.get(key);

        if (builder == null) {
            throw new IllegalStateException(
                    String.format("No PathNameSpecificationBuilder registered for entity <%s> and property <%s>. " +
                                    "Please register a builder using registerPathNameBuilder() before executing queries.",
                            entityClass.getSimpleName(), propertyClass.getSimpleName())
            );
        }

        return (PathNameSpecificationBuilder<E, P>) builder;
    }

    /**
     * Generates a unique registry key for the entity and property class combination.
     */
    private static <E, P extends Enum<P> & PropertyRef> String generateRegistryKey(
            Class<E> entityClass, Class<P> propertyClass) {
        return entityClass.getName() + ":" + propertyClass.getName();
    }

    /**
     * Retourne une valeur quelconque de la map.
     * @param map la map source
     * @return une valeur arbitraire, ou null si la map est vide
     */
    @SuppressWarnings("unchecked")
    private static <P extends Enum<P> & PropertyRef> Class<P> getPropertyClass(Map<String, FilterDefinition<P>> map) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("Filter request must contain at least one filter to infer property type");
        }
        return (Class<P>) map.values().iterator().next().getRef().getClass();
    }

    /**
     * Returns the number of registered path name specification builders.
     * Useful for monitoring and debugging.
     *
     * @return the number of registered builders
     */
    public int getRegisteredBuildersCount() {
        return registry.size();
    }

    /**
     * Returns a copy of all registered entity-property combinations.
     * Useful for debugging and monitoring.
     *
     * @return an unmodifiable set of registry keys
     */
    public Set<String> getRegisteredCombinations() {
        return Collections.unmodifiableSet(new HashSet<>(registry.keySet()));
    }

    /**
     * Clears all registered path name specification builders.
     * Use with caution - this will affect all subsequent queries.
     */
    public void clearRegistry() {
        registry.clear();
    }

    /**
     * Returns the underlying EntityManager.
     * Useful for advanced use cases or integration testing.
     *
     * @return the EntityManager instance
     */
    public EntityManager getEntityManager() {
        return em;
    }
}

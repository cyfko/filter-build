package io.github.cyfko.filterql.core;

import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.impl.DSLParser;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.PropertyReference;

import java.util.Objects;


/**
 * High-level facade for resolving filter requests into executable predicates.
 * <p>
 * The FilterResolver provides a streamlined API that combines DSL parsing, 
 * context management, and predicate resolution into a single, easy-to-use interface.
 * It orchestrates the entire filtering pipeline from filter request to executable predicate.
 * </p>
 * 
 * <p><strong>Architecture Overview:</strong></p>
 * <ol>
 *   <li><strong>Parse:</strong> Transform DSL expression into FilterTree using {@link Parser}</li>
 *   <li><strong>Populate:</strong> Register filter definitions in {@link Context}</li>
 *   <li><strong>Generate:</strong> Create condition tree from FilterTree and Context</li>
 *   <li><strong>Resolve:</strong> Convert conditions into executable {@link PredicateResolver}</li>
 * </ol>
 * 
 * <p><strong>Complete Usage Example:</strong></p>
 * <pre>{@code
 * // 1. Setup context with mapping strategy
 * FilterContext<User, UserPropertyRef> context = new FilterContext<>(
 *     User.class, 
 *     UserPropertyRef.class,
 *     propertyRef -> switch (propertyRef) {
 *         case NAME -> "name";           // Simple path mapping
 *         case EMAIL -> "email";         // Simple path mapping
 *         case ADDRESS_CITY -> "address.city.name"; // Nested path
 *         case CUSTOM_FILTER -> new CustomSpecificationMapping(); // Custom logic
 *     }
 * );
 * 
 * // 2. Create filter resolver
 * FilterResolver resolver = FilterResolver.of(context);
 * 
 * // 3. Build filter request
 * Map<String, FilterDefinition<UserPropertyRef>> filters = Map.of(
 *     "nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Op.LIKE, "John%"),
 *     "emailFilter", new FilterDefinition<>(UserPropertyRef.EMAIL, Op.LIKE, "%@company.com"),
 *     "cityFilter", new FilterDefinition<>(UserPropertyRef.ADDRESS_CITY, Op.EQUALS, "Paris")
 * );
 * 
 * FilterRequest<UserPropertyRef> request = new FilterRequest<>(
 *     filters,
 *     "(nameFilter & emailFilter) | cityFilter"  // DSL expression
 * );
 * 
 * // 4. Resolve to executable predicate
 * PredicateResolver<User> predicateResolver = resolver.resolve(User.class, request);
 * 
 * // 5. Execute query
 * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
 * CriteriaQuery<User> query = cb.createQuery(User.class);
 * Root<User> root = query.from(User.class);
 * 
 * query.where(predicateResolver.resolve(root, query, cb));
 * List<User> results = entityManager.createQuery(query).getResultList();
 * }</pre>
 * 
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>{@link DSLSyntaxException} - Invalid DSL expression syntax</li>
 *   <li>{@link FilterValidationException} - Filter validation failures</li>
 *   <li>{@link IllegalArgumentException} - Invalid filter definitions or mappings</li>
 * </ul>
 * 
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *   <li>Reuse FilterResolver instances when possible (they are thread-safe)</li>
 *   <li>Keep DSL expressions simple and readable</li>
 *   <li>Validate filter definitions early in the request processing</li>
 *   <li>Use meaningful filter keys that correspond to business concepts</li>
 * </ul>
 *
 * @see Parser
 * @see FilterTree
 * @see PredicateResolver
 * @see Context
 * @see FilterRequest
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterResolver {
    private Parser dslParser;
    private Context context;

    private FilterResolver(Parser dslParser, Context context) {
        this.dslParser = Objects.requireNonNull(dslParser, "DSL parser cannot be null");
        this.context = Objects.requireNonNull(context, "Context cannot be null");
    }

    /**
     * Creates a {@code FilterResolver} with the given parser and context.
     * <p>
     * This factory method allows full customization of both the parsing strategy
     * and the context implementation. Use this when you need custom parsing logic
     * or specialized context behavior.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Custom parser with extended DSL syntax
     * Parser customParser = new ExtendedDSLParser();
     * 
     * // Context with specific mapping strategies
     * Context context = new FilterContext<>(User.class, UserPropertyRef.class, mappingFunction);
     * 
     * // Create resolver with custom components
     * FilterResolver resolver = FilterResolver.of(customParser, context);
     * }</pre>
     * 
     * @param dslParser The parser to use for DSL expressions. Must not be null.
     * @param context The context to use for condition resolution. Must not be null.
     * @return A new FilterResolver instance configured with the provided components
     * @throws NullPointerException if dslParser or context is null
     */
    public static FilterResolver of(Parser dslParser, Context context) {
        return new FilterResolver(dslParser, context);
    }

    /**
     * Creates a {@code FilterResolver} with the default {@link DSLParser} and the given context.
     * <p>
     * This is the most commonly used factory method. It provides the standard DSL parsing
     * capabilities while allowing customization of the context and mapping strategies.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Create context with mapping function
     * FilterContext<Product, ProductPropertyRef> context = new FilterContext<>(
     *     Product.class, 
     *     ProductPropertyRef.class,
     *     ref -> switch (ref) {
     *         case NAME -> "name";
     *         case PRICE -> "price";
     *         case CATEGORY_NAME -> "category.name";
     *     }
     * );
     * 
     * // Create resolver with default parser
     * FilterResolver resolver = FilterResolver.of(context);
     * }</pre>
     * 
     * @param context The context to use for condition resolution. Must not be null.
     * @return A new FilterResolver instance with default DSL parser
     * @throws NullPointerException if context is null
     */
    public static FilterResolver of(Context context) {
        return new FilterResolver(new DSLParser(), context);
    }

    /**
     * Resolves a {@link PredicateResolver} for the specified entity type from the given {@link FilterRequest}.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Parse the DSL expression from {@link FilterRequest#getCombineWith()} into a {@link FilterTree}.</li>
     *   <li>Register all filter definitions into the {@link Context}.</li>
     *   <li>Generate a {@link Condition} tree and adapt it into a {@link PredicateResolver}.</li>
     * </ol>
     *
     * @param entityClass   the entity type
     * @param filterRequest the request containing filter definitions and the DSL combination logic
     * @param <E>           the entity type
     * @param <P>           the property enum type (must implement {@link PropertyReference})
     * @return a {@link PredicateResolver} representing the filter request
     * @throws NullPointerException      if entityClass or filterRequest is null
     * @throws DSLSyntaxException        if the DSL expression has invalid syntax
     * @throws FilterValidationException if filter validation fails
     */
    public <E, P extends Enum<P> & PropertyReference>
    PredicateResolver<E> resolve(Class<E> entityClass, FilterRequest<P> filterRequest)
            throws DSLSyntaxException, FilterValidationException {

        // Validate input parameters
        Objects.requireNonNull(entityClass, "Entity class cannot be null");
        Objects.requireNonNull(filterRequest, "Filter request cannot be null");

        // Parse DSL into a filter tree
        FilterTree filterTree = dslParser.parse(filterRequest.getCombineWith());

        // Populate the context with filter definitions
        filterRequest.getFilters().forEach(context::addCondition);

        // Generate a condition tree and convert it into a PredicateResolver
        Condition condition = filterTree.generate(context);
        return context.toResolver(entityClass, condition);
    }

    /**
     * Replaces the internal DSL parser and returns the previous parser instance.
     *
     * @param dslParser the new DSL parser (not null)
     * @return the previous parser instance
     * @throws NullPointerException if {@code dslParser} is null
     */
    public Parser setParser(Parser dslParser) {
        var prev = this.dslParser;
        this.dslParser = Objects.requireNonNull(dslParser, "DSL parser cannot be null");
        return prev;
    }

    /**
     * Replaces the internal context and returns the previous context instance.
     *
     * @param context the new context (not null)
     * @return the previous context instance
     * @throws NullPointerException if {@code context} is null
     */
    public Context setContext(Context context) {
        var prev = this.context;
        this.context = Objects.requireNonNull(context, "Context cannot be null");
        return prev;
    }
}

package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import org.springframework.data.jpa.domain.Specification;

/**
 * Spring Data JPA implementation of the Condition interface.
 * <p>
 * This adapter wraps Spring JPA {@link Specification} objects and provides
 * logical combination operations (AND, OR, NOT) while maintaining compatibility
 * with the FilterQL condition framework.
 * </p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Seamless integration with Spring Data JPA Specifications</li>
 *   <li>Type-safe condition combinations</li>
 *   <li>Immutable condition objects (operations return new instances)</li>
 *   <li>Full support for complex boolean logic</li>
 * </ul>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * 
 * <p><em>Basic Usage:</em></p>
 * <pre>{@code
 * // Create specifications
 * Specification<User> nameSpec = (root, query, cb) -> 
 *     cb.like(root.get("name"), "John%");
 * Specification<User> ageSpec = (root, query, cb) -> 
 *     cb.greaterThan(root.get("age"), 25);
 * 
 * // Wrap in FilterConditions
 * FilterCondition<User> nameCondition = new FilterCondition<>(nameSpec);
 * FilterCondition<User> ageCondition = new FilterCondition<>(ageSpec);
 * 
 * // Combine using logical operators
 * Condition combined = nameCondition.and(ageCondition);
 * // Result: name LIKE 'John%' AND age > 25
 * }</pre>
 * 
 * <p><em>Complex Combinations:</em></p>
 * <pre>{@code
 * FilterCondition<User> activeCondition = new FilterCondition<>(activeSpec);
 * FilterCondition<User> premiumCondition = new FilterCondition<>(premiumSpec);
 * FilterCondition<User> deletedCondition = new FilterCondition<>(deletedSpec);
 * 
 * // Build: (active OR premium) AND NOT deleted
 * Condition result = activeCondition
 *     .or(premiumCondition)
 *     .and(deletedCondition.not());
 * }</pre>
 * 
 * <p><em>Integration with Repository:</em></p>
 * <pre>{@code
 * // Use in Spring Data JPA repository
 * @Repository
 * public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
 * }
 * 
 * // Service usage
 * @Service
 * public class UserService {
 *     @Autowired
 *     private UserRepository userRepository;
 *     
 *     public List<User> findUsers(Condition condition) {
 *         if (!(condition instanceof FilterCondition<?>)) {
 *             throw new IllegalArgumentException("Unsupported condition type");
 *         }
 *         @SuppressWarnings("unchecked")
 *         FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
 *         Specification<User> spec = filterCondition.getSpecification();
 *         return userRepository.findAll(spec);
 *     }
 * }
 * }</pre>
 * 
 * <p><strong>Thread Safety:</strong></p>
 * <p>FilterCondition instances are immutable and thread-safe. All combination operations
 * return new instances without modifying the original conditions.</p>
 * 
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li>Condition combinations are lightweight (no deep copying)</li>
 *   <li>Underlying Specifications are composed efficiently</li>
 *   <li>JPA query generation is deferred until execution</li>
 * </ul>
 * 
 * @param <T> The entity type this condition applies to
 * @see Condition
 * @see org.springframework.data.jpa.domain.Specification
 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor
 * @author Frank KOSSI
 * @since 1.0
 */
public class FilterCondition<T> implements Condition {
    private final Specification<T> specification;

    /**
     * Constructs a new FilterCondition wrapping a Spring JPA Specification.
     * <p>
     * The provided specification will be used to generate JPA predicates
     * when this condition is evaluated in a query context.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Create a specification for active users
     * Specification<User> activeSpec = (root, query, cb) -> 
     *     cb.equal(root.get("status"), UserStatus.ACTIVE);
     * 
     * // Wrap in FilterCondition
     * FilterCondition<User> activeCondition = new FilterCondition<>(activeSpec);
     * }</pre>
     *
     * @param specification The Spring JPA specification to wrap. Must not be null.
     * @throws NullPointerException if specification is null
     */
    public FilterCondition(Specification<T> specification) {
        this.specification = specification;
    }

    /**
     * {@inheritDoc}
     *
     * @param other The other condition to combine with
     * @return A new condition representing the AND combination
     * @throws IllegalArgumentException if the other condition is not a Spring condition
     */
    @Override
    public Condition and(Condition other) {
        if (!(other instanceof FilterCondition<?>)) {
            throw new IllegalArgumentException("Cannot combine with non-Spring condition");
        }

        FilterCondition<T> otherSpring = (FilterCondition<T>) other;
        return new FilterCondition<>(Specification.where(specification).and(otherSpring.specification));
    }

    /**
     * {@inheritDoc}
     *
     * @param other The other condition to combine with
     * @return A new condition representing the OR combination
     * @throws IllegalArgumentException if the other condition is not a Spring condition
     */
    @Override
    public Condition or(Condition other) {
        if (!(other instanceof FilterCondition<?>)) {
            throw new IllegalArgumentException("Cannot combine with non-Spring condition");
        }
        
        FilterCondition<T> otherSpring = (FilterCondition<T>) other;
        return new FilterCondition<>(Specification.where(specification).or(otherSpring.specification));
    }

    /**
     * {@inheritDoc}
     *
     * @return A new condition representing the negation of this condition
     */
    @Override
    public Condition not() {
        return new FilterCondition<>(Specification.not(specification));
    }

    /**
     * Gets the underlying Spring JPA specification.
     * <p>
     * Returns the wrapped specification that can be used directly with
     * Spring Data JPA repositories or criteria queries.
     * </p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * FilterCondition<User> condition = // ... obtained from context
     * Specification<User> spec = condition.getSpecification();
     * 
     * // Use with repository
     * List<User> users = userRepository.findAll(spec);
     * 
     * // Or use with criteria API
     * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
     * CriteriaQuery<User> query = cb.createQuery(User.class);
     * Root<User> root = query.from(User.class);
     * query.where(spec.toPredicate(root, query, cb));
     * }</pre>
     *
     * @return The Spring JPA specification wrapped by this condition
     */
    public Specification<T> getSpecification() {
        return specification;
    }
}





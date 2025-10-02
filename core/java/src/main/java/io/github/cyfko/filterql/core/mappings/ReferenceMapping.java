package io.github.cyfko.filterql.core.mappings;

/**
 * Marker interface representing a mapping between a logical property reference
 * (usually an enum constant) and its representation in the target entity of type {@code E}.
 * <p>
 * A logical reference is a stable key defined in an enum, pointing to
 * either a direct or nested property of an entity. This mapping describes
 * how that reference translates technically in JPA (path navigation, Specification, etc.).
 * </p>
 * 
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Provide abstraction between logical property names and technical implementation</li>
 *   <li>Enable flexible mapping strategies (simple paths, complex specifications, etc.)</li>
 *   <li>Support different query execution technologies through adapter pattern</li>
 *   <li>Maintain type safety while allowing implementation flexibility</li>
 * </ul>
 * 
 * <p><strong>Implementation Examples:</strong></p>
 * <pre>{@code
 * // 1. Simple path mapping
 * public enum UserPropertyRef implements PropertyReference {
 *     NAME(String.class, OperatorUtils.FOR_TEXT),
 *     EMAIL(String.class, OperatorUtils.FOR_TEXT);
 *     
 *     // Implementation details...
 * }
 * 
 * // Usage in mapping function:
 * Function<UserPropertyRef, Object> mapping = ref -> switch (ref) {
 *     case NAME -> "name";           // Direct path to User.name
 *     case EMAIL -> "email";         // Direct path to User.email
 * };
 * 
 * // 2. Complex specification mapping
 * public class FullNameSpecificationMapping implements PredicateResolverMapping<User, UserPropertyRef> {
 *     @Override
 *     public PredicateResolver<User> resolve() {
 *         return (root, query, cb) -> {
 *             // Complex logic combining firstName and lastName
 *             return cb.or(
 *                 cb.like(root.get("firstName"), (String) definition.getValue()),
 *                 cb.like(root.get("lastName"), (String) definition.getValue())
 *             );
 *         };
 *     }
 * }
 * 
 * // Usage in mapping function:
 * Function<UserPropertyRef, Object> mapping = ref -> switch (ref) {
 *     case NAME -> "firstName";       // Simple mapping
 *     case FULL_NAME -> new FullNameSpecificationMapping(); // Complex mapping
 * };
 * }</pre>
 * 
 * <p><strong>Design Benefits:</strong></p>
 * <ul>
 *   <li><strong>Flexibility:</strong> Same logical property can map to different technical implementations</li>
 *   <li><strong>Maintainability:</strong> Changes to entity structure don't affect API consumers</li>
 *   <li><strong>Testability:</strong> Different mapping strategies for testing vs. production</li>
 *   <li><strong>Technology Independence:</strong> Abstract away JPA, SQL, or other query technologies</li>
 * </ul>
 * 
 * <p>Sub-interfaces like {@link PredicateResolverMapping} provide
 * concrete strategies for that translation.</p>
 *
 * @param <E> the entity type this mapping applies to
 * @see PredicateResolverMapping
 * @see io.github.cyfko.filterql.core.validation.PropertyReference
 * @author Frank KOSSI
 * @since 1.0
 */
public interface ReferenceMapping<E> {
}

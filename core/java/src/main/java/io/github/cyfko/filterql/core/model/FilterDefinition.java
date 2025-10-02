package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;

/**
 * Represents a single filter definition with a property reference,
 * an operator, and a value.
 * <p>
 * The reference property {@code ref} must be an enum implementing {@link PropertyReference}
 * to ensure type safety and performance.
 * </p>
 * <p>
 * The filter definition consists of:
 * </p>
 * <ul>
 *   <li>{@code ref}: the property reference (enum implementing {@link PropertyReference})</li>
 *   <li>{@code operator}: the comparison or logical operator</li>
 *   <li>{@code value}: the value used in the filter, which may be a simple object or a collection depending on the operator</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Assume an enum implementing PropertyReference:
 * public enum UserPropertyRef implements PropertyReference {
 *     NAME, EMAIL, AGE;
 *
 *     // Implement PropertyReference methods here
 * }
 *
 * // Create filter definitions:
 * FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
 *     UserPropertyRef.NAME,
 *     Op.LIKE,
 *     "John%"
 * );
 *
 * FilterDefinition<UserPropertyRef> emailFilter = new FilterDefinition<>(
 *     UserPropertyRef.EMAIL,
 *     Op.EQUALS,
 *     "alice@example.com"
 * );
 *
 * FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
 *     UserPropertyRef.AGE,
 *     Op.GREATER_THAN_OR_EQUAL,
 *     18
 * );
 *
 * System.out.println(nameFilter);
 * System.out.println(emailFilter);
 * System.out.println(ageFilter);
 * }</pre>
 *
 * @param <P> type of the reference property (enum implementing {@link PropertyReference})
 * @param ref the property reference (enum implementing {@link PropertyReference})
 * @param operator the comparison or logical operator to apply
 * @param value the value used in the filter condition
 * @author Frank KOSSI
 * @since 2.0.0
 */
public record FilterDefinition<P extends Enum<P> & PropertyReference>(P ref, Op operator, Object value) {

    @Override
    public String toString() {
        return String.format("FilterDefinition{ref=%s, operator=%s, value=%s}",
                ref, operator, value);
    }
}

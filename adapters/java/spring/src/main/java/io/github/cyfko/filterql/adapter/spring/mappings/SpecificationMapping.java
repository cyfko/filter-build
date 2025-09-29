package io.github.cyfko.filterql.adapter.spring.mappings;

import io.github.cyfko.filterql.adapter.spring.ReferenceMapping;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

/**
 * Defines a mapping that can transform a logical reference into a JPA
 * {@link org.springframework.data.jpa.domain.Specification} for entity type {@code E}.
 *
 * <p>The purpose is to take a logical reference (enum constant), an operator,
 * and a value, and produce a Specification that can be executed on the entity.</p>
 *
 * <p>Example:
 * <ul>
 *   <li>Enum: USER_NAME</li>
 *   <li>Operator: EQUAL</li>
 *   <li>Value: "Alice"</li>
 *   <li>Result: Specification filtering "User.name = 'Alice'"</li>
 * </ul>
 * </p>
 *
 * @param <E> the entity type the Specification applies to
 */
public interface SpecificationMapping<E> extends ReferenceMapping<E> {

    /**
     * Builds a {@link Specification} for a given entity type based on a logical reference,
     * an operator, and a comparison value.
     *
     * @param ref   the logical reference (enum constant) representing the targeted property
     * @param op    the comparison operator (e.g., EQUAL, LIKE, GREATER_THAN)
     * @param value the value to compare against
     * @param <E>   the entity type the Specification applies to
     * @param <P>   the enum type representing logical property references
     * @return a Specification ready to be combined or executed
     */
    <P extends Enum<P> & PropertyRef & ReferenceMapping<E>>
    Specification<E> toSpecification(@NonNull P ref, @NonNull Operator op, Object value);
}


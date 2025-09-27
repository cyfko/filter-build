package io.github.cyfko.filterql.core;

import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;

/**
 * Interface representing an expression tree parsed from a DSL expression.
 * <p>
 * An instance of {@code FilterTree} can generate a global condition by resolving
 * filter references according to the provided context.
 * </p>
 *
 * <p>This abstraction allows representing complex boolean expressions
 * composed of multiple combined filters.</p>
 *
 * @author Frank KOSSI
 * @since 1.0
 */
public interface FilterTree {

    /**
     * Generates a global condition by resolving all filter references
     * using the given context.
     *
     * @param context The context providing the conditions corresponding to filter keys.
     * @return A {@link Condition} representing the entire filter tree.
     * @throws FilterValidationException If filter validation fails or the condition cannot be generated.
     * @throws DSLSyntaxException If the filter combination refers to an undefined filter.
     */
    Condition generate(Context context) throws FilterValidationException, DSLSyntaxException;
}


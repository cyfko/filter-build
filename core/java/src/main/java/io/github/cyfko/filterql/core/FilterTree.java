package io.github.cyfko.filterql.core;

import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;

/**
 * Interface representing an expression tree parsed from a DSL expression.
 * <p>
 * An instance of {@code FilterTree} can generate a global condition by resolving
 * filter references according to the provided context. This abstraction allows
 * representing complex boolean expressions composed of multiple combined filters.
 * </p>
 * 
 * <p><strong>DSL Expression Examples:</strong></p>
 * <ul>
 *   <li>{@code "filter1"} - Simple filter reference</li>
 *   <li>{@code "filter1 & filter2"} - AND combination</li>
 *   <li>{@code "filter1 | filter2"} - OR combination</li>
 *   <li>{@code "!filter1"} - NOT operation</li>
 *   <li>{@code "(filter1 & filter2) | !filter3"} - Complex expression with precedence</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Parse DSL expression
 * Parser parser = new DSLParser();
 * FilterTree tree = parser.parse("(nameFilter & ageFilter) | statusFilter");
 * 
 * // Prepare context with filter definitions
 * Context context = new FilterContext<>(User.class, UserPropertyRef.class, mapping);
 * context.addCondition("nameFilter", nameFilterDef);
 * context.addCondition("ageFilter", ageFilterDef);
 * context.addCondition("statusFilter", statusFilterDef);
 * 
 * // Generate the combined condition
 * Condition result = tree.generate(context);
 * // Result represents: (name LIKE 'John%' AND age > 25) OR status = 'ACTIVE'
 * }</pre>
 * 
 * <p><strong>Implementation Notes:</strong></p>
 * <ul>
 *   <li>Filter trees are immutable once parsed</li>
 *   <li>The same tree can be reused with different contexts</li>
 *   <li>Validation occurs during generation, not parsing</li>
 *   <li>Supports operator precedence: NOT > AND > OR</li>
 * </ul>
 *
 * @see Parser
 * @see Context
 * @see Condition
 * @author Frank KOSSI
 * @since 1.0
 */
public interface FilterTree {

    /**
     * Generates a global condition by resolving all filter references using the given context.
     * <p>
     * This method traverses the expression tree and resolves each filter reference
     * to its corresponding condition from the context, then combines them according
     * to the boolean operators in the tree structure.
     * </p>
     * 
     * <p><strong>Process:</strong></p>
     * <ol>
     *   <li>Traverse the tree in post-order (leaves first)</li>
     *   <li>Resolve filter references using {@link Context#getCondition(String)}</li>
     *   <li>Apply boolean operators (AND, OR, NOT) to combine conditions</li>
     *   <li>Return the root condition representing the entire expression</li>
     * </ol>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Given DSL: "(active & premium) | !deleted"
     * // And context contains:
     * //   "active" -> status = 'ACTIVE'
     * //   "premium" -> type = 'PREMIUM' 
     * //   "deleted" -> deleted = true
     * 
     * Condition result = filterTree.generate(context);
     * // Result: (status = 'ACTIVE' AND type = 'PREMIUM') OR NOT(deleted = true)
     * }</pre>
     *
     * @param context The context providing the conditions corresponding to filter keys
     * @return A {@link Condition} representing the entire filter tree
     * @throws FilterValidationException If filter validation fails during condition resolution
     * @throws DSLSyntaxException If the filter combination refers to an undefined filter key
     * @throws NullPointerException if context is null
     */
    Condition generate(Context context) throws FilterValidationException, DSLSyntaxException;
}


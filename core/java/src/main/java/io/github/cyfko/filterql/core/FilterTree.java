package io.github.cyfko.filterql.core;

/**
 * Interface representing a parsed DSL expression tree.
 * The FilterTree can generate a global condition by resolving filter references
 * through the provided context.
 */
public interface FilterTree {
    
    /**
     * Generates a global condition by resolving all filter references through the context.
     * 
     * @param context The context providing conditions for filter keys
     * @return A condition representing the entire filter tree
     */
    Condition generate(Context context);
}

package io.github.cyfko.dynamicfilter.core;

import io.github.cyfko.dynamicfilter.core.exception.DSLSyntaxException;

/**
 * Interface for parsing DSL expressions into FilterTree structures.
 * This is the core parsing contract that all implementations must follow.
 */
public interface Parser {
    
    /**
     * Parses a DSL expression string into a FilterTree.
     * 
     * @param dslExpression The DSL expression to parse (e.g., "(f1 &amp; f2) | !f3")
     * @return A FilterTree representing the parsed expression
     * @throws DSLSyntaxException if the DSL expression is invalid
     */
    FilterTree parse(String dslExpression) throws DSLSyntaxException;
}

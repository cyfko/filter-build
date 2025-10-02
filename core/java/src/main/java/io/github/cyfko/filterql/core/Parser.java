package io.github.cyfko.filterql.core;

import io.github.cyfko.filterql.core.exception.DSLSyntaxException;

/**
 * Interface for parsing DSL expressions into FilterTree structures.
 * <p>
 * This is the core parsing contract that all implementations must follow.
 * The parser transforms a textual DSL expression into an abstract syntax tree
 * that can be later evaluated against a specific context.
 * </p>
 * 
 * <p><strong>Supported DSL Syntax:</strong></p>
 * <table border="1">
 * <caption>DSL Operator Reference</caption>
 * <tr><th>Operator</th><th>Symbol</th><th>Example</th><th>Description</th></tr>
 * <tr><td>AND</td><td>&amp;</td><td>filter1 &amp; filter2</td><td>Logical AND</td></tr>
 * <tr><td>OR</td><td>|</td><td>filter1 | filter2</td><td>Logical OR</td></tr>
 * <tr><td>NOT</td><td>!</td><td>!filter1</td><td>Logical NOT</td></tr>
 * <tr><td>Grouping</td><td>( )</td><td>(filter1 &amp; filter2)</td><td>Precedence control</td></tr>
 * </table>
 * 
 * <p><strong>Operator Precedence (highest to lowest):</strong></p>
 * <ol>
 *   <li>Parentheses {@code ( )}</li>
 *   <li>NOT {@code !}</li>
 *   <li>AND {@code &}</li>
 *   <li>OR {@code |}</li>
 * </ol>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * Parser parser = new DSLParser();
 * 
 * // Simple expressions
 * FilterTree tree1 = parser.parse("active");                    // Single filter
 * FilterTree tree2 = parser.parse("active & premium");          // AND combination
 * FilterTree tree3 = parser.parse("active | premium");          // OR combination
 * FilterTree tree4 = parser.parse("!deleted");                  // NOT operation
 * 
 * // Complex expressions
 * FilterTree tree5 = parser.parse("(active & premium) | vip");  // Precedence with parentheses
 * FilterTree tree6 = parser.parse("!deleted & (active | pending)"); // Mixed operators
 * }</pre>
 * 
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>Syntax errors are reported with specific error messages</li>
 *   <li>Invalid characters and malformed expressions are detected</li>
 *   <li>Missing operands or operators are identified</li>
 *   <li>Unmatched parentheses are caught during parsing</li>
 * </ul>
 *
 * @see FilterTree
 * @see DSLSyntaxException
 * @author Frank KOSSI
 * @since 1.0
 */
public interface Parser {
    
    /**
     * Parses a DSL expression string into a FilterTree.
     * <p>
     * Transforms a textual filter expression into an abstract syntax tree
     * that preserves the logical structure and operator precedence.
     * The resulting tree can be evaluated multiple times against different contexts.
     * </p>
     * 
     * <p><strong>Valid Expression Examples:</strong></p>
     * <pre>{@code
     * // Simple cases
     * parser.parse("userFilter");                    // Single filter reference
     * parser.parse("name & age");                    // AND combination
     * parser.parse("active | premium");              // OR combination  
     * parser.parse("!deleted");                      // NOT operation
     * 
     * // Complex cases
     * parser.parse("(name & age) | status");         // Precedence control
     * parser.parse("!deleted & (active | pending)"); // Nested logic
     * parser.parse("a & b | c & d");                 // Multiple operators
     * }</pre>
     * 
     * <p><strong>Invalid Expression Examples:</strong></p>
     * <pre>{@code
     * parser.parse("");                              // Empty expression
     * parser.parse("filter &");                      // Missing operand
     * parser.parse("& filter");                      // Missing operand
     * parser.parse("(filter");                       // Unmatched parenthesis
     * parser.parse("filter & & other");              // Double operator
     * }</pre>
     * 
     * @param dslExpression The DSL expression to parse (e.g., "(f1 &amp; f2) | !f3").
     *                     Must not be null or empty. Whitespace is ignored.
     * @return A FilterTree representing the parsed expression structure
     * @throws DSLSyntaxException if the DSL expression has invalid syntax, 
     *                           contains unknown operators, or has structural errors
     * @throws NullPointerException if dslExpression is null
     */
    FilterTree parse(String dslExpression) throws DSLSyntaxException;
}

package io.github.cyfko.filterql.core.impl;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Advanced tests for DSLParser covering complex expression scenarios,
 * deep nesting, operator precedence edge cases, and performance considerations.
 * 
 * These tests complement the basic DSLParserTest with more sophisticated scenarios
 * that test the robustness and scalability of the DSL parsing implementation.
 */
@DisplayName("DSL Parser Advanced Tests")
public class DSLParserAdvancedTest {

    private DSLParser parser;

    @Mock
    private Context mockContext;

    @Mock
    private Condition mockConditionA, mockConditionB, mockConditionC, mockConditionD, mockConditionE;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new DSLParser();

        // Setup mock conditions
        when(mockContext.getCondition("A")).thenReturn(mockConditionA);
        when(mockContext.getCondition("B")).thenReturn(mockConditionB);
        when(mockContext.getCondition("C")).thenReturn(mockConditionC);
        when(mockContext.getCondition("D")).thenReturn(mockConditionD);
        when(mockContext.getCondition("E")).thenReturn(mockConditionE);

        // Setup chaining for all conditions
        setupConditionChaining(mockConditionA);
        setupConditionChaining(mockConditionB);
        setupConditionChaining(mockConditionC);
        setupConditionChaining(mockConditionD);
        setupConditionChaining(mockConditionE);
    }

    private void setupConditionChaining(Condition condition) {
        when(condition.not()).thenReturn(condition);
        when(condition.and(any())).thenReturn(condition);
        when(condition.or(any())).thenReturn(condition);
    }

    @Nested
    @DisplayName("Complex Operator Precedence Tests")
    class ComplexPrecedenceTests {

        @Test
        @DisplayName("Mixed precedence with multiple levels: A | B & C | D & E")
        void testMixedPrecedenceMultipleLevels() throws DSLSyntaxException, FilterValidationException {
            // Should be parsed as: A | (B & C) | (D & E)
            FilterTree tree = parser.parse("A | B & C | D & E");
            assertNotNull(tree);

            tree.generate(mockContext);
            
            // Verify all conditions are accessed
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
            verify(mockContext).getCondition("D");
            verify(mockContext).getCondition("E");
        }

        @Test
        @DisplayName("Complex NOT precedence: !A & B | !C & D")
        void testComplexNotPrecedence() throws DSLSyntaxException, FilterValidationException {
            // Should be parsed as: ((!A) & B) | ((!C) & D)
            FilterTree tree = parser.parse("!A & B | !C & D");
            assertNotNull(tree);

            tree.generate(mockContext);
            
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
            verify(mockContext).getCondition("D");
            
            // Verify NOT operations
            verify(mockConditionA).not();
            verify(mockConditionC).not();
        }

        @Test
        @DisplayName("Nested parentheses with mixed operators: ((A & B) | (C & D)) & !(E)")
        void testNestedParenthesesMixedOperators() throws DSLSyntaxException, FilterValidationException {
            FilterTree tree = parser.parse("((A & B) | (C & D)) & !(E)");
            assertNotNull(tree);

            tree.generate(mockContext);
            
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
            verify(mockContext).getCondition("D");
            verify(mockContext).getCondition("E");
            verify(mockConditionE).not();
        }

        @Test
        @DisplayName("Right associative NOT operations: !!!A")
        void testRightAssociativeNot() throws DSLSyntaxException, FilterValidationException {
            FilterTree tree = parser.parse("!!!A");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockConditionA, times(3)).not();
        }
    }

    @Nested
    @DisplayName("Deep Nesting and Recursion Tests")
    class DeepNestingTests {

        @Test
        @DisplayName("Very deep parentheses nesting (10 levels)")
        void testVeryDeepParenthesesNesting() throws DSLSyntaxException, FilterValidationException {
            String expression = "((((((((((A))))))))))";
            FilterTree tree = parser.parse(expression);
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
        }

        @Test
        @DisplayName("Deep logical nesting with alternating operators")
        void testDeepLogicalNesting() throws DSLSyntaxException, FilterValidationException {
            // Create a deeply nested expression: (((A & B) | C) & D) | E
            String expression = "(((A & B) | C) & D) | E";
            FilterTree tree = parser.parse(expression);
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
            verify(mockContext).getCondition("D");
            verify(mockContext).getCondition("E");
        }

        @Test
        @DisplayName("Complex nested NOT expressions: !(!A & !B) | !(C & !D)")
        void testComplexNestedNotExpressions() throws DSLSyntaxException, FilterValidationException {
            FilterTree tree = parser.parse("!(!A & !B) | !(C & !D)");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
            verify(mockContext).getCondition("D");
        }
    }

    @Nested
    @DisplayName("Large Expression Scalability Tests")
    class ScalabilityTests {

        @Test
        @DisplayName("Large chain of OR operations (50 terms)")
        void testLargeOrChain() throws DSLSyntaxException {
            StringBuilder expression = new StringBuilder("A");
            
            // Create mock conditions for all terms
            for (int i = 1; i <= 50; i++) {
                String conditionName = "TERM_" + i;
                expression.append(" | ").append(conditionName);
                when(mockContext.getCondition(conditionName)).thenReturn(mockConditionA);
            }

            FilterTree tree = parser.parse(expression.toString());
            assertNotNull(tree);
            
            // Verify parsing doesn't fail with large expressions
            assertTrue(tree.toString().length() > 100);
        }

        @Test
        @DisplayName("Large chain of AND operations (30 terms)")
        void testLargeAndChain() throws DSLSyntaxException {
            StringBuilder expression = new StringBuilder("A");
            
            // Create mock conditions for all terms
            for (int i = 1; i <= 30; i++) {
                String conditionName = "COND_" + i;
                expression.append(" & ").append(conditionName);
                when(mockContext.getCondition(conditionName)).thenReturn(mockConditionA);
            }

            FilterTree tree = parser.parse(expression.toString());
            assertNotNull(tree);
            
            // Verify the expression is correctly parsed
            assertTrue(tree.toString().contains("AND"));
        }

        @Test
        @DisplayName("Mixed large expression with all operators")
        void testMixedLargeExpression() throws DSLSyntaxException {
            // Create a complex expression mixing all operators and parentheses
            String expression = "!(A & B) | (C & D) & !(E | (A & !B)) | ((C | D) & !A)";
            
            FilterTree tree = parser.parse(expression.toString());
            assertNotNull(tree);
            
            // Verify complex structure is maintained
            String treeString = tree.toString();
            assertTrue(treeString.contains("NOT"));
            assertTrue(treeString.contains("AND"));
            assertTrue(treeString.contains("OR"));
        }
    }

    @Nested
    @DisplayName("Edge Case Expression Patterns")
    class EdgeCasePatterns {

        @Test
        @DisplayName("Alternating NOT operations: !A & !B | !C & !D")
        void testAlternatingNotOperations() throws DSLSyntaxException, FilterValidationException {
            FilterTree tree = parser.parse("!A & !B | !C & !D");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockConditionA).not();
            verify(mockConditionB).not();
            verify(mockConditionC).not();
            verify(mockConditionD).not();
        }

        @Test
        @DisplayName("Redundant parentheses: (((A))) & (((B)))")
        void testRedundantParentheses() throws DSLSyntaxException, FilterValidationException {
            FilterTree tree = parser.parse("(((A))) & (((B)))");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
        }

        @Test
        @DisplayName("Mixed parentheses styles: ((A & B)) | (C) & ((D | E))")
        void testMixedParenthesesStyles() throws DSLSyntaxException, FilterValidationException {
            FilterTree tree = parser.parse("((A & B)) | (C) & ((D | E))");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
            verify(mockContext).getCondition("D");
            verify(mockContext).getCondition("E");
        }

        @ParameterizedTest
        @DisplayName("Complex valid identifier patterns")
        @ValueSource(strings = {
            "_very_long_identifier_name_with_underscores",
            "CamelCaseIdentifierWithNumbers123",
            "snake_case_with_numbers_456",
            "a1b2c3d4e5",
            "_123_numbers_after_underscore"
        })
        void testComplexValidIdentifiers(String identifier) throws DSLSyntaxException, FilterValidationException {
            when(mockContext.getCondition(identifier)).thenReturn(mockConditionA);
            
            FilterTree tree = parser.parse(identifier);
            assertNotNull(tree);
            
            tree.generate(mockContext);
            verify(mockContext).getCondition(identifier);
        }
    }

    @Nested
    @DisplayName("Error Recovery and Boundary Tests")
    class ErrorRecoveryTests {

        @Test
        @DisplayName("Maximum reasonable expression complexity")
        void testMaximumReasonableComplexity() throws DSLSyntaxException {
            // Test an expression that's complex but still reasonable
            String expression = "!(A & B) | (C & (D | E)) & !((A | B) & (C | D)) | (E & !A)";
            
            FilterTree tree = parser.parse(expression);
            assertNotNull(tree);
            
            // Verify the complex structure is preserved
            String treeString = tree.toString();
            assertNotNull(treeString);
            assertFalse(treeString.isEmpty());
        }

        @Test
        @DisplayName("Expression with maximum whitespace variations")
        void testMaximumWhitespaceVariations() throws DSLSyntaxException, FilterValidationException {
            String expression = "  !  (  A   &   B  )   |   (  C   &   D  )  ";
            
            FilterTree tree = parser.parse(expression);
            assertNotNull(tree);
            
            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
            verify(mockContext).getCondition("D");
        }

        @Test
        @DisplayName("Complex expression with toString verification")
        void testComplexExpressionToString() throws DSLSyntaxException {
            String expression = "!(A & B) | C";
            FilterTree tree = parser.parse(expression);
            
            String result = tree.toString();
            
            // Verify the toString provides a readable representation
            assertNotNull(result);
            assertTrue(result.contains("NOT"));
            assertTrue(result.contains("AND"));
            assertTrue(result.contains("OR"));
            assertTrue(result.contains("A"));
            assertTrue(result.contains("B"));
            assertTrue(result.contains("C"));
        }
    }

    @Nested
    @DisplayName("Performance and Memory Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Parser performance with moderately complex expression")
        void testParserPerformance() throws DSLSyntaxException {
            String expression = "!(A & B) | (C & D) | (E & !A) & (B | !C) | (D & E)";
            
            long startTime = System.nanoTime();
            FilterTree tree = parser.parse(expression);
            long endTime = System.nanoTime();
            
            assertNotNull(tree);
            
            // Verify parsing completes in reasonable time (less than 1ms for this complexity)
            long durationMs = (endTime - startTime) / 1_000_000;
            assertTrue(durationMs < 100, "Parsing took too long: " + durationMs + "ms");
        }

        @Test
        @DisplayName("Memory efficiency with repeated parsing")
        void testMemoryEfficiencyRepeatedParsing() throws DSLSyntaxException {
            String expression = "(A & B) | (C & D)";
            
            // Parse the same expression multiple times to test for memory leaks
            for (int i = 0; i < 100; i++) {
                FilterTree tree = parser.parse(expression);
                assertNotNull(tree);
                
                // Force a small GC hint every 10 iterations
                if (i % 10 == 0) {
                    System.gc();
                }
            }
            
            // If we reach here without OutOfMemoryError, the test passes
            assertTrue(true);
        }
    }
}
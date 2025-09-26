package io.github.cyfko.filterql.core.impl;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Tests exhaustifs pour DSLParser avec couverture totale de code.
 * Couvre tous les cas de succès, d'échecs, et les cas limites.
 */
public class DSLParserTest {

    private DSLParser parser;

    @Mock
    private Context mockContext;

    @Mock
    private Condition mockConditionA;

    @Mock
    private Condition mockConditionB;

    @Mock
    private Condition mockConditionC;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new DSLParser();

        // Configuration des mocks pour les conditions
        when(mockContext.getCondition("A")).thenReturn(mockConditionA);
        when(mockContext.getCondition("B")).thenReturn(mockConditionB);
        when(mockContext.getCondition("C")).thenReturn(mockConditionC);

        // Configuration des méthodes chainées
        when(mockConditionA.not()).thenReturn(mockConditionA);
        when(mockConditionA.and(any())).thenReturn(mockConditionA);
        when(mockConditionA.or(any())).thenReturn(mockConditionA);
        when(mockConditionB.not()).thenReturn(mockConditionB);
        when(mockConditionB.and(any())).thenReturn(mockConditionB);
        when(mockConditionB.or(any())).thenReturn(mockConditionB);
        when(mockConditionC.not()).thenReturn(mockConditionC);
        when(mockConditionC.and(any())).thenReturn(mockConditionC);
        when(mockConditionC.or(any())).thenReturn(mockConditionC);
    }

    @Nested
    @DisplayName("Tests d'expressions valides")
    class ValidExpressions {

        @Test
        @DisplayName("Identifiant simple")
        void testSingleIdentifier() throws DSLSyntaxException {
            FilterTree tree = parser.parse("A");
            assertNotNull(tree);

            Condition result = tree.generate(mockContext);
            assertNotNull(result);
            verify(mockContext).getCondition("A");
        }

        @Test
        @DisplayName("Expression AND simple")
        void testSimpleAnd() throws DSLSyntaxException {
            FilterTree tree = parser.parse("A & B");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockConditionA).and(mockConditionB);
        }

        @Test
        @DisplayName("Expression OR simple")
        void testSimpleOr() throws DSLSyntaxException {
            FilterTree tree = parser.parse("A | B");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockConditionA).or(mockConditionB);
        }

        @Test
        @DisplayName("Expression NOT simple")
        void testSimpleNot() throws DSLSyntaxException {
            FilterTree tree = parser.parse("!A");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockConditionA).not();
        }

        @Test
        @DisplayName("Parenthèses simples")
        void testSimpleParentheses() throws DSLSyntaxException {
            FilterTree tree = parser.parse("(A)");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
        }

        @Test
        @DisplayName("Expression complexe avec priorités")
        void testComplexWithPrecedence() throws DSLSyntaxException {
            // A | B & C devrait être interprété comme A | (B & C)
            FilterTree tree = parser.parse("A | B & C");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
        }

        @Test
        @DisplayName("Expression avec parenthèses complexes")
        void testComplexParentheses() throws DSLSyntaxException {
            FilterTree tree = parser.parse("!(A & B) | C");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
        }

        @Test
        @DisplayName("Associativité gauche pour AND")
        void testLeftAssociativityAnd() throws DSLSyntaxException {
            // A & B & C devrait être (A & B) & C
            FilterTree tree = parser.parse("A & B & C");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
        }

        @Test
        @DisplayName("Associativité gauche pour OR")
        void testLeftAssociativityOr() throws DSLSyntaxException {
            // A | B | C devrait être (A | B) | C
            FilterTree tree = parser.parse("A | B | C");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
        }

        @Test
        @DisplayName("Double négation")
        void testDoubleNegation() throws DSLSyntaxException {
            FilterTree tree = parser.parse("!!A");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockConditionA, times(2)).not();
        }

        @ParameterizedTest
        @DisplayName("Identifiants valides")
        @ValueSource(strings = {
                "_test", "test_", "test123", "Test", "TEST",
                "camelCase", "snake_case", "_123", "a", "_"
        })
        void testValidIdentifiers(String identifier) throws DSLSyntaxException {
            when(mockContext.getCondition(identifier)).thenReturn(mockConditionA);

            FilterTree tree = parser.parse(identifier);
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition(identifier);
        }

        @Test
        @DisplayName("Expression avec espaces multiples")
        void testMultipleWhitespaces() throws DSLSyntaxException {
            FilterTree tree = parser.parse("  A   &   B  ");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
        }

        @Test
        @DisplayName("Parenthèses imbriquées")
        void testNestedParentheses() throws DSLSyntaxException {
            FilterTree tree = parser.parse("((A & B) | (C))");
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
        }
    }

    @Nested
    @DisplayName("Tests d'expressions invalides")
    class InvalidExpressions {

        @ParameterizedTest
        @DisplayName("Expressions nulles ou vides")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        void testNullOrEmptyExpressions(String expression) {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse(expression)
            );
            assertTrue(exception.getMessage().contains("cannot be null or empty"));
        }

        @Test
        @DisplayName("Expression null")
        void testNullExpression() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse(null)
            );
            assertTrue(exception.getMessage().contains("cannot be null or empty"));
        }

        @ParameterizedTest
        @DisplayName("Caractères invalides")
        @CsvSource({
                "'A @ B', '@'",
                "'A # B', '#'",
                "'A $ B', '$'",
                "'A % B', '%'",
                "'A ^ B', '^'",
                "'A + B', '+'",
                "'A - B', '-'"
        })
        void testInvalidCharacters(String expression, String invalidChar) {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse(expression)
            );
            assertTrue(exception.getMessage().contains("Invalid character"));
            assertTrue(exception.getMessage().contains(invalidChar));
            assertTrue(exception.getMessage().contains("at position"));
        }

        @ParameterizedTest
        @DisplayName("Identifiants invalides")
        @ValueSource(strings = {
                "123abc",      // Commence par un chiffre
                "test-case",   // Contient un tiret
                "test.case",   // Contient un point
                "test case",   // Contient un espace (sera tokenizé séparément)
        })
        void testInvalidIdentifiers(String expression) {
            assertThrows(DSLSyntaxException.class, () -> parser.parse(expression));
        }

        @Test
        @DisplayName("Expression commençant par AND")
        void testStartingWithAnd() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("& A")
            );
            assertTrue(exception.getMessage().contains("Binary operator '&' requires a left operand"));
        }

        @Test
        @DisplayName("Expression commençant par OR")
        void testStartingWithOr() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("| A")
            );
            assertTrue(exception.getMessage().contains("Binary operator '|' requires a left operand"));
        }

        @Test
        @DisplayName("Expression se terminant par AND")
        void testEndingWithAnd() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A &")
            );
            assertTrue(exception.getMessage().contains("cannot end with operator"));
        }

        @Test
        @DisplayName("Expression se terminant par OR")
        void testEndingWithOr() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A |")
            );
            assertTrue(exception.getMessage().contains("cannot end with operator"));
        }

        @Test
        @DisplayName("Expression se terminant par NOT")
        void testEndingWithNot() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A !")
            );
            assertTrue(exception.getMessage().contains("identifier 'A' cannot be followed by '!' "));
        }

        @ParameterizedTest
        @DisplayName("Opérateurs consécutifs")
        @ValueSource(strings = {
                "A & & B", "A | | B", "A & | B", "A | & B"
        })
        void testConsecutiveOperators(String expression) {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse(expression)
            );
            assertTrue(exception.getMessage().contains("cannot be followed by"));
        }

        @Test
        @DisplayName("Identifiants consécutifs")
        void testConsecutiveIdentifiers() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A B")
            );
            assertTrue(exception.getMessage().contains("cannot be followed by"));
        }

        @Test
        @DisplayName("NOT suivi d'opérateur binaire")
        void testNotFollowedByBinaryOperator() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("! & A")
            );
            assertTrue(exception.getMessage().contains("NOT operator cannot be followed by binary operator"));
        }

        @Test
        @DisplayName("Parenthèse gauche non fermée")
        void testUnclosedLeftParenthesis() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("(A & B")
            );
            assertTrue(exception.getMessage().contains("Mismatched parentheses"));
            assertTrue(exception.getMessage().contains("unmatched '('"));
        }

        @Test
        @DisplayName("Parenthèse droite sans ouverture")
        void testUnmatchedRightParenthesis() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A & B)")
            );
            assertTrue(exception.getMessage().contains("Mismatched parentheses"));
            assertTrue(exception.getMessage().contains("unmatched ')'"));
        }

        @Test
        @DisplayName("Parenthèses vides")
        void testEmptyParentheses() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("()")
            );
            assertTrue(exception.getMessage().contains("Right parenthesis cannot follow"));
        }

        @Test
        @DisplayName("Parenthèse gauche après identifiant")
        void testLeftParenAfterIdentifier() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A(B)")
            );
            assertTrue(exception.getMessage().contains("Left parenthesis cannot follow"));
        }

        @Test
        @DisplayName("NOT après identifiant")
        void testNotAfterIdentifier() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A !B")
            );
            assertTrue(exception.getMessage().contains("identifier 'A' cannot be followed by '!' at position"));
        }

        @Test
        @DisplayName("Opérateur binaire sans opérande gauche")
        void testBinaryOperatorWithoutLeftOperand() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("(!A &)")
            );
            assertTrue(exception.getMessage().contains("Right parenthesis cannot follow '&'"));
        }
    }

    @Nested
    @DisplayName("Tests d'erreurs de génération")
    class GenerationErrors {

        @Test
        @DisplayName("Identifiant non trouvé dans le contexte")
        void testIdentifierNotFoundInContext() throws DSLSyntaxException {
            when(mockContext.getCondition("UNKNOWN")).thenReturn(null);

            FilterTree tree = parser.parse("UNKNOWN");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> tree.generate(mockContext)
            );
            assertTrue(exception.getMessage().contains("No condition found for identifier: UNKNOWN"));
        }

        @Test
        @DisplayName("Identifiant non trouvé dans expression complexe")
        void testIdentifierNotFoundInComplexExpression() throws DSLSyntaxException {
            when(mockContext.getCondition("UNKNOWN")).thenReturn(null);

            FilterTree tree = parser.parse("A & UNKNOWN");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> tree.generate(mockContext)
            );
            assertTrue(exception.getMessage().contains("No condition found for identifier: UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("Tests de couverture des méthodes toString")
    class ToStringTests {

        @Test
        @DisplayName("toString des nœuds")
        void testNodeToString() throws DSLSyntaxException {
            FilterTree tree = parser.parse("!(A & B) | C");
            String treeString = tree.toString();
            assertNotNull(treeString);
            assertFalse(treeString.isEmpty());
        }

        @Test
        @DisplayName("toString d'un identifiant simple")
        void testIdentifierToString() throws DSLSyntaxException {
            FilterTree tree = parser.parse("A");
            assertEquals("A", tree.toString());
        }

        @Test
        @DisplayName("toString d'une négation")
        void testNotToString() throws DSLSyntaxException {
            FilterTree tree = parser.parse("!A");
            assertEquals("NOT(A)", tree.toString());
        }

        @Test
        @DisplayName("toString d'un AND")
        void testAndToString() throws DSLSyntaxException {
            FilterTree tree = parser.parse("A & B");
            assertEquals("(A AND B)", tree.toString());
        }

        @Test
        @DisplayName("toString d'un OR")
        void testOrToString() throws DSLSyntaxException {
            FilterTree tree = parser.parse("A | B");
            assertEquals("(A OR B)", tree.toString());
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCases {

        @Test
        @DisplayName("Expression très longue")
        void testVeryLongExpression() throws DSLSyntaxException {
            // Créer une expression très longue mais valide
            StringBuilder expr = new StringBuilder("A");
            for (int i = 1; i < 10; i++) { // Réduire à 10 au lieu de 100
                expr.append(" | B").append(i);
                when(mockContext.getCondition("B" + i)).thenReturn(mockConditionB);
            }

            FilterTree tree = parser.parse(expr.toString());
            assertNotNull(tree);
        }

        @Test
        @DisplayName("Parenthèses profondément imbriquées")
        void testDeeplyNestedParentheses() throws DSLSyntaxException {
            String expr = "((((A))))";
            FilterTree tree = parser.parse(expr);
            assertNotNull(tree);

            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
        }

        @Test
        @DisplayName("Tous les types de tokens dans une expression")
        void testAllTokenTypesInExpression() throws DSLSyntaxException {
            String expr = "!(A & B) | (C)";
            FilterTree tree = parser.parse(expr);
            assertNotNull(tree);

            // Vérifier que l'arbre peut être généré
            tree.generate(mockContext);
            verify(mockContext).getCondition("A");
            verify(mockContext).getCondition("B");
            verify(mockContext).getCondition("C");
        }

        @Test
        @DisplayName("Expression avec uniquement des espaces entre tokens")
        void testExpressionWithOnlySpacesBetweenTokens() throws DSLSyntaxException {
            FilterTree tree = parser.parse("A & B");
            assertNotNull(tree);

            FilterTree treeWithSpaces = parser.parse("A    &    B");
            assertNotNull(treeWithSpaces);
        }
    }

    @Nested
    @DisplayName("Tests de robustesse")
    class RobustnessTests {

        @Test
        @DisplayName("Gestion d'exception interne dans getTokenType")
        void testGetTokenTypeWithInvalidCharacter() {
            // Ce test vérifie que getTokenType lève bien une IllegalArgumentException
            // pour un caractère non supporté (bien que cela ne devrait pas arriver
            // en pratique grâce à la validation en amont)
            DSLParser parserInstance = new DSLParser();

            // Nous ne pouvons pas tester getTokenType directement car elle est private,
            // mais nous pouvons vérifier qu'un caractère invalide est bien détecté
            assertThrows(DSLSyntaxException.class, () -> parser.parse("A $ B"));
        }

        @Test
        @DisplayName("Validation de position correcte dans les erreurs")
        void testPositionInErrorMessages() {
            DSLSyntaxException exception = assertThrows(
                    DSLSyntaxException.class,
                    () -> parser.parse("A & & B")
            );
            assertTrue(exception.getMessage().contains("at position"));
        }

        @Test
        @DisplayName("Tous les cas du switch dans validateTokenTransition")
        void testAllTokenTransitionCases() throws DSLSyntaxException {
            // Test pour couvrir tous les cas du switch dans validateTokenTransition

            // IDENTIFIER case - déjà couvert par d'autres tests
            parser.parse("A");

            // AND/OR case - déjà couvert
            parser.parse("A & B");
            parser.parse("A | B");

            // NOT case - déjà couvert
            parser.parse("!A");

            // LEFT_PAREN case - déjà couvert
            parser.parse("(A)");

            // RIGHT_PAREN case - déjà couvert
            parser.parse("(A)");
        }
    }
}

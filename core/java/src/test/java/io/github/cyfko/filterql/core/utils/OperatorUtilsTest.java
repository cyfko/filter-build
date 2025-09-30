package io.github.cyfko.filterql.core.utils;

import io.github.cyfko.filterql.core.validation.Op;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests exhaustifs pour OperatorUtils avec couverture complète de toutes les méthodes.
 * Couvre les cas normaux, limites et la validation des ensembles d'opérateurs.
 */
@DisplayName("Tests pour OperatorUtils")
class OperatorUtilsTest {

    @Nested
    @DisplayName("Tests pour FOR_TEXT")
    class ForTextTests {

        @Test
        @DisplayName("shouldContainCorrectTextOperators")
        void shouldContainCorrectTextOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que tous les opérateurs attendus sont présents
            assertTrue(textOperators.contains(Op.EQUALS));
            assertTrue(textOperators.contains(Op.NOT_EQUALS));
            assertTrue(textOperators.contains(Op.LIKE));
            assertTrue(textOperators.contains(Op.NOT_LIKE));
            assertTrue(textOperators.contains(Op.IN));
            assertTrue(textOperators.contains(Op.NOT_IN));
            assertTrue(textOperators.contains(Op.IS_NULL));
            assertTrue(textOperators.contains(Op.IS_NOT_NULL));
        }

        @Test
        @DisplayName("shouldNotContainNumericOperators")
        void shouldNotContainNumericOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que les opérateurs numériques ne sont pas présents
            assertFalse(textOperators.contains(Op.GREATER_THAN));
            assertFalse(textOperators.contains(Op.GREATER_THAN_OR_EQUAL));
            assertFalse(textOperators.contains(Op.LESS_THAN));
            assertFalse(textOperators.contains(Op.LESS_THAN_OR_EQUAL));
            assertFalse(textOperators.contains(Op.BETWEEN));
            assertFalse(textOperators.contains(Op.NOT_BETWEEN));
        }

        @Test
        @DisplayName("shouldHaveCorrectSize")
        void shouldHaveCorrectSize() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que l'ensemble contient exactement 8 opérateurs
            assertEquals(8, textOperators.size());
        }

        @Test
        @DisplayName("shouldBeImmutable")
        void shouldBeImmutable() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que l'ensemble est immuable
            assertThrows(UnsupportedOperationException.class, () -> 
                textOperators.add(Op.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                textOperators.remove(Op.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                textOperators.clear());
        }

        @Test
        @DisplayName("shouldContainOnlyTextAppropriateOperators")
        void shouldContainOnlyTextAppropriateOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que tous les opérateurs sont appropriés pour le texte
            for (Op operator : textOperators) {
                // Les opérateurs de texte ne devraient pas nécessiter de comparaisons numériques
                assertTrue(
                    operator == Op.EQUALS ||
                    operator == Op.NOT_EQUALS ||
                    operator == Op.LIKE ||
                    operator == Op.NOT_LIKE ||
                    operator == Op.IN ||
                    operator == Op.NOT_IN ||
                    operator == Op.IS_NULL ||
                    operator == Op.IS_NOT_NULL,
                    "Operator " + operator + " should be appropriate for text"
                );
            }
        }

        @ParameterizedTest
        @DisplayName("shouldContainSpecificTextOperators")
        @ValueSource(strings = {"EQUALS", "NOT_EQUALS", "LIKE", "NOT_LIKE", "IN", "NOT_IN", "IS_NULL", "IS_NOT_NULL"})
        void shouldContainSpecificTextOperators(String operatorName) {
            Op operator = Op.valueOf(operatorName);
            assertTrue(OperatorUtils.FOR_TEXT.contains(operator), 
                "FOR_TEXT should contain " + operatorName);
        }
    }

    @Nested
    @DisplayName("Tests pour FOR_NUMBER")
    class ForNumberTests {

        @Test
        @DisplayName("shouldContainCorrectNumericOperators")
        void shouldContainCorrectNumericOperators() {
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs attendus sont présents
            assertTrue(numericOperators.contains(Op.EQUALS));
            assertTrue(numericOperators.contains(Op.NOT_EQUALS));
            assertTrue(numericOperators.contains(Op.GREATER_THAN));
            assertTrue(numericOperators.contains(Op.GREATER_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Op.LESS_THAN));
            assertTrue(numericOperators.contains(Op.LESS_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Op.BETWEEN));
            assertTrue(numericOperators.contains(Op.NOT_BETWEEN));
            assertTrue(numericOperators.contains(Op.IN));
            assertTrue(numericOperators.contains(Op.NOT_IN));
            assertTrue(numericOperators.contains(Op.IS_NULL));
            assertTrue(numericOperators.contains(Op.IS_NOT_NULL));
        }

        @Test
        @DisplayName("shouldNotContainTextSpecificOperators")
        void shouldNotContainTextSpecificOperators() {
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que les opérateurs spécifiques au texte ne sont pas présents
            // (en fait, LIKE et NOT_LIKE ne sont pas dans FOR_NUMBER)
            assertFalse(numericOperators.contains(Op.LIKE));
            assertFalse(numericOperators.contains(Op.NOT_LIKE));
        }

        @Test
        @DisplayName("shouldHaveCorrectSize")
        void shouldHaveCorrectSize() {
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que l'ensemble contient exactement 12 opérateurs
            assertEquals(12, numericOperators.size());
        }

        @Test
        @DisplayName("shouldBeImmutable")
        void shouldBeImmutable() {
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que l'ensemble est immuable
            assertThrows(UnsupportedOperationException.class, () -> 
                numericOperators.add(Op.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                numericOperators.remove(Op.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                numericOperators.clear());
        }

        @Test
        @DisplayName("shouldContainOnlyNumericAppropriateOperators")
        void shouldContainOnlyNumericAppropriateOperators() {
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs sont appropriés pour les nombres
            for (Op operator : numericOperators) {
                // Les opérateurs numériques devraient inclure les comparaisons et les opérateurs de collection
                assertTrue(
                    operator == Op.EQUALS ||
                    operator == Op.NOT_EQUALS ||
                    operator == Op.GREATER_THAN ||
                    operator == Op.GREATER_THAN_OR_EQUAL ||
                    operator == Op.LESS_THAN ||
                    operator == Op.LESS_THAN_OR_EQUAL ||
                    operator == Op.BETWEEN ||
                    operator == Op.NOT_BETWEEN ||
                    operator == Op.IN ||
                    operator == Op.NOT_IN ||
                    operator == Op.IS_NULL ||
                    operator == Op.IS_NOT_NULL,
                    "Operator " + operator + " should be appropriate for numbers"
                );
            }
        }

        @ParameterizedTest
        @DisplayName("shouldContainSpecificNumericOperators")
        @ValueSource(strings = {
            "EQUALS", "NOT_EQUALS", "GREATER_THAN", "GREATER_THAN_OR_EQUAL",
            "LESS_THAN", "LESS_THAN_OR_EQUAL", "BETWEEN", "NOT_BETWEEN",
            "IN", "NOT_IN", "IS_NULL", "IS_NOT_NULL"
        })
        void shouldContainSpecificNumericOperators(String operatorName) {
            Op operator = Op.valueOf(operatorName);
            assertTrue(OperatorUtils.FOR_NUMBER.contains(operator), 
                "FOR_NUMBER should contain " + operatorName);
        }
    }

    @Nested
    @DisplayName("Tests de comparaison entre FOR_TEXT et FOR_NUMBER")
    class ComparisonTests {

        @Test
        @DisplayName("shouldHaveOverlappingOperators")
        void shouldHaveOverlappingOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que certains opérateurs sont communs aux deux
            assertTrue(textOperators.contains(Op.EQUALS));
            assertTrue(numericOperators.contains(Op.EQUALS));
            
            assertTrue(textOperators.contains(Op.NOT_EQUALS));
            assertTrue(numericOperators.contains(Op.NOT_EQUALS));
            
            assertTrue(textOperators.contains(Op.IN));
            assertTrue(numericOperators.contains(Op.IN));
            
            assertTrue(textOperators.contains(Op.NOT_IN));
            assertTrue(numericOperators.contains(Op.NOT_IN));
            
            assertTrue(textOperators.contains(Op.IS_NULL));
            assertTrue(numericOperators.contains(Op.IS_NULL));
            
            assertTrue(textOperators.contains(Op.IS_NOT_NULL));
            assertTrue(numericOperators.contains(Op.IS_NOT_NULL));
        }

        @Test
        @DisplayName("shouldHaveDifferentSizes")
        void shouldHaveDifferentSizes() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // FOR_NUMBER devrait avoir plus d'opérateurs que FOR_TEXT
            assertTrue(numericOperators.size() > textOperators.size());
            assertEquals(8, textOperators.size());
            assertEquals(12, numericOperators.size());
        }

        @Test
        @DisplayName("shouldHaveTextSpecificOperators")
        void shouldHaveTextSpecificOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // LIKE et NOT_LIKE devraient être spécifiques au texte
            assertTrue(textOperators.contains(Op.LIKE));
            assertFalse(numericOperators.contains(Op.LIKE));
            
            assertTrue(textOperators.contains(Op.NOT_LIKE));
            assertFalse(numericOperators.contains(Op.NOT_LIKE));
        }

        @Test
        @DisplayName("shouldHaveNumericSpecificOperators")
        void shouldHaveNumericSpecificOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Les opérateurs de comparaison devraient être spécifiques aux nombres
            assertFalse(textOperators.contains(Op.GREATER_THAN));
            assertTrue(numericOperators.contains(Op.GREATER_THAN));
            
            assertFalse(textOperators.contains(Op.GREATER_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Op.GREATER_THAN_OR_EQUAL));
            
            assertFalse(textOperators.contains(Op.LESS_THAN));
            assertTrue(numericOperators.contains(Op.LESS_THAN));
            
            assertFalse(textOperators.contains(Op.LESS_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Op.LESS_THAN_OR_EQUAL));
            
            assertFalse(textOperators.contains(Op.BETWEEN));
            assertTrue(numericOperators.contains(Op.BETWEEN));
            
            assertFalse(textOperators.contains(Op.NOT_BETWEEN));
            assertTrue(numericOperators.contains(Op.NOT_BETWEEN));
        }
    }

    @Nested
    @DisplayName("Tests de validation des opérateurs")
    class ValidationTests {

        @Test
        @DisplayName("shouldContainAllRequiredOperators")
        void shouldContainAllRequiredOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs nécessaires sont présents
            assertTrue(textOperators.containsAll(Set.of(
                Op.EQUALS, Op.NOT_EQUALS, Op.LIKE, Op.NOT_LIKE,
                Op.IN, Op.NOT_IN, Op.IS_NULL, Op.IS_NOT_NULL
            )));
            
            assertTrue(numericOperators.containsAll(Set.of(
                Op.EQUALS, Op.NOT_EQUALS, Op.GREATER_THAN, Op.GREATER_THAN_OR_EQUAL,
                Op.LESS_THAN, Op.LESS_THAN_OR_EQUAL, Op.BETWEEN, Op.NOT_BETWEEN,
                Op.IN, Op.NOT_IN, Op.IS_NULL, Op.IS_NOT_NULL
            )));
        }

        @Test
        @DisplayName("shouldNotContainDuplicateOperators")
        void shouldNotContainDuplicateOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier qu'il n'y a pas de doublons (les Set ne peuvent pas en contenir)
            assertEquals(textOperators.size(), Set.copyOf(textOperators).size());
            assertEquals(numericOperators.size(), Set.copyOf(numericOperators).size());
        }

        @Test
        @DisplayName("shouldContainOnlyValidOperators")
        void shouldContainOnlyValidOperators() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs sont des valeurs valides de l'enum Operator
            for (Op operator : textOperators) {
                assertNotNull(operator);
                assertTrue(operator instanceof Op);
            }
            
            for (Op operator : numericOperators) {
                assertNotNull(operator);
                assertTrue(operator instanceof Op);
            }
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCaseTests {

        @Test
        @DisplayName("shouldHandleEmptyIntersection")
        void shouldHandleEmptyIntersection() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Créer un ensemble avec des opérateurs qui ne sont dans aucun des deux
            Set<Op> emptySet = Set.of();
            
            // L'intersection avec un ensemble vide devrait être vide
            assertTrue(textOperators.stream().noneMatch(emptySet::contains));
            assertTrue(numericOperators.stream().noneMatch(emptySet::contains));
        }

        @Test
        @DisplayName("shouldHandleNullSafety")
        void shouldHandleNullSafety() {
            Set<Op> textOperators = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que les ensembles ne contiennent pas de valeurs nulles
            // Note: Set.of() ne peut pas contenir de null, donc contains(null) lèvera NPE
            // On teste plutôt que les ensembles sont bien formés
            assertNotNull(textOperators);
            assertNotNull(numericOperators);
            assertFalse(textOperators.isEmpty());
            assertFalse(numericOperators.isEmpty());
        }

        @Test
        @DisplayName("shouldBeConsistentAcrossMultipleCalls")
        void shouldBeConsistentAcrossMultipleCalls() {
            // Vérifier que les ensembles sont les mêmes à chaque appel
            Set<Op> textOperators1 = OperatorUtils.FOR_TEXT;
            Set<Op> textOperators2 = OperatorUtils.FOR_TEXT;
            Set<Op> numericOperators1 = OperatorUtils.FOR_NUMBER;
            Set<Op> numericOperators2 = OperatorUtils.FOR_NUMBER;
            
            assertEquals(textOperators1, textOperators2);
            assertEquals(numericOperators1, numericOperators2);
            assertSame(textOperators1, textOperators2);
            assertSame(numericOperators1, numericOperators2);
        }
    }
}

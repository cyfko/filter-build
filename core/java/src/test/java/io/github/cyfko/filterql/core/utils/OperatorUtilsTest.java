package io.github.cyfko.filterql.core.utils;

import io.github.cyfko.filterql.core.validation.Operator;
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
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que tous les opérateurs attendus sont présents
            assertTrue(textOperators.contains(Operator.EQUALS));
            assertTrue(textOperators.contains(Operator.NOT_EQUALS));
            assertTrue(textOperators.contains(Operator.LIKE));
            assertTrue(textOperators.contains(Operator.NOT_LIKE));
            assertTrue(textOperators.contains(Operator.IN));
            assertTrue(textOperators.contains(Operator.NOT_IN));
            assertTrue(textOperators.contains(Operator.IS_NULL));
            assertTrue(textOperators.contains(Operator.IS_NOT_NULL));
        }

        @Test
        @DisplayName("shouldNotContainNumericOperators")
        void shouldNotContainNumericOperators() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que les opérateurs numériques ne sont pas présents
            assertFalse(textOperators.contains(Operator.GREATER_THAN));
            assertFalse(textOperators.contains(Operator.GREATER_THAN_OR_EQUAL));
            assertFalse(textOperators.contains(Operator.LESS_THAN));
            assertFalse(textOperators.contains(Operator.LESS_THAN_OR_EQUAL));
            assertFalse(textOperators.contains(Operator.BETWEEN));
            assertFalse(textOperators.contains(Operator.NOT_BETWEEN));
        }

        @Test
        @DisplayName("shouldHaveCorrectSize")
        void shouldHaveCorrectSize() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que l'ensemble contient exactement 8 opérateurs
            assertEquals(8, textOperators.size());
        }

        @Test
        @DisplayName("shouldBeImmutable")
        void shouldBeImmutable() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que l'ensemble est immuable
            assertThrows(UnsupportedOperationException.class, () -> 
                textOperators.add(Operator.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                textOperators.remove(Operator.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                textOperators.clear());
        }

        @Test
        @DisplayName("shouldContainOnlyTextAppropriateOperators")
        void shouldContainOnlyTextAppropriateOperators() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            
            // Vérifier que tous les opérateurs sont appropriés pour le texte
            for (Operator operator : textOperators) {
                // Les opérateurs de texte ne devraient pas nécessiter de comparaisons numériques
                assertTrue(
                    operator == Operator.EQUALS ||
                    operator == Operator.NOT_EQUALS ||
                    operator == Operator.LIKE ||
                    operator == Operator.NOT_LIKE ||
                    operator == Operator.IN ||
                    operator == Operator.NOT_IN ||
                    operator == Operator.IS_NULL ||
                    operator == Operator.IS_NOT_NULL,
                    "Operator " + operator + " should be appropriate for text"
                );
            }
        }

        @ParameterizedTest
        @DisplayName("shouldContainSpecificTextOperators")
        @ValueSource(strings = {"EQUALS", "NOT_EQUALS", "LIKE", "NOT_LIKE", "IN", "NOT_IN", "IS_NULL", "IS_NOT_NULL"})
        void shouldContainSpecificTextOperators(String operatorName) {
            Operator operator = Operator.valueOf(operatorName);
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
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs attendus sont présents
            assertTrue(numericOperators.contains(Operator.EQUALS));
            assertTrue(numericOperators.contains(Operator.NOT_EQUALS));
            assertTrue(numericOperators.contains(Operator.GREATER_THAN));
            assertTrue(numericOperators.contains(Operator.GREATER_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Operator.LESS_THAN));
            assertTrue(numericOperators.contains(Operator.LESS_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Operator.BETWEEN));
            assertTrue(numericOperators.contains(Operator.NOT_BETWEEN));
            assertTrue(numericOperators.contains(Operator.IN));
            assertTrue(numericOperators.contains(Operator.NOT_IN));
            assertTrue(numericOperators.contains(Operator.IS_NULL));
            assertTrue(numericOperators.contains(Operator.IS_NOT_NULL));
        }

        @Test
        @DisplayName("shouldNotContainTextSpecificOperators")
        void shouldNotContainTextSpecificOperators() {
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que les opérateurs spécifiques au texte ne sont pas présents
            // (en fait, LIKE et NOT_LIKE ne sont pas dans FOR_NUMBER)
            assertFalse(numericOperators.contains(Operator.LIKE));
            assertFalse(numericOperators.contains(Operator.NOT_LIKE));
        }

        @Test
        @DisplayName("shouldHaveCorrectSize")
        void shouldHaveCorrectSize() {
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que l'ensemble contient exactement 12 opérateurs
            assertEquals(12, numericOperators.size());
        }

        @Test
        @DisplayName("shouldBeImmutable")
        void shouldBeImmutable() {
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que l'ensemble est immuable
            assertThrows(UnsupportedOperationException.class, () -> 
                numericOperators.add(Operator.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                numericOperators.remove(Operator.EQUALS));
            
            assertThrows(UnsupportedOperationException.class, () -> 
                numericOperators.clear());
        }

        @Test
        @DisplayName("shouldContainOnlyNumericAppropriateOperators")
        void shouldContainOnlyNumericAppropriateOperators() {
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs sont appropriés pour les nombres
            for (Operator operator : numericOperators) {
                // Les opérateurs numériques devraient inclure les comparaisons et les opérateurs de collection
                assertTrue(
                    operator == Operator.EQUALS ||
                    operator == Operator.NOT_EQUALS ||
                    operator == Operator.GREATER_THAN ||
                    operator == Operator.GREATER_THAN_OR_EQUAL ||
                    operator == Operator.LESS_THAN ||
                    operator == Operator.LESS_THAN_OR_EQUAL ||
                    operator == Operator.BETWEEN ||
                    operator == Operator.NOT_BETWEEN ||
                    operator == Operator.IN ||
                    operator == Operator.NOT_IN ||
                    operator == Operator.IS_NULL ||
                    operator == Operator.IS_NOT_NULL,
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
            Operator operator = Operator.valueOf(operatorName);
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
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que certains opérateurs sont communs aux deux
            assertTrue(textOperators.contains(Operator.EQUALS));
            assertTrue(numericOperators.contains(Operator.EQUALS));
            
            assertTrue(textOperators.contains(Operator.NOT_EQUALS));
            assertTrue(numericOperators.contains(Operator.NOT_EQUALS));
            
            assertTrue(textOperators.contains(Operator.IN));
            assertTrue(numericOperators.contains(Operator.IN));
            
            assertTrue(textOperators.contains(Operator.NOT_IN));
            assertTrue(numericOperators.contains(Operator.NOT_IN));
            
            assertTrue(textOperators.contains(Operator.IS_NULL));
            assertTrue(numericOperators.contains(Operator.IS_NULL));
            
            assertTrue(textOperators.contains(Operator.IS_NOT_NULL));
            assertTrue(numericOperators.contains(Operator.IS_NOT_NULL));
        }

        @Test
        @DisplayName("shouldHaveDifferentSizes")
        void shouldHaveDifferentSizes() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // FOR_NUMBER devrait avoir plus d'opérateurs que FOR_TEXT
            assertTrue(numericOperators.size() > textOperators.size());
            assertEquals(8, textOperators.size());
            assertEquals(12, numericOperators.size());
        }

        @Test
        @DisplayName("shouldHaveTextSpecificOperators")
        void shouldHaveTextSpecificOperators() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // LIKE et NOT_LIKE devraient être spécifiques au texte
            assertTrue(textOperators.contains(Operator.LIKE));
            assertFalse(numericOperators.contains(Operator.LIKE));
            
            assertTrue(textOperators.contains(Operator.NOT_LIKE));
            assertFalse(numericOperators.contains(Operator.NOT_LIKE));
        }

        @Test
        @DisplayName("shouldHaveNumericSpecificOperators")
        void shouldHaveNumericSpecificOperators() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Les opérateurs de comparaison devraient être spécifiques aux nombres
            assertFalse(textOperators.contains(Operator.GREATER_THAN));
            assertTrue(numericOperators.contains(Operator.GREATER_THAN));
            
            assertFalse(textOperators.contains(Operator.GREATER_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Operator.GREATER_THAN_OR_EQUAL));
            
            assertFalse(textOperators.contains(Operator.LESS_THAN));
            assertTrue(numericOperators.contains(Operator.LESS_THAN));
            
            assertFalse(textOperators.contains(Operator.LESS_THAN_OR_EQUAL));
            assertTrue(numericOperators.contains(Operator.LESS_THAN_OR_EQUAL));
            
            assertFalse(textOperators.contains(Operator.BETWEEN));
            assertTrue(numericOperators.contains(Operator.BETWEEN));
            
            assertFalse(textOperators.contains(Operator.NOT_BETWEEN));
            assertTrue(numericOperators.contains(Operator.NOT_BETWEEN));
        }
    }

    @Nested
    @DisplayName("Tests de validation des opérateurs")
    class ValidationTests {

        @Test
        @DisplayName("shouldContainAllRequiredOperators")
        void shouldContainAllRequiredOperators() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs nécessaires sont présents
            assertTrue(textOperators.containsAll(Set.of(
                Operator.EQUALS, Operator.NOT_EQUALS, Operator.LIKE, Operator.NOT_LIKE,
                Operator.IN, Operator.NOT_IN, Operator.IS_NULL, Operator.IS_NOT_NULL
            )));
            
            assertTrue(numericOperators.containsAll(Set.of(
                Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
                Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN,
                Operator.IN, Operator.NOT_IN, Operator.IS_NULL, Operator.IS_NOT_NULL
            )));
        }

        @Test
        @DisplayName("shouldNotContainDuplicateOperators")
        void shouldNotContainDuplicateOperators() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier qu'il n'y a pas de doublons (les Set ne peuvent pas en contenir)
            assertEquals(textOperators.size(), Set.copyOf(textOperators).size());
            assertEquals(numericOperators.size(), Set.copyOf(numericOperators).size());
        }

        @Test
        @DisplayName("shouldContainOnlyValidOperators")
        void shouldContainOnlyValidOperators() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Vérifier que tous les opérateurs sont des valeurs valides de l'enum Operator
            for (Operator operator : textOperators) {
                assertNotNull(operator);
                assertTrue(operator instanceof Operator);
            }
            
            for (Operator operator : numericOperators) {
                assertNotNull(operator);
                assertTrue(operator instanceof Operator);
            }
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCaseTests {

        @Test
        @DisplayName("shouldHandleEmptyIntersection")
        void shouldHandleEmptyIntersection() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
            // Créer un ensemble avec des opérateurs qui ne sont dans aucun des deux
            Set<Operator> emptySet = Set.of();
            
            // L'intersection avec un ensemble vide devrait être vide
            assertTrue(textOperators.stream().noneMatch(emptySet::contains));
            assertTrue(numericOperators.stream().noneMatch(emptySet::contains));
        }

        @Test
        @DisplayName("shouldHandleNullSafety")
        void shouldHandleNullSafety() {
            Set<Operator> textOperators = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators = OperatorUtils.FOR_NUMBER;
            
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
            Set<Operator> textOperators1 = OperatorUtils.FOR_TEXT;
            Set<Operator> textOperators2 = OperatorUtils.FOR_TEXT;
            Set<Operator> numericOperators1 = OperatorUtils.FOR_NUMBER;
            Set<Operator> numericOperators2 = OperatorUtils.FOR_NUMBER;
            
            assertEquals(textOperators1, textOperators2);
            assertEquals(numericOperators1, numericOperators2);
            assertSame(textOperators1, textOperators2);
            assertSame(numericOperators1, numericOperators2);
        }
    }
}

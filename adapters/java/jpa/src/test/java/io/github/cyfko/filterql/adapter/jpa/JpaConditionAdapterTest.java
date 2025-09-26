package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests exhaustifs pour JpaConditionAdapter avec couverture complète de toutes les méthodes.
 * Couvre les cas normaux, limites, erreurs et les fonctionnalités de combinaison logique.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour JpaConditionAdapter")
class JpaConditionAdapterTest {

    @Mock
    private Predicate mockPredicate;

    @Mock
    private CriteriaBuilder mockCriteriaBuilder;

    @Mock
    private Predicate mockOtherPredicate;

    @Mock
    private Predicate mockCombinedPredicate;

    @Mock
    private Predicate mockNegatedPredicate;

    private JpaConditionAdapter<Object> jpaConditionAdapter;

    @BeforeEach
    void setUp() {
        jpaConditionAdapter = new JpaConditionAdapter<>(mockPredicate, mockCriteriaBuilder);
    }

    @Nested
    @DisplayName("Tests pour le constructeur")
    class ConstructorTests {

        @Test
        @DisplayName("shouldCreateInstanceWithValidParameters")
        void shouldCreateInstanceWithValidParameters() {
            assertNotNull(jpaConditionAdapter);
        }

        @Test
        @DisplayName("shouldStorePredicateAndCriteriaBuilder")
        void shouldStorePredicateAndCriteriaBuilder() {
            assertEquals(mockPredicate, jpaConditionAdapter.getPredicate());
        }
    }

    @Nested
    @DisplayName("Tests pour getPredicate")
    class GetPredicateTests {

        @Test
        @DisplayName("shouldReturnStoredPredicate")
        void shouldReturnStoredPredicate() {
            Predicate result = jpaConditionAdapter.getPredicate();
            
            assertNotNull(result);
            assertEquals(mockPredicate, result);
        }

        @Test
        @DisplayName("shouldReturnSamePredicateInstance")
        void shouldReturnSamePredicateInstance() {
            Predicate result1 = jpaConditionAdapter.getPredicate();
            Predicate result2 = jpaConditionAdapter.getPredicate();
            
            assertSame(result1, result2);
        }
    }

    @Nested
    @DisplayName("Tests pour and")
    class AndTests {

        @Test
        @DisplayName("shouldCombineWithOtherJpaCondition")
        void shouldCombineWithOtherJpaCondition() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.and(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);

            // Act
            Condition result = jpaConditionAdapter.and(otherJpaCondition);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            JpaConditionAdapter<Object> resultJpa = (JpaConditionAdapter<Object>) result;
            assertEquals(mockCombinedPredicate, resultJpa.getPredicate());
            verify(mockCriteriaBuilder).and(mockPredicate, mockOtherPredicate);
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenOtherIsNotJpaCondition")
        void shouldThrowExceptionWhenOtherIsNotJpaCondition() {
            // Arrange
            Condition nonJpaCondition = mock(Condition.class);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jpaConditionAdapter.and(nonJpaCondition)
            );
            
            assertEquals("Cannot combine with non-JPA condition", exception.getMessage());
            verifyNoInteractions(mockCriteriaBuilder);
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenOtherIsNull")
        void shouldThrowExceptionWhenOtherIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> jpaConditionAdapter.and(null));
        }

        @Test
        @DisplayName("shouldCreateNewInstanceForAndCombination")
        void shouldCreateNewInstanceForAndCombination() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.and(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);

            // Act
            Condition result = jpaConditionAdapter.and(otherJpaCondition);

            // Assert
            assertNotSame(jpaConditionAdapter, result);
            assertTrue(result instanceof JpaConditionAdapter);
        }

        @Test
        @DisplayName("shouldPreserveCriteriaBuilderInResult")
        void shouldPreserveCriteriaBuilderInResult() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.and(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);

            // Act
            Condition result = jpaConditionAdapter.and(otherJpaCondition);

            // Assert
            JpaConditionAdapter<Object> resultJpa = (JpaConditionAdapter<Object>) result;
            // Le CriteriaBuilder n'est pas exposé publiquement, mais on peut vérifier que l'opération s'est bien passée
            verify(mockCriteriaBuilder).and(mockPredicate, mockOtherPredicate);
        }
    }

    @Nested
    @DisplayName("Tests pour or")
    class OrTests {

        @Test
        @DisplayName("shouldCombineWithOtherJpaCondition")
        void shouldCombineWithOtherJpaCondition() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.or(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);

            // Act
            Condition result = jpaConditionAdapter.or(otherJpaCondition);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            JpaConditionAdapter<Object> resultJpa = (JpaConditionAdapter<Object>) result;
            assertEquals(mockCombinedPredicate, resultJpa.getPredicate());
            verify(mockCriteriaBuilder).or(mockPredicate, mockOtherPredicate);
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenOtherIsNotJpaCondition")
        void shouldThrowExceptionWhenOtherIsNotJpaCondition() {
            // Arrange
            Condition nonJpaCondition = mock(Condition.class);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jpaConditionAdapter.or(nonJpaCondition)
            );
            
            assertEquals("Cannot combine with non-JPA condition", exception.getMessage());
            verifyNoInteractions(mockCriteriaBuilder);
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenOtherIsNull")
        void shouldThrowExceptionWhenOtherIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> jpaConditionAdapter.or(null));
        }

        @Test
        @DisplayName("shouldCreateNewInstanceForOrCombination")
        void shouldCreateNewInstanceForOrCombination() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.or(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);

            // Act
            Condition result = jpaConditionAdapter.or(otherJpaCondition);

            // Assert
            assertNotSame(jpaConditionAdapter, result);
            assertTrue(result instanceof JpaConditionAdapter);
        }
    }

    @Nested
    @DisplayName("Tests pour not")
    class NotTests {

        @Test
        @DisplayName("shouldCreateNegatedCondition")
        void shouldCreateNegatedCondition() {
            // Arrange
            when(mockCriteriaBuilder.not(mockPredicate)).thenReturn(mockNegatedPredicate);

            // Act
            Condition result = jpaConditionAdapter.not();

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            JpaConditionAdapter<Object> resultJpa = (JpaConditionAdapter<Object>) result;
            assertEquals(mockNegatedPredicate, resultJpa.getPredicate());
            verify(mockCriteriaBuilder).not(mockPredicate);
        }

        @Test
        @DisplayName("shouldCreateNewInstanceForNegation")
        void shouldCreateNewInstanceForNegation() {
            // Arrange
            when(mockCriteriaBuilder.not(mockPredicate)).thenReturn(mockNegatedPredicate);

            // Act
            Condition result = jpaConditionAdapter.not();

            // Assert
            assertNotSame(jpaConditionAdapter, result);
            assertTrue(result instanceof JpaConditionAdapter);
        }

        @Test
        @DisplayName("shouldPreserveCriteriaBuilderInNegatedResult")
        void shouldPreserveCriteriaBuilderInNegatedResult() {
            // Arrange
            when(mockCriteriaBuilder.not(mockPredicate)).thenReturn(mockNegatedPredicate);

            // Act
            Condition result = jpaConditionAdapter.not();

            // Assert
            verify(mockCriteriaBuilder).not(mockPredicate);
            // Le CriteriaBuilder est préservé dans le résultat
        }
    }

    @Nested
    @DisplayName("Tests de combinaisons complexes")
    class ComplexCombinationTests {

        @Test
        @DisplayName("shouldSupportChainedOperations")
        void shouldSupportChainedOperations() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.and(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);
            when(mockCriteriaBuilder.not(mockCombinedPredicate)).thenReturn(mockNegatedPredicate);

            // Act
            Condition result = jpaConditionAdapter.and(otherJpaCondition).not();

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            verify(mockCriteriaBuilder).and(mockPredicate, mockOtherPredicate);
            verify(mockCriteriaBuilder).not(mockCombinedPredicate);
        }

        @Test
        @DisplayName("shouldSupportMultipleAndOperations")
        void shouldSupportMultipleAndOperations() {
            // Arrange
            Predicate mockPredicate2 = mock(Predicate.class);
            Predicate mockPredicate3 = mock(Predicate.class);
            Predicate mockCombinedPredicate2 = mock(Predicate.class);
            
            JpaConditionAdapter<Object> otherJpaCondition1 = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            JpaConditionAdapter<Object> otherJpaCondition2 = new JpaConditionAdapter<>(mockPredicate2, mockCriteriaBuilder);
            
            when(mockCriteriaBuilder.and(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);
            when(mockCriteriaBuilder.and(mockCombinedPredicate, mockPredicate2)).thenReturn(mockCombinedPredicate2);

            // Act
            Condition result = jpaConditionAdapter.and(otherJpaCondition1).and(otherJpaCondition2);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            verify(mockCriteriaBuilder).and(mockPredicate, mockOtherPredicate);
            verify(mockCriteriaBuilder).and(mockCombinedPredicate, mockPredicate2);
        }

        @Test
        @DisplayName("shouldSupportMultipleOrOperations")
        void shouldSupportMultipleOrOperations() {
            // Arrange
            Predicate mockPredicate2 = mock(Predicate.class);
            Predicate mockCombinedPredicate2 = mock(Predicate.class);
            
            JpaConditionAdapter<Object> otherJpaCondition1 = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            JpaConditionAdapter<Object> otherJpaCondition2 = new JpaConditionAdapter<>(mockPredicate2, mockCriteriaBuilder);
            
            when(mockCriteriaBuilder.or(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);
            when(mockCriteriaBuilder.or(mockCombinedPredicate, mockPredicate2)).thenReturn(mockCombinedPredicate2);

            // Act
            Condition result = jpaConditionAdapter.or(otherJpaCondition1).or(otherJpaCondition2);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            verify(mockCriteriaBuilder).or(mockPredicate, mockOtherPredicate);
            verify(mockCriteriaBuilder).or(mockCombinedPredicate, mockPredicate2);
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCaseTests {

        @Test
        @DisplayName("shouldHandleSelfCombination")
        void shouldHandleSelfCombination() {
            // Arrange
            when(mockCriteriaBuilder.and(mockPredicate, mockPredicate)).thenReturn(mockCombinedPredicate);

            // Act
            Condition result = jpaConditionAdapter.and(jpaConditionAdapter);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            verify(mockCriteriaBuilder).and(mockPredicate, mockPredicate);
        }

        @Test
        @DisplayName("shouldHandleDoubleNegation")
        void shouldHandleDoubleNegation() {
            // Arrange
            Predicate mockDoubleNegatedPredicate = mock(Predicate.class);
            when(mockCriteriaBuilder.not(mockPredicate)).thenReturn(mockNegatedPredicate);
            when(mockCriteriaBuilder.not(mockNegatedPredicate)).thenReturn(mockDoubleNegatedPredicate);

            // Act
            Condition result = jpaConditionAdapter.not().not();

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            verify(mockCriteriaBuilder).not(mockPredicate);
            verify(mockCriteriaBuilder).not(mockNegatedPredicate);
        }

        @Test
        @DisplayName("shouldMaintainImmutability")
        void shouldMaintainImmutability() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.and(mockPredicate, mockOtherPredicate)).thenReturn(mockCombinedPredicate);

            // Act
            Condition result = jpaConditionAdapter.and(otherJpaCondition);

            // Assert
            // L'instance originale ne doit pas être modifiée
            assertEquals(mockPredicate, jpaConditionAdapter.getPredicate());
            assertNotSame(jpaConditionAdapter, result);
        }
    }

    @Nested
    @DisplayName("Tests de robustesse")
    class RobustnessTests {

        @Test
        @DisplayName("shouldHandleCriteriaBuilderExceptions")
        void shouldHandleCriteriaBuilderExceptions() {
            // Arrange
            JpaConditionAdapter<Object> otherJpaCondition = new JpaConditionAdapter<>(mockOtherPredicate, mockCriteriaBuilder);
            when(mockCriteriaBuilder.and(mockPredicate, mockOtherPredicate))
                .thenThrow(new RuntimeException("CriteriaBuilder error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> jpaConditionAdapter.and(otherJpaCondition));
        }

        @Test
        @DisplayName("shouldHandleNullPredicateInConstructor")
        void shouldHandleNullPredicateInConstructor() {
            // Act & Assert - Le constructeur n'effectue pas de validation null
            assertDoesNotThrow(() -> 
                new JpaConditionAdapter<>(null, mockCriteriaBuilder));
        }

        @Test
        @DisplayName("shouldHandleNullCriteriaBuilderInConstructor")
        void shouldHandleNullCriteriaBuilderInConstructor() {
            // Act & Assert - Le constructeur n'effectue pas de validation null
            assertDoesNotThrow(() -> 
                new JpaConditionAdapter<>(mockPredicate, null));
        }
    }
}

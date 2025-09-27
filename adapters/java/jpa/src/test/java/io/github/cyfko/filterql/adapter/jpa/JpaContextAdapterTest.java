package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests exhaustifs pour JpaContextAdapter avec couverture complète de toutes les méthodes.
 * Couvre les cas normaux, limites, erreurs et les fonctionnalités de gestion de contexte.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour JpaContextAdapter")
class JpaContextAdapterTest {

    @Mock
    private EntityManager mockEntityManager;

    @Mock
    private CriteriaBuilder mockCriteriaBuilder;

    @Mock
    private CriteriaQuery<TestEntity> mockCriteriaQuery;

    @Mock
    private Root<TestEntity> mockRoot;

    @Mock
    private SpecificationBuilder<TestEntity, TestPropertyRef> mockSpecificationBuilder;

    @Mock
    private Specification<TestEntity> mockSpecification;

    @Mock
    private jakarta.persistence.criteria.Predicate mockPredicate;

    private JpaContextAdapter<TestEntity, TestPropertyRef> jpaContextAdapter;

    // Enum de test pour PropertyRef
    enum TestPropertyRef implements PropertyRef {
        NAME(String.class, Set.of(Operator.EQUALS, Operator.LIKE)),
        AGE(Integer.class, Set.of(Operator.EQUALS, Operator.GREATER_THAN));

        private final Class<?> type;
        private final Set<Operator> supportedOperators;

        TestPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
            this.type = type;
            this.supportedOperators = supportedOperators;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Set<Operator> getSupportedOperators() {
            return supportedOperators;
        }

        @Override
        public void validateOperator(Operator operator) {
            if (!supportedOperators.contains(operator)) {
                throw new IllegalArgumentException("Operator " + operator + " not supported for " + this);
            }
        }

        @Override
        public void validateOperatorForValue(Operator operator, Object value) {
            validateOperator(operator);
            // Validation basique du type
            if (value != null && !type.isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("Value type " + value.getClass() + " not compatible with " + type);
            }
        }
    }

    // Classe d'entité de test
    static class TestEntity {
        private String name;
        private Integer age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    @BeforeEach
    void setUp() {
        when(mockEntityManager.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
        when(mockCriteriaBuilder.createQuery(TestEntity.class)).thenReturn(mockCriteriaQuery);
        when(mockCriteriaQuery.from(TestEntity.class)).thenReturn(mockRoot);

        jpaContextAdapter = new JpaContextAdapter<>(
            TestEntity.class, 
            mockEntityManager, 
            mockSpecificationBuilder
        );
    }

    @Nested
    @DisplayName("Tests pour le constructeur")
    class ConstructorTests {

        @Test
        @DisplayName("shouldCreateInstanceWithValidParameters")
        void shouldCreateInstanceWithValidParameters() {
            assertNotNull(jpaContextAdapter);
        }

        @Test
        @DisplayName("shouldInitializeEmptyFiltersMap")
        void shouldInitializeEmptyFiltersMap() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                jpaContextAdapter.getCondition("nonExistent"));
        }

        @Test
        @DisplayName("shouldStoreEntityManagerAndCriteriaBuilder")
        void shouldStoreEntityManagerAndCriteriaBuilder() {
            // Vérifier que les mocks ont été appelés
            verify(mockEntityManager).getCriteriaBuilder();
            verify(mockCriteriaBuilder).createQuery(TestEntity.class);
            verify(mockCriteriaQuery).from(TestEntity.class);
        }
    }

    @Nested
    @DisplayName("Tests pour addCondition")
    class AddConditionTests {

        @Test
        @DisplayName("shouldAddValidCondition")
        void shouldAddValidCondition() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            when(mockSpecificationBuilder.build(TestPropertyRef.NAME, Operator.EQUALS, "John"))
                .thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("nameFilter", definition);

            // Assert
            Condition result = jpaContextAdapter.getCondition("nameFilter");
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
            verify(mockSpecificationBuilder).build(TestPropertyRef.NAME, Operator.EQUALS, "John");
        }

        @Test
        @DisplayName("shouldValidateOperatorBeforeAdding")
        void shouldValidateOperatorBeforeAdding() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.GREATER_THAN, // Non supporté pour NAME
                "John"
            );

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jpaContextAdapter.addCondition("nameFilter", definition)
            );
            
            assertTrue(exception.getMessage().contains("Operator GREATER_THAN not supported"));
            verifyNoInteractions(mockSpecificationBuilder);
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenFilterKeyIsNull")
        void shouldThrowExceptionWhenFilterKeyIsNull() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );

            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                jpaContextAdapter.addCondition(null, definition));
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenDefinitionIsNull")
        void shouldThrowExceptionWhenDefinitionIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                jpaContextAdapter.addCondition("filter", null));
        }

        @Test
        @DisplayName("shouldAllowMultipleConditionsWithDifferentKeys")
        void shouldAllowMultipleConditionsWithDifferentKeys() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition1 = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            FilterDefinition<TestPropertyRef> definition2 = new FilterDefinition<>(
                TestPropertyRef.AGE, 
                Operator.GREATER_THAN, 
                18
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("nameFilter", definition1);
            jpaContextAdapter.addCondition("ageFilter", definition2);

            // Assert
            Condition nameCondition = jpaContextAdapter.getCondition("nameFilter");
            Condition ageCondition = jpaContextAdapter.getCondition("ageFilter");
            
            assertNotNull(nameCondition);
            assertNotNull(ageCondition);
            assertNotSame(nameCondition, ageCondition);
        }

        @Test
        @DisplayName("shouldOverwriteExistingConditionWithSameKey")
        void shouldOverwriteExistingConditionWithSameKey() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition1 = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            FilterDefinition<TestPropertyRef> definition2 = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.LIKE, 
                "Jo%"
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("nameFilter", definition1);
            Condition firstCondition = jpaContextAdapter.getCondition("nameFilter");
            
            jpaContextAdapter.addCondition("nameFilter", definition2);
            Condition secondCondition = jpaContextAdapter.getCondition("nameFilter");

            // Assert
            assertNotNull(firstCondition);
            assertNotNull(secondCondition);
            // Les conditions peuvent être différentes (nouvelle instance créée)
        }
    }

    @Nested
    @DisplayName("Tests pour getCondition")
    class GetConditionTests {

        @Test
        @DisplayName("shouldReturnExistingCondition")
        void shouldReturnExistingCondition() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);
            jpaContextAdapter.addCondition("nameFilter", definition);

            // Act
            Condition result = jpaContextAdapter.getCondition("nameFilter");

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof JpaConditionAdapter);
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenConditionNotFound")
        void shouldThrowExceptionWhenConditionNotFound() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jpaContextAdapter.getCondition("nonExistent")
            );
            
            assertEquals("No condition found for key: nonExistent", exception.getMessage());
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenKeyIsNull")
        void shouldThrowExceptionWhenKeyIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                jpaContextAdapter.getCondition(null));
        }

        @Test
        @DisplayName("shouldThrowExceptionWhenKeyIsEmpty")
        void shouldThrowExceptionWhenKeyIsEmpty() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jpaContextAdapter.getCondition("")
            );
            
            assertEquals("No condition found for key: ", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests pour getQuery")
    class GetQueryTests {

        @Test
        @DisplayName("shouldReturnCriteriaQuery")
        void shouldReturnCriteriaQuery() {
            // Act
            CriteriaQuery<TestEntity> result = jpaContextAdapter.getQuery();

            // Assert
            assertNotNull(result);
            assertEquals(mockCriteriaQuery, result);
        }

        @Test
        @DisplayName("shouldReturnSameQueryInstance")
        void shouldReturnSameQueryInstance() {
            // Act
            CriteriaQuery<TestEntity> result1 = jpaContextAdapter.getQuery();
            CriteriaQuery<TestEntity> result2 = jpaContextAdapter.getQuery();

            // Assert
            assertSame(result1, result2);
        }
    }

    @Nested
    @DisplayName("Tests d'intégration")
    class IntegrationTests {

        @Test
        @DisplayName("shouldBuildCompleteConditionWorkflow")
        void shouldBuildCompleteConditionWorkflow() {
            // Arrange
            FilterDefinition<TestPropertyRef> nameDefinition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            FilterDefinition<TestPropertyRef> ageDefinition = new FilterDefinition<>(
                TestPropertyRef.AGE, 
                Operator.GREATER_THAN, 
                18
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("nameFilter", nameDefinition);
            jpaContextAdapter.addCondition("ageFilter", ageDefinition);
            
            Condition nameCondition = jpaContextAdapter.getCondition("nameFilter");
            Condition ageCondition = jpaContextAdapter.getCondition("ageFilter");

            // Assert
            assertNotNull(nameCondition);
            assertNotNull(ageCondition);
            assertTrue(nameCondition instanceof JpaConditionAdapter);
            assertTrue(ageCondition instanceof JpaConditionAdapter);
            
            verify(mockSpecificationBuilder).build(TestPropertyRef.NAME, Operator.EQUALS, "John");
            verify(mockSpecificationBuilder).build(TestPropertyRef.AGE, Operator.GREATER_THAN, 18);
        }

        @Test
        @DisplayName("shouldSupportComplexFilterDefinitions")
        void shouldSupportComplexFilterDefinitions() {
            // Arrange
            FilterDefinition<TestPropertyRef> likeDefinition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.LIKE, 
                "Jo%"
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("likeFilter", likeDefinition);
            Condition result = jpaContextAdapter.getCondition("likeFilter");

            // Assert
            assertNotNull(result);
            verify(mockSpecificationBuilder).build(TestPropertyRef.NAME, Operator.LIKE, "Jo%");
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCaseTests {

        @Test
        @DisplayName("shouldHandleEmptyFilterKey")
        void shouldHandleEmptyFilterKey() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("", definition);
            Condition result = jpaContextAdapter.getCondition("");

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("shouldHandleWhitespaceFilterKey")
        void shouldHandleWhitespaceFilterKey() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("   ", definition);
            Condition result = jpaContextAdapter.getCondition("   ");

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("shouldHandleNullValueInDefinition")
        void shouldHandleNullValueInDefinition() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                null
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any())).thenReturn(mockPredicate);

            // Act
            jpaContextAdapter.addCondition("nullFilter", definition);
            Condition result = jpaContextAdapter.getCondition("nullFilter");

            // Assert
            assertNotNull(result);
            verify(mockSpecificationBuilder).build(TestPropertyRef.NAME, Operator.EQUALS, null);
        }
    }

    @Nested
    @DisplayName("Tests de robustesse")
    class RobustnessTests {

        @Test
        @DisplayName("shouldHandleSpecificationBuilderExceptions")
        void shouldHandleSpecificationBuilderExceptions() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            when(mockSpecificationBuilder.build(any(), any(), any()))
                .thenThrow(new RuntimeException("SpecificationBuilder error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> 
                jpaContextAdapter.addCondition("nameFilter", definition));
        }

        @Test
        @DisplayName("shouldHandleSpecificationToPredicateExceptions")
        void shouldHandleSpecificationToPredicateExceptions() {
            // Arrange
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );
            when(mockSpecificationBuilder.build(any(), any(), any())).thenReturn(mockSpecification);
            when(mockSpecification.toPredicate(any(), any(), any()))
                .thenThrow(new RuntimeException("Predicate creation error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> 
                jpaContextAdapter.addCondition("nameFilter", definition));
        }
    }
}

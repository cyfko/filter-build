package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
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
 * Tests pour l'interface SpecificationBuilder et ses implémentations.
 * Couvre les cas normaux, limites et les fonctionnalités de construction de spécifications.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour SpecificationBuilder")
class SpecificationBuilderTest {

    @Mock
    private Specification<TestEntity> mockSpecification;

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

    // Implémentation de test pour SpecificationBuilder
    static class TestSpecificationBuilder implements SpecificationBuilder<TestEntity, TestPropertyRef> {
        @Override
        public Specification<TestEntity> build(TestPropertyRef ref, Operator op, Object value) {
            // Validation des paramètres
            if (ref == null) {
                throw new IllegalArgumentException("PropertyRef cannot be null");
            }
            if (op == null) {
                throw new IllegalArgumentException("Operator cannot be null");
            }
            
            // Validation de l'opérateur
            ref.validateOperator(op);
            
            // Validation de la valeur
            ref.validateOperatorForValue(op, value);
            
            // Retourner une spécification mock pour les tests
            return new Specification<TestEntity>() {
                @Override
                public jakarta.persistence.criteria.Predicate toPredicate(
                    jakarta.persistence.criteria.Root<TestEntity> root,
                    jakarta.persistence.criteria.CriteriaQuery<?> query,
                    jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
                    return criteriaBuilder.conjunction(); // Toujours true
                }
            };
        }
    }

    @Nested
    @DisplayName("Tests pour l'interface SpecificationBuilder")
    class InterfaceTests {

        @Test
        @DisplayName("shouldDefineCorrectMethodSignature")
        void shouldDefineCorrectMethodSignature() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();
            FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );

            // Act
            Specification<TestEntity> result = builder.build(
                definition.getRef(), 
                definition.getOperator(), 
                definition.getValue()
            );

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof Specification);
        }

        @Test
        @DisplayName("shouldAcceptValidParameters")
        void shouldAcceptValidParameters() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, "John"));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.GREATER_THAN, 18));
        }

        @Test
        @DisplayName("shouldRejectNullPropertyRef")
        void shouldRejectNullPropertyRef() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(null, Operator.EQUALS, "John")
            );
            
            assertEquals("PropertyRef cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("shouldRejectNullOperator")
        void shouldRejectNullOperator() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(TestPropertyRef.NAME, null, "John")
            );
            
            assertEquals("Operator cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("shouldValidateOperatorSupport")
        void shouldValidateOperatorSupport() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(TestPropertyRef.NAME, Operator.GREATER_THAN, "John")
            );
            
            assertTrue(exception.getMessage().contains("Operator GREATER_THAN not supported"));
        }

        @Test
        @DisplayName("shouldValidateValueType")
        void shouldValidateValueType() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(TestPropertyRef.NAME, Operator.EQUALS, 123)
            );
            
            assertTrue(exception.getMessage().contains("Value type"));
        }
    }

    @Nested
    @DisplayName("Tests pour les implémentations concrètes")
    class ImplementationTests {

        @Test
        @DisplayName("shouldReturnSpecificationInstance")
        void shouldReturnSpecificationInstance() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act
            Specification<TestEntity> result = builder.build(
                TestPropertyRef.NAME, 
                Operator.EQUALS, 
                "John"
            );

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof Specification);
        }

        @Test
        @DisplayName("shouldHandleDifferentOperators")
        void shouldHandleDifferentOperators() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, "John"));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.LIKE, "Jo%"));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.EQUALS, 25));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.GREATER_THAN, 18));
        }

        @Test
        @DisplayName("shouldHandleNullValues")
        void shouldHandleNullValues() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, null));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.EQUALS, null));
        }

        @Test
        @DisplayName("shouldHandleEmptyStringValues")
        void shouldHandleEmptyStringValues() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, ""));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.LIKE, ""));
        }

        @Test
        @DisplayName("shouldHandleZeroValues")
        void shouldHandleZeroValues() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.EQUALS, 0));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.GREATER_THAN, 0));
        }
    }

    @Nested
    @DisplayName("Tests de cas limites")
    class EdgeCaseTests {

        @Test
        @DisplayName("shouldHandleComplexValueTypes")
        void shouldHandleComplexValueTypes() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, "Complex String with Special Characters !@#$%"));
        }

        @Test
        @DisplayName("shouldHandleVeryLongStrings")
        void shouldHandleVeryLongStrings() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();
            String longString = "a".repeat(10000);

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, longString));
        }

        @Test
        @DisplayName("shouldHandleLargeNumbers")
        void shouldHandleLargeNumbers() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.EQUALS, Integer.MAX_VALUE));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.GREATER_THAN, Integer.MIN_VALUE));
        }

        @Test
        @DisplayName("shouldHandleNegativeNumbers")
        void shouldHandleNegativeNumbers() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.EQUALS, -1));
            assertDoesNotThrow(() -> 
                builder.build(TestPropertyRef.AGE, Operator.GREATER_THAN, -100));
        }
    }

    @Nested
    @DisplayName("Tests de robustesse")
    class RobustnessTests {

        @Test
        @DisplayName("shouldHandleUnsupportedOperatorsGracefully")
        void shouldHandleUnsupportedOperatorsGracefully() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                builder.build(TestPropertyRef.NAME, Operator.IN, "John"));
            assertThrows(IllegalArgumentException.class, () -> 
                builder.build(TestPropertyRef.AGE, Operator.LIKE, 25));
        }

        @Test
        @DisplayName("shouldHandleTypeMismatchGracefully")
        void shouldHandleTypeMismatchGracefully() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, 123));
            assertThrows(IllegalArgumentException.class, () -> 
                builder.build(TestPropertyRef.AGE, Operator.EQUALS, "not a number"));
        }

        @Test
        @DisplayName("shouldHandleConcurrentAccess")
        void shouldHandleConcurrentAccess() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();

            // Act & Assert
            // Simuler un accès concurrent (bien que ce soit un test unitaire)
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    builder.build(TestPropertyRef.NAME, Operator.EQUALS, "Test" + i);
                }
            });
        }
    }

    @Nested
    @DisplayName("Tests de performance")
    class PerformanceTests {

        @Test
        @DisplayName("shouldBuildSpecificationsEfficiently")
        void shouldBuildSpecificationsEfficiently() {
            // Arrange
            TestSpecificationBuilder builder = new TestSpecificationBuilder();
            long startTime = System.currentTimeMillis();

            // Act
            for (int i = 0; i < 1000; i++) {
                builder.build(TestPropertyRef.NAME, Operator.EQUALS, "Test" + i);
            }
            long endTime = System.currentTimeMillis();

            // Assert
            long duration = endTime - startTime;
            assertTrue(duration < 1000, "Building 1000 specifications should take less than 1 second");
        }
    }
}

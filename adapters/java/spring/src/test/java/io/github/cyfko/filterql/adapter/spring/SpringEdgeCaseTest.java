package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests pour les cas limites et les erreurs de l'adaptateur Spring.
 * Couvre les scénarios d'erreur et les cas limites.
 */
@ExtendWith(MockitoExtension.class)
class SpringEdgeCaseTest {

    @Mock
    private ConditionAdapterBuilder<TestEntity, TestPropertyRef> conditionBuilder;

    @Mock
    private ConditionAdapter<TestEntity> conditionAdapter;

    @Mock
    private ConditionAdapter<TestEntity> otherConditionAdapter;

    private ContextAdapter<TestEntity, TestPropertyRef> contextAdapter;

    @BeforeEach
    void setUp() {
        contextAdapter = new ContextAdapter<>(conditionBuilder);
    }

    @Test
    void testSpringConditionAdapterWithNullSpecification() {
        // Arrange
        ConditionAdapter<TestEntity> adapter = new ConditionAdapter<>(null);

        // Act & Assert - Le constructeur n'effectue pas de validation null
        assertDoesNotThrow(() -> {
            adapter.getSpecification();
        });
    }

    @Test
    void testSpringConditionAdapterAndWithNullCondition() {
        // Arrange
        ConditionAdapter<TestEntity> adapter = new ConditionAdapter<>(mock(org.springframework.data.jpa.domain.Specification.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.and(null);
        });
    }

    @Test
    void testSpringConditionAdapterAndWithNonSpringCondition() {
        // Arrange
        ConditionAdapter<TestEntity> adapter = new ConditionAdapter<>(mock(org.springframework.data.jpa.domain.Specification.class));
        Condition nonSpringCondition = mock(Condition.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.and(nonSpringCondition);
        });
    }

    @Test
    void testSpringConditionAdapterOrWithNullCondition() {
        // Arrange
        ConditionAdapter<TestEntity> adapter = new ConditionAdapter<>(mock(org.springframework.data.jpa.domain.Specification.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.or(null);
        });
    }

    @Test
    void testSpringConditionAdapterOrWithNonSpringCondition() {
        // Arrange
        ConditionAdapter<TestEntity> adapter = new ConditionAdapter<>(mock(org.springframework.data.jpa.domain.Specification.class));
        Condition nonSpringCondition = mock(Condition.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.or(nonSpringCondition);
        });
    }

    @Test
    void testSpringConditionAdapterNot() {
        // Arrange
        org.springframework.data.jpa.domain.Specification<TestEntity> spec = mock(org.springframework.data.jpa.domain.Specification.class);
        ConditionAdapter<TestEntity> adapter = new ConditionAdapter<>(spec);

        // Act
        Condition notCondition = adapter.not();

        // Assert
        assertNotNull(notCondition);
        assertTrue(notCondition instanceof ConditionAdapter);
    }

    @Test
    void testPathResolverUtilsWithNullRoot() {
        // Act & Assert - PathResolverUtils lance une IllegalArgumentException pour un root null
        assertThrows(IllegalArgumentException.class, () -> {
            PathResolverUtils.resolvePath(null, "testPath");
        });
    }

    @Test
    void testPathResolverUtilsWithNullPath() {
        // Arrange
        jakarta.persistence.criteria.Root<TestEntity> root = mock(jakarta.persistence.criteria.Root.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PathResolverUtils.resolvePath(root, null);
        });
    }

    @Test
    void testPathResolverUtilsWithEmptyPath() {
        // Arrange
        jakarta.persistence.criteria.Root<TestEntity> root = mock(jakarta.persistence.criteria.Root.class);

        // Act & Assert - PathResolverUtils a des problèmes avec les types génériques
        assertThrows(IllegalArgumentException.class, () -> {
            PathResolverUtils.resolvePath(root, "");
        });
    }

    @Test
    void testSpecificationBuilderWithNullFilterRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            SpecificationBuilder.toSpecification(null);
        });
    }

    @Test
    void testSpecificationBuilderWithEmptyFilters() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Collections.emptyMap(),
            "true"
        );

        // Act & Assert - L'expression "true" fait référence à un filtre inexistant
        assertThrows(DSLSyntaxException.class, () -> {
            SpecificationBuilder.toSpecification(filterRequest);
        });
    }

    @Test
    void testSpecificationBuilderWithInvalidDSL() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filterName", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value")
            ),
            "invalid DSL syntax"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            SpecificationBuilder.toSpecification(filterRequest);
        });
    }

    @Test
    void testSpecificationBuilderWithNonExistentFilterKey() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filterName", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value")
            ),
            "nonExistentFilter"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            SpecificationBuilder.toSpecification(filterRequest);
        });
    }

    @Test
    void testSpringContextAdapterWithNullBuilder() {
        // Act
        ContextAdapter<TestEntity, TestPropertyRef> adapter =
            new ContextAdapter<>(null);

        // Assert
        assertNotNull(adapter);
        // Le constructeur ne vérifie pas les nulls, donc pas d'exception
    }

    @Test
    void testSpringContextAdapterAddConditionWithNullDefinition() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            contextAdapter.addCondition("key", null);
        });
    }

    @Test
    void testSpringContextAdapterAddConditionWithEmptyKey() {
        // Arrange
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value"
        );

        // Act
        contextAdapter.addCondition("", filterDef);

        // Assert - SpringContextAdapter accepte les clés vides mais la condition n'existe pas
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.getCondition("");
        });
    }

    @Test
    void testSpringContextAdapterAddConditionWithWhitespaceKey() {
        // Arrange
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value"
        );

        // Act
        contextAdapter.addCondition("   ", filterDef);

        // Assert - SpringContextAdapter accepte les clés avec des espaces mais la condition n'existe pas
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.getCondition("   ");
        });
    }

    @Test
    void testSpringContextAdapterGetSpecificationWithNonExistentKey() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            org.springframework.data.jpa.domain.Specification<TestEntity> spec =
                    contextAdapter.getSpecification("nonExistent");
        });
    }

    @Test
    void testSpringContextAdapterGetConditionWithNonExistentKey() {
        // Act & Assert - SpringContextAdapter lance une exception pour les clés inexistantes
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.getCondition("nonExistent");
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithUnsupportedOperator() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};
        jakarta.persistence.criteria.Root<TestEntity> root = mock(jakarta.persistence.criteria.Root.class);
        jakarta.persistence.criteria.CriteriaQuery<?> query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);

        // Act
        ConditionAdapter<TestEntity> adapter = builder.build(TestPropertyRef.TEST_FIELD, Operator.GREATER_THAN, "stringValue");

        // Assert - La validation se fait lors de l'utilisation de la spécification
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.getSpecification().toPredicate(root, query, cb);
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithNullValue() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Act
        ConditionAdapter<TestEntity> adapter =
            builder.build(TestPropertyRef.TEST_FIELD, Operator.IS_NULL, null);

        // Assert
        assertNotNull(adapter);
    }

    @Test
    void testSpringConditionAdapterBuilderWithEmptyCollection() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Act & Assert - Le builder n'effectue pas de validation des collections vides
        assertDoesNotThrow(() -> {
            builder.build(TestPropertyRef.TEST_FIELD, Operator.IN, Collections.emptyList());
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithInvalidBetweenValues() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Act & Assert - Le builder n'effectue pas de validation du nombre d'éléments
        assertDoesNotThrow(() -> {
            builder.build(TestPropertyRef.TEST_FIELD, Operator.BETWEEN, Arrays.asList("singleValue"));
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithTooManyBetweenValues() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Act & Assert - Le builder n'effectue pas de validation du nombre d'éléments
        assertDoesNotThrow(() -> {
            builder.build(TestPropertyRef.TEST_FIELD, Operator.BETWEEN, 
                Arrays.asList("value1", "value2", "value3"));
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithNullBetweenValues() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Act & Assert - Le builder n'effectue pas de validation null
        assertDoesNotThrow(() -> {
            builder.build(TestPropertyRef.TEST_FIELD, Operator.BETWEEN, null);
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithInvalidTypeCast() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Act & Assert - Le builder n'effectue pas de validation de type au niveau du cast
        assertDoesNotThrow(() -> {
            builder.build(TestPropertyRef.TEST_FIELD, Operator.GREATER_THAN, "stringValue");
        });
    }

    // Test entity class
    static class TestEntity {
        private String testField;
        
        public String getTestField() { return testField; }
        public void setTestField(String testField) { this.testField = testField; }
    }

    // Test property reference enum
    enum TestPropertyRef implements PropertyRef, PathShape {
        TEST_FIELD("testField", String.class, Set.of(
            Operator.EQUALS, Operator.NOT_EQUALS,
            Operator.LIKE, Operator.NOT_LIKE,
            Operator.IN, Operator.NOT_IN,
            Operator.IS_NULL, Operator.IS_NOT_NULL
        ));

        private final String path;
        private final Class<?> type;
        private final Set<Operator> supportedOperators;

        TestPropertyRef(String path, Class<?> type, Set<Operator> supportedOperators) {
            this.path = path;
            this.type = type;
            this.supportedOperators = supportedOperators;
        }

        @Override
        public String getPath() {
            return path;
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
            
            // Validation spécifique pour certains opérateurs
            switch (operator) {
                case IN:
                case NOT_IN:
                    if (value == null || !(value instanceof Collection) || ((Collection<?>) value).isEmpty()) {
                        throw new IllegalArgumentException("IN/NOT_IN operator requires a non-empty collection");
                    }
                    break;
                case BETWEEN:
                case NOT_BETWEEN:
                    if (value == null || !(value instanceof Collection)) {
                        throw new IllegalArgumentException("BETWEEN operator requires a collection");
                    }
                    Collection<?> values = (Collection<?>) value;
                    if (values.size() != 2) {
                        throw new IllegalArgumentException("BETWEEN operator requires exactly 2 values");
                    }
                    break;
            }
        }
    }
}


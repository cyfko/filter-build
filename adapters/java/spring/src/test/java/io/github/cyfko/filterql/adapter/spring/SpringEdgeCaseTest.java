package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.utils.PathResolverUtils;
import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
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
    private FilterCondition<TestEntity> filterCondition;

    @Mock
    private FilterCondition<TestEntity> otherFilterCondition;

    private FilterContext<TestEntity,TestPropertyRef> context;

    @BeforeEach
    void setUp() {
        context = new FilterContext<>(TestEntity.class,TestPropertyRef.class, p -> switch (p) {
                case TEST_FIELD -> "testField";
        });
    }

    @Test
    void testSpringConditionAdapterWithNullSpecification() {
        // Arrange
        FilterCondition<TestEntity> adapter = new FilterCondition<>(null);

        // Act & Assert - Le constructeur n'effectue pas de validation null
        assertDoesNotThrow(() -> {
            adapter.getSpecification();
        });
    }

    @Test
    void testSpringConditionAdapterAndWithNullCondition() {
        // Arrange
        FilterCondition<TestEntity> adapter = new FilterCondition<>(mock(org.springframework.data.jpa.domain.Specification.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.and(null);
        });
    }

    @Test
    void testSpringConditionAdapterAndWithNonSpringCondition() {
        // Arrange
        FilterCondition<TestEntity> adapter = new FilterCondition<>(mock(org.springframework.data.jpa.domain.Specification.class));
        Condition nonSpringCondition = mock(Condition.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.and(nonSpringCondition);
        });
    }

    @Test
    void testSpringConditionAdapterOrWithNullCondition() {
        // Arrange
        FilterCondition<TestEntity> adapter = new FilterCondition<>(mock(org.springframework.data.jpa.domain.Specification.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.or(null);
        });
    }

    @Test
    void testSpringConditionAdapterOrWithNonSpringCondition() {
        // Arrange
        FilterCondition<TestEntity> adapter = new FilterCondition<>(mock(org.springframework.data.jpa.domain.Specification.class));
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
        FilterCondition<TestEntity> adapter = new FilterCondition<>(spec);

        // Act
        Condition notCondition = adapter.not();

        // Assert
        assertNotNull(notCondition);
        assertTrue(notCondition instanceof FilterCondition);
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
    void testSpecificationBuilderWithNullFilterRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            FilterResolver.of(context).resolve(TestEntity.class, null);
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
            FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        });
    }

    @Test
    void testSpecificationBuilderWithInvalidDSL() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filterName", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQUALS, "value")
            ),
            "invalid DSL syntax"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        });
    }

    @Test
    void testSpecificationBuilderWithNonExistentFilterKey() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filterName", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQUALS, "value")
            ),
            "nonExistentFilter"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        });
    }

    @Test
    void testFilterContextAddConditionWithNullDefinition() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            context.addCondition("key", null);
        });
    }

    @Test
    void testFilterContextAddConditionWithEmptyKey() {
        // Arrange
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Op.EQUALS, "value"
        );

        // Act & Assert - Empty keys should be rejected
        assertThrows(IllegalArgumentException.class, () -> {
            context.addCondition("", filterDef);
        }, "Filter key cannot be null or empty");
    }

    @Test
    void testFilterContextAddConditionWithWhitespaceKey() {
        // Arrange
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Op.EQUALS, "value"
        );

        // Act & Assert - Whitespace-only keys should be rejected
        assertThrows(IllegalArgumentException.class, () -> {
            context.addCondition("   ", filterDef);
        }, "Filter key cannot be null or empty");
    }

    @Test
    void testFilterContextGetConditionWithNonExistentKey() {
        // Act & Assert - SpringContextAdapter lance une exception pour les clés inexistantes
        assertThrows(IllegalArgumentException.class, () -> {
            context.getCondition("nonExistent");
        });
    }

    @Test
    void testFilterContextWithUnsupportedOperator() {
        // Arrange
        FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.GREATER_THAN, "stringValue");

        // Assert - La validation se fait lors de l'utilisation de la création de la condition
        assertThrows(FilterValidationException.class, () -> context.addCondition("someKey", definition));
    }

    @Test
    void testFilterContextWithNullValue() {
        // Arrange
        FilterDefinition<TestPropertyRef> definition = new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.IS_NULL, null);

        // Act
        context.addCondition("someKey", definition);
        Condition condition = context.getCondition("someKey");

        // Assert
        assertNotNull(condition);
    }

    @Test
    void testFilterContextWithEmptyCollection() {
        assertThrows(FilterValidationException.class, () -> {
            context.addCondition("someKey", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.IN, Collections.emptyList()));
        });
    }

    @Test
    void testFilterContextWithInvalidBetweenValues() {
        // Act & Assert - Le builder n'effectue pas de validation du nombre d'éléments
        assertThrows(FilterValidationException.class, () -> {
            context.addCondition("someKey", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.BETWEEN, Arrays.asList("singleValue")));
        });
    }

    @Test
    void testFilterContextWithTooManyBetweenValues() {
        // Act & Assert - Le builder n'effectue pas de validation du nombre d'éléments
        assertThrows(FilterValidationException.class, () -> {
            context.addCondition("someKey", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.BETWEEN,
                Arrays.asList("value1", "value2", "value3")));
        });
    }

    @Test
    void testFilterContextWithNullBetweenValues() {
        // Act & Assert - Le builder n'effectue pas de validation null
        assertThrows(FilterValidationException.class, () -> {
            context.addCondition("someKey", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.BETWEEN, null));
        });
    }

    @Test
    void testFilterContextWithInvalidTypeCast() {
        // Act & Assert - Le builder n'effectue pas de validation de type au niveau du cast
        assertThrows(FilterValidationException.class, () -> {
            context.addCondition("someKey", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.GREATER_THAN, "stringValue"));
        });
    }

    // Test entity class
    static class TestEntity {
        private String testField;
        
        public String getTestField() { return testField; }
        public void setTestField(String testField) { this.testField = testField; }
    }

    // Test property reference enum
    enum TestPropertyRef implements PropertyReference {
        TEST_FIELD(String.class, Set.of(
            Op.EQUALS, Op.NOT_EQUALS,
            Op.LIKE, Op.NOT_LIKE,
            Op.IN, Op.NOT_IN,
            Op.IS_NULL, Op.IS_NOT_NULL
        ));

        private final Class<?> type;
        private final Set<Op> supportedOperators;

        TestPropertyRef(Class<?> type, Set<Op> supportedOperators) {
            this.type = type;
            this.supportedOperators = supportedOperators;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Set<Op> getSupportedOperators() {
            return supportedOperators;
        }
    }
}


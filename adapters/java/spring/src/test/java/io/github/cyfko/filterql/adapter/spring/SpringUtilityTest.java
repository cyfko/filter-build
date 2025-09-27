package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour les utilitaires et helpers de l'adaptateur Spring.
 * Teste les méthodes utilitaires et les classes d'aide.
 */
@ExtendWith(MockitoExtension.class)
class SpringUtilityTest {

    @Mock
    private Root<TestEntity> root;
    
    @Mock
    private CriteriaBuilder cb;
    
    @Mock
    private Path<String> stringPath;
    
    @Mock
    private Path<Number> numberPath;
    
    @Mock
    private Predicate predicate;

    // Les tests de PathResolverUtils sont maintenant dans PathResolverUtilsTest
    // pour éviter la duplication et assurer la cohérence

    @Test
    void testSpecificationBuilderWithSimpleDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "testFilter", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value")
            ),
            "testFilter"
        );

        // Act
        Specification<TestEntity> spec = SpecificationBuilder.toSpecification(filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithAndDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value1"),
                "filter2", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.LIKE, "%value2%")
            ),
            "filter1 & filter2"
        );

        // Act
        Specification<TestEntity> spec = SpecificationBuilder.toSpecification(filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithOrDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value1"),
                "filter2", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value2")
            ),
            "filter1 | filter2"
        );

        // Act
        Specification<TestEntity> spec = SpecificationBuilder.toSpecification(filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithNotDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value")
            ),
            "! filter1"
        );

        // Act
        Specification<TestEntity> spec = SpecificationBuilder.toSpecification(filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithComplexDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value1"),
                "filter2",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value2"),
                "filter3",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value3")
            ),
            "(filter1 & filter2) | filter3"
        );

        // Act
        Specification<TestEntity> spec = SpecificationBuilder.toSpecification(filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithParenthesesDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value1"),
                "filter2",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value2"),
                "filter3",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value3"),
                "filter4",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value4")
            ),
            "((filter1 & filter2) | (filter3 & filter4))"
        );

        // Act
        Specification<TestEntity> spec = SpecificationBuilder.toSpecification(filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithInvalidDSL() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filterName",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value")
            ),
            "invalid syntax"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            SpecificationBuilder.toSpecification(filterRequest);
        });
    }

    @Test
    void testSpecificationBuilderWithUnbalancedParentheses() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value1"),
                "filter2",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value2")
            ),
            "(filter1 AND filter2"
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
                "filter", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value")
            ),
            "nonExistentFilter"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            SpecificationBuilder.toSpecification(filterRequest);
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithAllOperators() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Test all supported operators
        Operator[] operators = {
            Operator.EQUALS, Operator.NOT_EQUALS,
            Operator.LIKE, Operator.NOT_LIKE,
            Operator.IN, Operator.NOT_IN,
            Operator.IS_NULL, Operator.IS_NOT_NULL
        };

        for (Operator operator : operators) {
            // Act
            ConditionAdapter<TestEntity> adapter = builder.build(
                TestPropertyRef.TEST_FIELD, 
                operator, 
                getTestValueForOperator(operator)
            );

            // Assert
            assertNotNull(adapter);
            assertNotNull(adapter.getSpecification());
        }
    }

    @Test
    void testSpringConditionAdapterBuilderWithDifferentValueTypes() {
        // Arrange
        ConditionAdapterBuilder<TestEntity, TestPropertyRef> builder =
            new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        // Test with different value types
        Object[] values = {
            "stringValue",
            Arrays.asList("value1", "value2"),
            null
        };

        for (Object value : values) {
            // Act
            ConditionAdapter<TestEntity> adapter = builder.build(
                TestPropertyRef.TEST_FIELD, 
                Operator.EQUALS, 
                value
            );

            // Assert
            assertNotNull(adapter);
            assertNotNull(adapter.getSpecification());
        }
    }

    @Test
    void testSpringContextAdapterWithMultiplePropertyRefs() {
        // Arrange
        ContextAdapter<TestEntity, TestPropertyRef> contextAdapter =
            new ContextAdapter<>(new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {});

        // Act
        contextAdapter.addCondition("filter1", new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value1"
        ));
        contextAdapter.addCondition("filter2", new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Operator.LIKE, "%value2%"
        ));

        // Assert
        assertNotNull(contextAdapter.getCondition("filter1"));
        assertNotNull(contextAdapter.getCondition("filter2"));
        assertNotNull(contextAdapter.getSpecification("filter1"));
        assertNotNull(contextAdapter.getSpecification("filter2"));
    }

    @Test
    void testSpringContextAdapterWithOverwriteCondition() {
        // Arrange
        ContextAdapter<TestEntity, TestPropertyRef> contextAdapter =
            new ContextAdapter<>(new ConditionAdapterBuilder<TestEntity, TestPropertyRef>() {});

        // Act
        contextAdapter.addCondition("filter1", new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Operator.EQUALS, "value1"
        ));
        contextAdapter.addCondition("filter1", new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Operator.LIKE, "%value2%"
        ));

        // Assert
        Condition condition = contextAdapter.getCondition("filter1");
        assertNotNull(condition);
        // The condition should be the second one (overwritten)
    }

    private Object getTestValueForOperator(Operator operator) {
        switch (operator) {
            case EQUALS:
            case NOT_EQUALS:
                return "testValue";
            case LIKE:
            case NOT_LIKE:
                return "%test%";
            case IN:
            case NOT_IN:
                return Arrays.asList("value1", "value2");
            case IS_NULL:
            case IS_NOT_NULL:
                return null;
            default:
                return "defaultValue";
        }
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
            // Additional value validation could be added here
        }
    }
}

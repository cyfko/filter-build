package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private FilterContext<TestEntity,TestPropertyRef> context;

    @BeforeEach
    void setUp() {
        context = new FilterContext<>(TestEntity.class, TestPropertyRef.class, def -> switch (def.ref()){
            case TEST_FIELD -> "testField";
        });
    }

    @Test
    void testSpecificationBuilderWithSimpleDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "testFilter", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value")
            ),
            "testFilter"
        );

        // Act
        PredicateResolver<TestEntity> spec = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithAndDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value1"),
                "filter2", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.MATCHES, "%value2%")
            ),
            "filter1 & filter2"
        );

        // Act
        PredicateResolver<TestEntity> spec = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithOrDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value1"),
                "filter2", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value2")
            ),
            "filter1 | filter2"
        );

        // Act
        PredicateResolver<TestEntity> spec = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithNotDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value")
            ),
            "! filter1"
        );

        // Act
        PredicateResolver<TestEntity> spec = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithComplexDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value1"),
                "filter2",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value2"),
                "filter3",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value3")
            ),
            "(filter1 & filter2) | filter3"
        );

        // Act
        PredicateResolver<TestEntity> spec = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithParenthesesDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value1"),
                "filter2",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value2"),
                "filter3",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value3"),
                "filter4",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value4")
            ),
            "((filter1 & filter2) | (filter3 & filter4))"
        );

        // Act
        PredicateResolver<TestEntity> spec = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);

        // Assert
        assertNotNull(spec);
    }

    @Test
    void testSpecificationBuilderWithInvalidDSL() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filterName",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value")
            ),
            "invalid syntax"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        });
    }

    @Test
    void testSpecificationBuilderWithUnbalancedParentheses() {
        // Arrange
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "filter1",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value1"),
                "filter2",new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value2")
            ),
            "(filter1 AND filter2"
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
                "filter", new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, "value")
            ),
            "nonExistentFilter"
        );

        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        });
    }

    @Test
    void testSpringConditionAdapterBuilderWithAllOperators() {

        // Test all supported operators
        Op[] operators = {
            Op.EQ, Op.NE,
            Op.MATCHES, Op.NOT_MATCHES,
            Op.IN, Op.NOT_IN,
            Op.IS_NULL, Op.NOT_NULL
        };

        for (Op operator : operators) {
            // Act
            Condition condition = context.addCondition("key",
                    new FilterDefinition<>(TestPropertyRef.TEST_FIELD, operator, getTestValueForOperator(operator))
            );

            // Assert
            assertNotNull(condition);
            assertNotNull(context.toResolver(TestEntity.class, condition));
        }
    }

    @Test
    void testSpringConditionAdapterBuilderWithDifferentValueTypes() {
        // Test with different value types
        Object[] values = {
            "stringValue",
            Arrays.asList("value1", "value2"),
            null
        };

        for (Object value : values) {
            // Act
            Condition condition = context.addCondition("key", 
                    new FilterDefinition<>(TestPropertyRef.TEST_FIELD, Op.EQ, value)
            );

            // Assert
            assertNotNull(condition);
            assertNotNull(context.toResolver(TestEntity.class, condition));
        }
    }

    @Test
    void testSpringContextAdapterWithMultiplePropertyRefs() {
        // Act
        Condition condition1 = context.addCondition("filter1", new FilterDefinition<>(
                TestPropertyRef.TEST_FIELD, Op.EQ, "value1"
        ));
        Condition condition2 = context.addCondition("filter2", new FilterDefinition<>(
                TestPropertyRef.TEST_FIELD, Op.MATCHES, "%value2%"
        ));

        // Assert
        assertNotNull(condition1);
        assertNotNull(condition2);
        assertNotNull(context.toResolver(TestEntity.class, condition1));
        assertNotNull(context.toResolver(TestEntity.class, condition2));
    }

    @Test
    void testSpringContextAdapterWithOverwriteCondition() {
        // Act
        context.addCondition("filter1", new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Op.EQ, "value1"
        ));
        context.addCondition("filter1", new FilterDefinition<>(
            TestPropertyRef.TEST_FIELD, Op.MATCHES, "%value2%"
        ));

        // Assert
        Condition condition = context.getCondition("filter1");
        assertNotNull(condition);
        // The condition should be the second one (overwritten)
    }

    private Object getTestValueForOperator(Op operator) {
        return switch (operator) {
            case EQ, NE -> "testValue";
            case MATCHES, NOT_MATCHES -> "%test%";
            case IN, NOT_IN -> Arrays.asList("value1", "value2");
            case IS_NULL, NOT_NULL -> null;
            default -> "defaultValue";
        };
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
            Op.EQ, Op.NE,
            Op.MATCHES, Op.NOT_MATCHES,
            Op.IN, Op.NOT_IN,
            Op.IS_NULL, Op.NOT_NULL
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

        @Override
        public void validateOperator(Op operator) {
            if (!supportedOperators.contains(operator)) {
                throw new IllegalArgumentException("Op " + operator + " not supported for " + this);
            }
        }

        @Override
        public void validateOperatorForValue(Op operator, Object value) {
            validateOperator(operator);
            // Additional value validation could be added here
        }
    }
}

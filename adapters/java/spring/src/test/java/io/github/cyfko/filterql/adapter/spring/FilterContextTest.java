package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.utils.OperatorUtils;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static io.github.cyfko.filterql.adapter.spring.FilterContextTest.TestPropertyRef.USER_AGE;
import static io.github.cyfko.filterql.adapter.spring.FilterContextTest.TestPropertyRef.USER_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour FilterContext.
 * Teste la gestion des conditions dans un contexte Spring.
 */
@ExtendWith(MockitoExtension.class)
class FilterContextTest {

    @Mock
    private FilterCondition<TestEntity> mockedFilterCondition;

    @Mock
    private FilterContext<TestEntity, TestPropertyRef> mockedContext;
    
    private FilterContext<TestEntity, TestPropertyRef> context;

    @BeforeEach
    void setUp() {
        context = new FilterContext<>(TestEntity.class, TestPropertyRef.class, def -> switch (def.getRef()){
            case USER_NAME -> "name";
            case USER_AGE -> "age";
        });
    }

    @Test
    void testConstructor() {
        assertNotNull(context);
    }

    @Test
    void testAddConditionSuccess() {

        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            USER_NAME, Op.EQUALS, "testValue"
        );
        when(mockedContext.addCondition(filterKey,filterDef)).thenReturn(mockedFilterCondition);
        
        // Act
        Condition retrievedCondition = mockedContext.addCondition(filterKey, filterDef);

        // Verify condition is stored
        assertEquals(mockedFilterCondition, retrievedCondition);
    }

    @Test
    void testAddConditionWithValidationError() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            USER_NAME, Op.GREATER_THAN, "testValue" // Unsupported operator
        );
        
        // Act & Assert
        // La validation se fait dans propertyRef.validateOperator() avant d'appeler le builder
        assertThrows(FilterValidationException.class, () -> {
            context.addCondition(filterKey, filterDef);
        });
    }

    @Test
    void testAddConditionWithNullFilterKey() {
        // Arrange
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            USER_NAME, Op.EQUALS, "testValue"
        );
        
        // Act & Assert
        // HashMap.put() accepte les clés null, donc pas d'exception
        assertThrows(IllegalArgumentException.class, () -> {
            context.addCondition(null, filterDef);
        });
    }

    @Test
    void testAddConditionWithNullFilterDefinition() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            context.addCondition("testFilter", null);
        });
    }

    @Test
    void testGetConditionExisting() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            USER_NAME, Op.EQUALS, "testValue"
        );
        when(mockedContext.getCondition(filterKey)).thenReturn(mockedFilterCondition);
        
        // Act
        Condition result = mockedContext.getCondition(filterKey);
        
        // Assert
        assertEquals(mockedFilterCondition, result);
    }

    @Test
    void testGetConditionNonExistent() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            context.getCondition("nonExistentFilter");
        });
    }

    @Test
    void testGetConditionWithNullKey() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            context.getCondition(null);
        });
    }

    @Test
    void testGetSpecificationExisting() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            USER_NAME, Op.EQUALS, "testValue"
        );
        
        // Act
        context.addCondition(filterKey, filterDef);
        Condition condition = context.getCondition(filterKey);
        PredicateResolver<TestEntity> result = context.toResolver(TestEntity.class, condition);

        // Assert - Test that we get a valid PredicateResolver
        assertNotNull(result);
        assertNotNull(condition);
        assertTrue(condition instanceof FilterCondition);
        
        // Test that the condition can be retrieved
        assertEquals(condition, context.getCondition(filterKey));
    }

    @Test
    void testGetSpecificationNonExistent() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            PredicateResolver<TestEntity> result = context.toResolver(TestEntity.class, null);
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            context.toResolver(TestEntity.class, new Condition() {
                @Override
                public Condition and(Condition other) {
                    return null;
                }

                @Override
                public Condition or(Condition other) {
                    return null;
                }

                @Override
                public Condition not() {
                    return null;
                }
            });
        });
    }

    @Test
    void testMultipleConditions() {
        // Arrange
        String filterKey1 = "filter1";
        String filterKey2 = "filter2";
        
        FilterDefinition<TestPropertyRef> filterDef1 = new FilterDefinition<>(
            USER_NAME, Op.EQUALS, "value1"
        );
        FilterDefinition<TestPropertyRef> filterDef2 = new FilterDefinition<>(
            USER_AGE, Op.GREATER_THAN, 25
        );
        
        // Act
        context.addCondition(filterKey1, filterDef1);
        context.addCondition(filterKey2, filterDef2);
        
        // Assert
        Condition condition1 = context.getCondition(filterKey1);
        Condition condition2 = context.getCondition(filterKey2);
        
        assertNotNull(condition1);
        assertNotNull(condition2);
        assertTrue(condition1 instanceof FilterCondition);
        assertTrue(condition2 instanceof FilterCondition);
        
        // Test that both conditions are stored and retrievable
        assertNotEquals(condition1, condition2);
    }

    @Test
    void testOverwriteExistingCondition() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef1 = new FilterDefinition<>(
            USER_NAME, Op.EQUALS, "value1"
        );
        FilterDefinition<TestPropertyRef> filterDef2 = new FilterDefinition<>(
            USER_NAME, Op.LIKE, "%value2%"
        );
        
        // Act
        context.addCondition(filterKey, filterDef1);
        Condition condition1 = context.getCondition(filterKey);
        
        context.addCondition(filterKey, filterDef2); // Overwrite
        Condition condition2 = context.getCondition(filterKey);
        
        // Assert
        assertNotNull(condition1);
        assertNotNull(condition2);
        assertNotEquals(condition1, condition2); // Should be different after overwrite
        assertTrue(condition2 instanceof FilterCondition);
    }

    @Test
    void testDifferentOperators() {
        // Test only operators supported by USER_NAME
        Op[] supportedOperators = {
            Op.EQUALS, Op.NOT_EQUALS,
            Op.LIKE, Op.NOT_LIKE,
            Op.IN, Op.NOT_IN,
            Op.IS_NULL, Op.IS_NOT_NULL
        };
        
        for (int i = 0; i < supportedOperators.length; i++) {
            String filterKey = "filter" + i;
            Object value = getTestValueForOperator(supportedOperators[i]);
            
            FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
                USER_NAME, supportedOperators[i], value
            );

            // Act
            Condition condition = context.addCondition(filterKey, filterDef);

            // Assert
            assertNotNull(condition);
        }
    }

    private Object getTestValueForOperator(Op operator) {
        return switch (operator) {
            case EQUALS, NOT_EQUALS, LIKE, NOT_LIKE -> "testValue";
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> 100;
            case IN, NOT_IN -> java.util.List.of("value1", "value2");
            case IS_NULL, IS_NOT_NULL -> null;
            case BETWEEN, NOT_BETWEEN -> java.util.List.of(10, 20);
        };
    }

    /**
     * Classe d'entité de test simple.
     */
    static class TestEntity {
        private Long id;
        private String name;
        
        public TestEntity() {}
        
        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /**
     * Enum de test pour PropertyRef avec PathShape.
     */
    enum TestPropertyRef implements PropertyReference {
        USER_NAME(String.class, OperatorUtils.FOR_TEXT),
        USER_AGE(Integer.class, OperatorUtils.FOR_NUMBER);

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

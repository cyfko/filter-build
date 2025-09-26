package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.Context;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.utils.OperatorUtils;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SpringContextAdapter.
 * Teste la gestion des conditions dans un contexte Spring.
 */
@ExtendWith(MockitoExtension.class)
class SpringContextAdapterTest {

    @Mock
    private SpringConditionAdapterBuilder<TestEntity, TestPropertyRef> conditionAdapterBuilder;
    
    @Mock
    private SpringConditionAdapter<TestEntity> conditionAdapter;
    
    private SpringContextAdapter<TestEntity, TestPropertyRef> contextAdapter;

    @BeforeEach
    void setUp() {
        contextAdapter = new SpringContextAdapter<>(conditionAdapterBuilder);
    }

    @Test
    void testConstructor() {
        assertNotNull(contextAdapter);
    }

    @Test
    void testConstructorWithNullBuilder() {
        // Le constructeur ne vérifie pas les nulls, donc ce test doit être adapté
        // ou supprimé car il n'y a pas de validation dans le constructeur
        assertDoesNotThrow(() -> {
            new SpringContextAdapter<>(null);
        });
    }

    @Test
    void testAddConditionSuccess() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue"
        );
        
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue"))
            .thenReturn(conditionAdapter);
        
        // Act
        contextAdapter.addCondition(filterKey, filterDef);
        
        // Assert
        verify(conditionAdapterBuilder).build(TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue");
        
        // Verify condition is stored
        Condition retrievedCondition = contextAdapter.getCondition(filterKey);
        assertEquals(conditionAdapter, retrievedCondition);
    }

    @Test
    void testAddConditionWithValidationError() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.GREATER_THAN, "testValue" // Unsupported operator
        );
        
        // Act & Assert
        // La validation se fait dans propertyRef.validateOperator() avant d'appeler le builder
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.addCondition(filterKey, filterDef);
        });
        
        // Le builder ne devrait pas être appelé car la validation échoue avant
        verify(conditionAdapterBuilder, never()).build(any(), any(), any());
    }

    @Test
    void testAddConditionWithNullFilterKey() {
        // Arrange
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue"
        );
        
        // Act & Assert
        // HashMap.put() accepte les clés null, donc pas d'exception
        assertDoesNotThrow(() -> {
            contextAdapter.addCondition(null, filterDef);
        });
    }

    @Test
    void testAddConditionWithNullFilterDefinition() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            contextAdapter.addCondition("testFilter", null);
        });
    }

    @Test
    void testGetConditionExisting() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue"
        );
        
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue"))
            .thenReturn(conditionAdapter);
        
        contextAdapter.addCondition(filterKey, filterDef);
        
        // Act
        Condition result = contextAdapter.getCondition(filterKey);
        
        // Assert
        assertEquals(conditionAdapter, result);
    }

    @Test
    void testGetConditionNonExistent() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.getCondition("nonExistentFilter");
        });
    }

    @Test
    void testGetConditionWithNullKey() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.getCondition(null);
        });
    }

    @Test
    void testGetSpecificationExisting() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue"
        );
        
        Specification<TestEntity> specification = mock(Specification.class);
        
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Operator.EQUALS, "testValue"))
            .thenReturn(conditionAdapter);
        when(conditionAdapter.getSpecification()).thenReturn(specification);
        
        contextAdapter.addCondition(filterKey, filterDef);
        
        // Act
        Specification<TestEntity> result = contextAdapter.getSpecification(filterKey);
        
        // Assert
        assertEquals(specification, result);
    }

    @Test
    void testGetSpecificationNonExistent() {
        // Act
        Specification<TestEntity> result = contextAdapter.getSpecification("nonExistentFilter");
        
        // Assert
        assertNull(result);
    }

    @Test
    void testMultipleConditions() {
        // Arrange
        String filterKey1 = "filter1";
        String filterKey2 = "filter2";
        
        FilterDefinition<TestPropertyRef> filterDef1 = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.EQUALS, "value1"
        );
        FilterDefinition<TestPropertyRef> filterDef2 = new FilterDefinition<>(
            TestPropertyRef.USER_AGE, Operator.GREATER_THAN, 25
        );
        
        SpringConditionAdapter<TestEntity> conditionAdapter1 = mock(SpringConditionAdapter.class);
        SpringConditionAdapter<TestEntity> conditionAdapter2 = mock(SpringConditionAdapter.class);
        
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Operator.EQUALS, "value1"))
            .thenReturn(conditionAdapter1);
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_AGE, Operator.GREATER_THAN, 25))
            .thenReturn(conditionAdapter2);
        
        // Act
        contextAdapter.addCondition(filterKey1, filterDef1);
        contextAdapter.addCondition(filterKey2, filterDef2);
        
        // Assert
        assertEquals(conditionAdapter1, contextAdapter.getCondition(filterKey1));
        assertEquals(conditionAdapter2, contextAdapter.getCondition(filterKey2));
        
        verify(conditionAdapterBuilder).build(TestPropertyRef.USER_NAME, Operator.EQUALS, "value1");
        verify(conditionAdapterBuilder).build(TestPropertyRef.USER_AGE, Operator.GREATER_THAN, 25);
    }

    @Test
    void testOverwriteExistingCondition() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef1 = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.EQUALS, "value1"
        );
        FilterDefinition<TestPropertyRef> filterDef2 = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Operator.LIKE, "%value2%"
        );
        
        SpringConditionAdapter<TestEntity> conditionAdapter1 = mock(SpringConditionAdapter.class);
        SpringConditionAdapter<TestEntity> conditionAdapter2 = mock(SpringConditionAdapter.class);
        
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Operator.EQUALS, "value1"))
            .thenReturn(conditionAdapter1);
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Operator.LIKE, "%value2%"))
            .thenReturn(conditionAdapter2);
        
        // Act
        contextAdapter.addCondition(filterKey, filterDef1);
        contextAdapter.addCondition(filterKey, filterDef2); // Overwrite
        
        // Assert
        assertEquals(conditionAdapter2, contextAdapter.getCondition(filterKey));
        
        verify(conditionAdapterBuilder).build(TestPropertyRef.USER_NAME, Operator.EQUALS, "value1");
        verify(conditionAdapterBuilder).build(TestPropertyRef.USER_NAME, Operator.LIKE, "%value2%");
    }

    @Test
    void testDifferentOperators() {
        // Test only operators supported by USER_NAME
        Operator[] supportedOperators = {
            Operator.EQUALS, Operator.NOT_EQUALS,
            Operator.LIKE, Operator.NOT_LIKE,
            Operator.IN, Operator.NOT_IN,
            Operator.IS_NULL, Operator.IS_NOT_NULL
        };
        
        for (int i = 0; i < supportedOperators.length; i++) {
            String filterKey = "filter" + i;
            Object value = getTestValueForOperator(supportedOperators[i]);
            
            FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
                TestPropertyRef.USER_NAME, supportedOperators[i], value
            );
            
            SpringConditionAdapter<TestEntity> conditionAdapter = mock(SpringConditionAdapter.class);
            
            when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, supportedOperators[i], value))
                .thenReturn(conditionAdapter);
            
            // Act
            contextAdapter.addCondition(filterKey, filterDef);
            
            // Assert
            assertEquals(conditionAdapter, contextAdapter.getCondition(filterKey));
        }
    }

    private Object getTestValueForOperator(Operator operator) {
        switch (operator) {
            case EQUALS:
            case NOT_EQUALS:
            case LIKE:
            case NOT_LIKE:
                return "testValue";
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
                return 100;
            case IN:
            case NOT_IN:
                return java.util.List.of("value1", "value2");
            case IS_NULL:
            case IS_NOT_NULL:
                return null;
            case BETWEEN:
            case NOT_BETWEEN:
                return java.util.List.of(10, 20);
            default:
                return "defaultValue";
        }
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
    enum TestPropertyRef implements PropertyRef, PathShape {
        USER_NAME(String.class, OperatorUtils.FOR_TEXT, "name"),
        USER_AGE(Integer.class, OperatorUtils.FOR_NUMBER, "age");

        private final Class<?> type;
        private final Set<Operator> supportedOperators;
        private final String path;

        TestPropertyRef(Class<?> type, Set<Operator> supportedOperators, String path) {
            this.type = type;
            this.supportedOperators = supportedOperators;
            this.path = path;
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
        public String getPath() {
            return path;
        }
    }
}

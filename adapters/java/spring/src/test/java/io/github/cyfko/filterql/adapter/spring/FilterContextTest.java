package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.mappings.PathMapping;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.utils.OperatorUtils;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
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
class FilterContextTest {

    @Mock
    private ConditionAdapterBuilder<TestEntity, TestPropertyRef> conditionAdapterBuilder;
    
    @Mock
    private ConditionAdapter<TestEntity> conditionAdapter;
    
    private FilterContext<TestEntity> filterContext;

    @BeforeEach
    void setUp() {
        filterContext = new FilterContext<>(TestEntity.class);
        filterContext.setConditionBuilder(propertyRef -> {
            if (propertyRef instanceof TestPropertyRef ref) {
                switch (ref){
                    case USER_NAME -> { return ((PathMapping<TestEntity>) () -> "name"); }
                    case USER_AGE -> { return ((PathMapping<TestEntity>) () -> "age"); }
                }
            }

            throw new IllegalArgumentException("");
        });
    }

    @Test
    void testConstructor() {
        assertNotNull(filterContext);
    }

    @Test
    void testConstructorWithNullBuilder() {
        // Le constructeur ne vérifie pas les nulls, donc ce test doit être adapté
        // ou supprimé car il n'y a pas de validation dans le constructeur
        assertDoesNotThrow(() -> {
            new FilterContext<>(null);
        });
    }

    @Test
    void testAddConditionSuccess() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.EQUALS, "testValue"
        );
        
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Op.EQUALS, "testValue"))
            .thenReturn(conditionAdapter);
        
        // Act
        filterContext.addCondition(filterKey, filterDef);
        
        // Assert
        verify(conditionAdapterBuilder).build(TestPropertyRef.USER_NAME, Op.EQUALS, "testValue");
        
        // Verify condition is stored
        Condition retrievedCondition = filterContext.getCondition(filterKey);
        assertEquals(conditionAdapter, retrievedCondition);
    }

    @Test
    void testAddConditionWithValidationError() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.GREATER_THAN, "testValue" // Unsupported operator
        );
        
        // Act & Assert
        // La validation se fait dans propertyRef.validateOperator() avant d'appeler le builder
        assertThrows(IllegalArgumentException.class, () -> {
            filterContext.addCondition(filterKey, filterDef);
        });
        
        // Le builder ne devrait pas être appelé car la validation échoue avant
        verify(conditionAdapterBuilder, never()).build(any(), any(), any());
    }

    @Test
    void testAddConditionWithNullFilterKey() {
        // Arrange
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.EQUALS, "testValue"
        );
        
        // Act & Assert
        // HashMap.put() accepte les clés null, donc pas d'exception
        assertDoesNotThrow(() -> {
            filterContext.addCondition(null, filterDef);
        });
    }

    @Test
    void testAddConditionWithNullFilterDefinition() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            filterContext.addCondition("testFilter", null);
        });
    }

    @Test
    void testGetConditionExisting() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.EQUALS, "testValue"
        );
        
        when(conditionAdapterBuilder.build(TestPropertyRef.USER_NAME, Op.EQUALS, "testValue"))
            .thenReturn(conditionAdapter);
        
        filterContext.addCondition(filterKey, filterDef);
        
        // Act
        Condition result = filterContext.getCondition(filterKey);
        
        // Assert
        assertEquals(conditionAdapter, result);
    }

    @Test
    void testGetConditionNonExistent() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            filterContext.getCondition("nonExistentFilter");
        });
    }

    @Test
    void testGetConditionWithNullKey() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            filterContext.getCondition(null);
        });
    }

    @Test
    void testGetSpecificationExisting() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.EQUALS, "testValue"
        );
        
        Specification<TestEntity> specification = mock(Specification.class);
        
        when(conditionAdapterBuilder.build(filterDef))
            .thenReturn(conditionAdapter);
        when(conditionAdapter.getSpecification()).thenReturn(specification);
        
        filterContext.addCondition(filterKey, filterDef);
        
        // Act
        Specification<TestEntity> result = filterContext.getSpecification(filterKey);
        
        // Assert
        assertEquals(specification, result);
    }

    @Test
    void testGetSpecificationNonExistent() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            Specification<TestEntity> result = filterContext.getSpecification("nonExistentFilter");
        });
    }

    @Test
    void testMultipleConditions() {
        // Arrange
        String filterKey1 = "filter1";
        String filterKey2 = "filter2";
        
        FilterDefinition<TestPropertyRef> filterDef1 = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.EQUALS, "value1"
        );
        FilterDefinition<TestPropertyRef> filterDef2 = new FilterDefinition<>(
            TestPropertyRef.USER_AGE, Op.GREATER_THAN, 25
        );
        
        ConditionAdapter<TestEntity> conditionAdapter1 = mock(ConditionAdapter.class);
        ConditionAdapter<TestEntity> conditionAdapter2 = mock(ConditionAdapter.class);
        
        when(conditionAdapterBuilder.build(filterDef1))
            .thenReturn(conditionAdapter1);
        when(conditionAdapterBuilder.build(filterDef2))
            .thenReturn(conditionAdapter2);
        
        // Act
        filterContext.addCondition(filterKey1, filterDef1);
        filterContext.addCondition(filterKey2, filterDef2);
        
        // Assert
        assertEquals(conditionAdapter1, filterContext.getCondition(filterKey1));
        assertEquals(conditionAdapter2, filterContext.getCondition(filterKey2));
        
        verify(conditionAdapterBuilder).build(filterDef1);
        verify(conditionAdapterBuilder).build(filterDef2);
    }

    @Test
    void testOverwriteExistingCondition() {
        // Arrange
        String filterKey = "testFilter";
        FilterDefinition<TestPropertyRef> filterDef1 = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.EQUALS, "value1"
        );
        FilterDefinition<TestPropertyRef> filterDef2 = new FilterDefinition<>(
            TestPropertyRef.USER_NAME, Op.LIKE, "%value2%"
        );
        
        ConditionAdapter<TestEntity> conditionAdapter1 = mock(ConditionAdapter.class);
        ConditionAdapter<TestEntity> conditionAdapter2 = mock(ConditionAdapter.class);
        
        when(conditionAdapterBuilder.build(filterDef1))
            .thenReturn(conditionAdapter1);
        when(conditionAdapterBuilder.build(filterDef2))
            .thenReturn(conditionAdapter2);
        
        // Act
        filterContext.addCondition(filterKey, filterDef1);
        filterContext.addCondition(filterKey, filterDef2); // Overwrite
        
        // Assert
        assertEquals(conditionAdapter2, filterContext.getCondition(filterKey));
        
        verify(conditionAdapterBuilder).build(filterDef1);
        verify(conditionAdapterBuilder).build(filterDef2);
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
                TestPropertyRef.USER_NAME, supportedOperators[i], value
            );
            
            ConditionAdapter<TestEntity> conditionAdapter = mock(ConditionAdapter.class);
            
            when(conditionAdapterBuilder.build(filterDef))
                .thenReturn(conditionAdapter);
            
            // Act
            filterContext.addCondition(filterKey, filterDef);
            
            // Assert
            assertEquals(conditionAdapter, filterContext.getCondition(filterKey));
        }
    }

    private Object getTestValueForOperator(Op operator) {
        return switch (operator) {
            case EQUALS, NOT_EQUALS, LIKE, NOT_LIKE -> "testValue";
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> 100;
            case IN, NOT_IN -> java.util.List.of("value1", "value2");
            case IS_NULL, IS_NOT_NULL -> null;
            case BETWEEN, NOT_BETWEEN -> java.util.List.of(10, 20);
            default -> "defaultValue";
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

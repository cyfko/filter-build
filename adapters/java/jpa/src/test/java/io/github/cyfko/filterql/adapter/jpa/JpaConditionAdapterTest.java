package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JpaConditionAdapter.
 */
@ExtendWith(MockitoExtension.class)
class JpaConditionAdapterTest {
    
    @Mock
    private CriteriaBuilder criteriaBuilder;
    
    @Mock
    private Predicate predicate1;
    
    @Mock
    private Predicate predicate2;
    
    @Mock
    private Predicate combinedPredicate;
    
    @Mock
    private Predicate negatedPredicate;
    
    private JpaConditionAdapter<TestTypes.TestEntity> conditionAdapter1;
    private JpaConditionAdapter<TestTypes.TestEntity> conditionAdapter2;
    
    @BeforeEach
    void setUp() {
        conditionAdapter1 = new JpaConditionAdapter<>(predicate1, criteriaBuilder);
        conditionAdapter2 = new JpaConditionAdapter<>(predicate2, criteriaBuilder);
    }
    
    @Test
    void testGetPredicate_ShouldReturnCorrectPredicate() {
        // Act
        Predicate result = conditionAdapter1.getPredicate();
        
        // Assert
        assertEquals(predicate1, result);
    }
    
    @Test
    void testAnd_WithJpaConditionAdapter_ShouldReturnCombinedCondition() {
        // Arrange
        when(criteriaBuilder.and(predicate1, predicate2)).thenReturn(combinedPredicate);
        
        // Act
        Condition result = conditionAdapter1.and(conditionAdapter2);
        
        // Assert
        assertNotNull(result);
        assertTrue(result instanceof JpaConditionAdapter);
        
        JpaConditionAdapter<TestTypes.TestEntity> resultJpa = (JpaConditionAdapter<TestTypes.TestEntity>) result;
        assertEquals(combinedPredicate, resultJpa.getPredicate());
        
        verify(criteriaBuilder).and(predicate1, predicate2);
    }
    
    @Test
    void testAnd_WithNonJpaCondition_ShouldThrowException() {
        // Arrange
        Condition nonJpaCondition = mock(Condition.class);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> conditionAdapter1.and(nonJpaCondition)
        );
        
        assertTrue(exception.getMessage().contains("Cannot combine with non-JPA condition"));
    }
    
    @Test
    void testOr_WithJpaConditionAdapter_ShouldReturnCombinedCondition() {
        // Arrange
        when(criteriaBuilder.or(predicate1, predicate2)).thenReturn(combinedPredicate);
        
        // Act
        Condition result = conditionAdapter1.or(conditionAdapter2);
        
        // Assert
        assertNotNull(result);
        assertTrue(result instanceof JpaConditionAdapter);
        
        JpaConditionAdapter<TestTypes.TestEntity> resultJpa = (JpaConditionAdapter<TestTypes.TestEntity>) result;
        assertEquals(combinedPredicate, resultJpa.getPredicate());
        
        verify(criteriaBuilder).or(predicate1, predicate2);
    }
    
    @Test
    void testOr_WithNonJpaCondition_ShouldThrowException() {
        // Arrange
        Condition nonJpaCondition = mock(Condition.class);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> conditionAdapter1.or(nonJpaCondition)
        );
        
        assertTrue(exception.getMessage().contains("Cannot combine with non-JPA condition"));
    }
    
    @Test
    void testNot_ShouldReturnNegatedCondition() {
        // Arrange
        when(criteriaBuilder.not(predicate1)).thenReturn(negatedPredicate);
        
        // Act
        Condition result = conditionAdapter1.not();
        
        // Assert
        assertNotNull(result);
        assertTrue(result instanceof JpaConditionAdapter);
        
        JpaConditionAdapter<TestTypes.TestEntity> resultJpa = (JpaConditionAdapter<TestTypes.TestEntity>) result;
        assertEquals(negatedPredicate, resultJpa.getPredicate());
        
        verify(criteriaBuilder).not(predicate1);
    }
    
    @Test
    void testChainedOperations_ShouldWorkCorrectly() {
        // Arrange
        when(criteriaBuilder.and(predicate1, predicate2)).thenReturn(combinedPredicate);
        when(criteriaBuilder.not(combinedPredicate)).thenReturn(negatedPredicate);
        
        // Act
        Condition result = conditionAdapter1.and(conditionAdapter2).not();
        
        // Assert
        assertNotNull(result);
        assertTrue(result instanceof JpaConditionAdapter);
        
        JpaConditionAdapter<TestTypes.TestEntity> resultJpa = (JpaConditionAdapter<TestTypes.TestEntity>) result;
        assertEquals(negatedPredicate, resultJpa.getPredicate());
        
        verify(criteriaBuilder).and(predicate1, predicate2);
        verify(criteriaBuilder).not(combinedPredicate);
    }
}

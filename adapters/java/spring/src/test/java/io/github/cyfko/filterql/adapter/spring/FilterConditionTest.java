package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires simples pour SpringConditionAdapter.
 */
class FilterConditionTest {

    @Test
    void testConstructor() {
        Specification<TestEntity> specification = mock(Specification.class);
        FilterCondition<TestEntity> adapter = new FilterCondition<>(specification);
        
        assertNotNull(adapter);
        assertEquals(specification, adapter.getSpecification());
    }

    @Test
    void testGetSpecification() {
        Specification<TestEntity> specification = mock(Specification.class);
        FilterCondition<TestEntity> adapter = new FilterCondition<>(specification);
        
        Specification<TestEntity> result = adapter.getSpecification();
        assertEquals(specification, result);
    }

    @Test
    void testAndOperation() {
        Specification<TestEntity> spec1 = mock(Specification.class);
        Specification<TestEntity> spec2 = mock(Specification.class);
        
        FilterCondition<TestEntity> adapter1 = new FilterCondition<>(spec1);
        FilterCondition<TestEntity> adapter2 = new FilterCondition<>(spec2);
        
        Condition result = adapter1.and(adapter2);
        
        assertNotNull(result);
        assertTrue(result instanceof FilterCondition);
    }

    @Test
    void testOrOperation() {
        Specification<TestEntity> spec1 = mock(Specification.class);
        Specification<TestEntity> spec2 = mock(Specification.class);
        
        FilterCondition<TestEntity> adapter1 = new FilterCondition<>(spec1);
        FilterCondition<TestEntity> adapter2 = new FilterCondition<>(spec2);
        
        Condition result = adapter1.or(adapter2);
        
        assertNotNull(result);
        assertTrue(result instanceof FilterCondition);
    }

    @Test
    void testNotOperation() {
        Specification<TestEntity> specification = mock(Specification.class);
        FilterCondition<TestEntity> adapter = new FilterCondition<>(specification);
        
        Condition result = adapter.not();
        
        assertNotNull(result);
        assertTrue(result instanceof FilterCondition);
    }

    @Test
    void testAndOperationWithNonSpringCondition() {
        Specification<TestEntity> specification = mock(Specification.class);
        FilterCondition<TestEntity> adapter = new FilterCondition<>(specification);
        Condition nonSpringCondition = mock(Condition.class);
        
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.and(nonSpringCondition);
        });
    }

    @Test
    void testOrOperationWithNonSpringCondition() {
        Specification<TestEntity> specification = mock(Specification.class);
        FilterCondition<TestEntity> adapter = new FilterCondition<>(specification);
        Condition nonSpringCondition = mock(Condition.class);
        
        assertThrows(IllegalArgumentException.class, () -> {
            adapter.or(nonSpringCondition);
        });
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
}
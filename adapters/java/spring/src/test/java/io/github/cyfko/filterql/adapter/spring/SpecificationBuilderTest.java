package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.utils.OperatorUtils;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires simples pour FilterResolver.
 */
class SpecificationBuilderTest {

    private FilterContext<TestEntity, TestPropertyRef> context;

    @BeforeEach
    void setUp() {
        context = new FilterContext<>(TestEntity.class, TestPropertyRef.class, p -> switch (p) {
            case USER_NAME -> "name";
            case USER_AGE -> "age";
        });
    }

    @Test
    void testToSpecificationSimpleExpression() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        TestPropertyRef propertyRef = TestPropertyRef.USER_NAME;
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            propertyRef, Op.EQUALS, "John"
        );
        
        Map<String, FilterDefinition<TestPropertyRef>> filters = Map.of("f1", filterDef);
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(filters, "f1");
        
        // Act
        PredicateResolver<TestEntity> result = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void testToSpecificationAndExpression() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        TestPropertyRef nameRef = TestPropertyRef.USER_NAME;
        TestPropertyRef ageRef = TestPropertyRef.USER_AGE;
        
        FilterDefinition<TestPropertyRef> nameFilter = new FilterDefinition<>(
            nameRef, Op.EQUALS, "John"
        );
        FilterDefinition<TestPropertyRef> ageFilter = new FilterDefinition<>(
            ageRef, Op.GREATER_THAN, 25
        );
        
        Map<String, FilterDefinition<TestPropertyRef>> filters = Map.of(
            "f1", nameFilter,
            "f2", ageFilter
        );
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(filters, "f1 & f2");
        
        // Act
        PredicateResolver<TestEntity> result = FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void testToSpecificationWithInvalidDSL() {
        // Arrange
        TestPropertyRef nameRef = TestPropertyRef.USER_NAME;
        
        FilterDefinition<TestPropertyRef> nameFilter = new FilterDefinition<>(
            nameRef, Op.EQUALS, "John"
        );
        
        Map<String, FilterDefinition<TestPropertyRef>> filters = Map.of("f1", nameFilter);
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(filters, "f1 &"); // Invalid DSL
        
        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            FilterResolver.of(context).resolve(TestEntity.class, filterRequest);
        });
    }

    /**
     * Classe d'entité de test simple.
     */
    static class TestEntity {
        private Long id;
        private String name;
        private Integer age;
        
        public TestEntity() {}
        
        public TestEntity(Long id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
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
package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.adapter.spring.mappings.PathMapping;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.utils.OperatorUtils;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires simples pour SpecificationBuilder.
 */
class SpecificationBuilderTest {

    @Test
    void testToSpecificationSimpleExpression() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        TestPropertyRef propertyRef = TestPropertyRef.USER_NAME;
        FilterDefinition<TestPropertyRef> filterDef = new FilterDefinition<>(
            propertyRef, Operator.EQUALS, "John"
        );
        
        Map<String, FilterDefinition<TestPropertyRef>> filters = Map.of("f1", filterDef);
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(filters, "f1");
        
        // Act
        Specification<TestEntity> result = SpecificationBuilder.toSpecification(filterRequest);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void testToSpecificationAndExpression() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        TestPropertyRef nameRef = TestPropertyRef.USER_NAME;
        TestPropertyRef ageRef = TestPropertyRef.USER_AGE;
        
        FilterDefinition<TestPropertyRef> nameFilter = new FilterDefinition<>(
            nameRef, Operator.EQUALS, "John"
        );
        FilterDefinition<TestPropertyRef> ageFilter = new FilterDefinition<>(
            ageRef, Operator.GREATER_THAN, 25
        );
        
        Map<String, FilterDefinition<TestPropertyRef>> filters = Map.of(
            "f1", nameFilter,
            "f2", ageFilter
        );
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(filters, "f1 & f2");
        
        // Act
        Specification<TestEntity> result = SpecificationBuilder.toSpecification(filterRequest);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    void testToSpecificationWithInvalidDSL() {
        // Arrange
        TestPropertyRef nameRef = TestPropertyRef.USER_NAME;
        
        FilterDefinition<TestPropertyRef> nameFilter = new FilterDefinition<>(
            nameRef, Operator.EQUALS, "John"
        );
        
        Map<String, FilterDefinition<TestPropertyRef>> filters = Map.of("f1", nameFilter);
        FilterRequest<TestPropertyRef> filterRequest = new FilterRequest<>(filters, "f1 &"); // Invalid DSL
        
        // Act & Assert
        assertThrows(DSLSyntaxException.class, () -> {
            SpecificationBuilder.toSpecification(filterRequest);
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
    enum TestPropertyRef implements PropertyRef, PathMapping {
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
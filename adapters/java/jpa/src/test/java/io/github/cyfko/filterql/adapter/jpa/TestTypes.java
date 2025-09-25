package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;

import java.util.Set;

/**
 * Test types for JPA adapter testing.
 */
public class TestTypes {
    
    /**
     * Test entity class.
     */
    public static class TestEntity {
        private String name;
        private Integer age;
        private String email;
        private String status;
        
        public TestEntity() {}
        
        public TestEntity(String name, Integer age, String email, String status) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.status = status;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * Test PropertyRef enum for testing.
     */
    public enum TestPropertyRef implements PropertyRef {
        USER_NAME(String.class, Set.of(Operator.EQUALS, Operator.LIKE, Operator.IN)),
        USER_AGE(Integer.class, Set.of(Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN)),
        USER_EMAIL(String.class, Set.of(Operator.EQUALS, Operator.LIKE)),
        USER_STATUS(String.class, Set.of(Operator.EQUALS, Operator.IN));
        
        private final Class<?> type;
        private final Set<Operator> supportedOperators;
        
        TestPropertyRef(Class<?> type, Set<Operator> supportedOperators) {
            this.type = type;
            this.supportedOperators = supportedOperators;
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
        public boolean supportsOperator(Operator operator) {
            return supportedOperators.contains(operator);
        }
        
        @Override
        public void validateOperator(Operator operator) {
            if (!supportsOperator(operator)) {
                throw new IllegalArgumentException(
                    "Operator '" + operator + "' is not supported for property " + this.name()
                );
            }
        }
    }
}

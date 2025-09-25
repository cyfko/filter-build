package io.github.cyfko.dynamicfilter.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class ContextTest {

    private TestContext context;

    @BeforeEach
    void setUp() {
        context = new TestContext();
    }

    @Test
    @DisplayName("Should get condition by key")
    void shouldGetConditionByKey() {
        // Given
        String key = "testCondition";
        TestCondition expectedCondition = new TestCondition("test");
        context.addCondition(key, expectedCondition);

        // When
        Condition result = context.getCondition(key);

        // Then
        assertEquals(expectedCondition, result);
    }

    @Test
    @DisplayName("Should return null for non-existent key")
    void shouldReturnNullForNonExistentKey() {
        // When
        Condition result = context.getCondition("nonExistent");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null key")
    void shouldHandleNullKey() {
        // When
        Condition result = context.getCondition(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty key")
    void shouldHandleEmptyKey() {
        // When
        Condition result = context.getCondition("");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should override existing condition")
    void shouldOverrideExistingCondition() {
        // Given
        String key = "testCondition";
        TestCondition condition1 = new TestCondition("first");
        TestCondition condition2 = new TestCondition("second");
        
        context.addCondition(key, condition1);
        context.addCondition(key, condition2);

        // When
        Condition result = context.getCondition(key);

        // Then
        assertEquals(condition2, result);
        assertNotEquals(condition1, result);
    }

    // Test implementation of Context interface
    private static class TestContext implements Context {
        private final Map<String, Condition> conditions = new HashMap<>();

        public void addCondition(String key, Condition condition) {
            conditions.put(key, condition);
        }

        @Override
        public Condition getCondition(String key) {
            return conditions.get(key);
        }
    }

    // Test implementation of Condition interface
    private static class TestCondition implements Condition {
        private final String name;

        public TestCondition(String name) {
            this.name = name;
        }

        @Override
        public Condition and(Condition other) {
            return new AndCondition(this, other);
        }

        @Override
        public Condition or(Condition other) {
            return new OrCondition(this, other);
        }

        @Override
        public Condition not() {
            return new NotCondition(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestCondition that = (TestCondition) obj;
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "TestCondition{" + name + "}";
        }

        static class AndCondition implements Condition {
            private final Condition left, right;

            AndCondition(Condition left, Condition right) {
                this.left = left;
                this.right = right;
            }

            @Override
            public Condition and(Condition other) {
                return new AndCondition(this, other);
            }

            @Override
            public Condition or(Condition other) {
                return new OrCondition(this, other);
            }

            @Override
            public Condition not() {
                return new NotCondition(this);
            }
        }

        static class OrCondition implements Condition {
            private final Condition left, right;

            OrCondition(Condition left, Condition right) {
                this.left = left;
                this.right = right;
            }

            @Override
            public Condition and(Condition other) {
                return new AndCondition(this, other);
            }

            @Override
            public Condition or(Condition other) {
                return new OrCondition(this, other);
            }

            @Override
            public Condition not() {
                return new NotCondition(this);
            }
        }

        static class NotCondition implements Condition {
            private final Condition condition;

            NotCondition(Condition condition) {
                this.condition = condition;
            }

            @Override
            public Condition and(Condition other) {
                return new AndCondition(this, other);
            }

            @Override
            public Condition or(Condition other) {
                return new OrCondition(this, other);
            }

            @Override
            public Condition not() {
                return new NotCondition(this);
            }
        }
    }
}


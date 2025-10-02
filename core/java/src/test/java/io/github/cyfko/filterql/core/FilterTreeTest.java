package io.github.cyfko.filterql.core;

import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class FilterTreeTest {

    private TestFilterTree filterTree;
    private TestContext context;

    @BeforeEach
    void setUp() {
        filterTree = new TestFilterTree();
        context = new TestContext();
        
        // Setup some test conditions in context
        context.addCondition("filter1", new TestCondition("condition1"));
        context.addCondition("filter2", new TestCondition("condition2"));
        context.addCondition("filter3", new TestCondition("condition3"));
    }

    @Test
    @DisplayName("Should evaluate simple filter tree")
    void shouldEvaluateSimpleFilterTree() {
        // Given
        filterTree.setExpression("filter1");

        // When
        Condition result = filterTree.generate(context);

        // Then
        assertNotNull(result);
        assertEquals("condition1", ((TestCondition) result).getName());
    }

    @Test
    @DisplayName("Should handle non-existent filter")
    void shouldHandleNonExistentFilter() {
        // Given
        filterTree.setExpression("nonExistent");

        // When
        Condition result = filterTree.generate(context);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null context")
    void shouldHandleNullContext() {
        // Given
        filterTree.setExpression("filter1");

        // When & Then
        assertThrows(NullPointerException.class, () -> filterTree.generate(null));
    }

    @Test
    @DisplayName("Should handle empty expression")
    void shouldHandleEmptyExpression() {
        // Given
        filterTree.setExpression("");

        // When
        Condition result = filterTree.generate(context);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null expression")
    void shouldHandleNullExpression() {
        // Given
        filterTree.setExpression(null);

        // When
        Condition result = filterTree.generate(context);

        // Then
        assertNull(result);
    }

    // Test implementation of FilterTree interface
    private static class TestFilterTree implements FilterTree {
        private String expression;

        public void setExpression(String expression) {
            this.expression = expression;
        }

        @Override
        public Condition generate(Context context) {
            if (context == null) {
                throw new NullPointerException("Context cannot be null");
            }
            
            if (expression == null || expression.trim().isEmpty()) {
                return null;
            }
            
            return context.getCondition(expression.trim());
        }
    }

    // Test implementation of Context interface
    private static class TestContext implements Context {
        private final java.util.Map<String, Condition> conditions = new java.util.HashMap<>();

        public void addCondition(String key, Condition condition) {
            conditions.put(key, condition);
        }

        @Override
        public Condition addCondition(String filterKey, FilterDefinition<?> definition) {
            return conditions.put(filterKey, null);
        }

        @Override
        public Condition getCondition(String key) {
            return conditions.get(key);
        }

        @Override
        public <E> PredicateResolver<E> toResolver(Class<E> entityClass, Condition condition) {
            return null;
        }
    }

    // Test implementation of Condition interface
    private static class TestCondition implements Condition {
        private final String name;

        public TestCondition(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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

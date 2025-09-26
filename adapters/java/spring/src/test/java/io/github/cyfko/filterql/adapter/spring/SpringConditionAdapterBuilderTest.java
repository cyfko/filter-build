package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SpringConditionAdapterBuilderIntegrationTest {

    @Autowired
    private EntityManager em;

    private SpringConditionAdapterBuilder<TestEntity, TestPropertyRef> builder;

    @BeforeEach
    void setUp() {
        builder = new SpringConditionAdapterBuilder<TestEntity, TestPropertyRef>() {};

        em.persist(new TestEntity("Alice", 25, "alice@test.com"));
        em.persist(new TestEntity("Bob", 30, "bob@test.com"));
        em.persist(new TestEntity("Charlie", 40, "charlie@test.com"));
        em.flush();
    }

    private List<TestEntity> runSpec(Specification<TestEntity> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TestEntity> query = cb.createQuery(TestEntity.class);
        Root<TestEntity> root = query.from(TestEntity.class);
        query.where(spec.toPredicate(root, query, cb));
        return em.createQuery(query).getResultList();
    }

    @Test
    void testEqualsCondition() {
        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.EQUALS, "Alice").getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getName());
    }

    @Test
    void testNotEqualsCondition() {
        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.NOT_EQUALS, "Alice").getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void testGreaterThanCondition() {
        var spec = builder.build(TestPropertyRef.USER_AGE, Operator.GREATER_THAN, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Charlie", results.get(0).getName());
    }

    @Test
    void testGreaterThanOrEqualCondition() {
        var spec = builder.build(TestPropertyRef.USER_AGE, Operator.GREATER_THAN_OR_EQUAL, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Bob")));
    }

    @Test
    void testLessThanCondition() {
        var spec = builder.build(TestPropertyRef.USER_AGE, Operator.LESS_THAN, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getName());
    }

    @Test
    void testLessThanOrEqualCondition() {
        var spec = builder.build(TestPropertyRef.USER_AGE, Operator.LESS_THAN_OR_EQUAL, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void testLikeCondition() {
        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.LIKE, "%li%").getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size()); // Alice + Charlie
    }

    @Test
    void testNotLikeCondition() {
        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.NOT_LIKE, "%li%").getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size()); // Bob
        assertEquals("Bob", results.get(0).getName());
    }

    @Test
    void testInCondition() {
        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.IN, List.of("Alice", "Charlie")).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
    }

    @Test
    void testNotInCondition() {
        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.NOT_IN, List.of("Alice", "Charlie")).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Bob", results.get(0).getName());
    }

    @Test
    void testIsNullCondition() {
        em.persist(new TestEntity(null, 50, null));
        em.flush();

        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.IS_NULL, null).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertNull(results.get(0).getName());
    }

    @Test
    void testIsNotNullCondition() {
        var spec = builder.build(TestPropertyRef.USER_NAME, Operator.IS_NOT_NULL, null).getSpecification();
        var results = runSpec(spec);

        assertEquals(3, results.size());
    }

    @Test
    void testBetweenCondition() {
        var spec = builder.build(TestPropertyRef.USER_AGE, Operator.BETWEEN, List.of(25, 35)).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size()); // Alice (25) + Bob (30)
    }

    @Test
    void testNotBetweenCondition() {
        var spec = builder.build(TestPropertyRef.USER_AGE, Operator.NOT_BETWEEN, List.of(25, 35)).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size()); // Charlie (40)
    }

    @Entity
    @Table(name = "test_entity")
    static class TestEntity {
        @Id @GeneratedValue
        private Long id;
        private String name;
        private Integer age;
        private String email;

        protected TestEntity() {}

        TestEntity(String name, Integer age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() { return name; }
        public Integer getAge() { return age; }
        public String getEmail() { return email; }
    }

    enum TestPropertyRef implements PropertyRef, PathShape {
        USER_NAME("name", String.class, Set.of(
                Operator.EQUALS, Operator.NOT_EQUALS,
                Operator.LIKE, Operator.NOT_LIKE,
                Operator.IN, Operator.NOT_IN,
                Operator.IS_NULL, Operator.IS_NOT_NULL
        )),
        USER_AGE("age", Integer.class, Set.of(
                Operator.EQUALS, Operator.NOT_EQUALS,
                Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
                Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL,
                Operator.IN, Operator.NOT_IN,
                Operator.IS_NULL, Operator.IS_NOT_NULL,
                Operator.BETWEEN, Operator.NOT_BETWEEN
        )),
        USER_EMAIL("email", String.class, Set.of(
                Operator.EQUALS, Operator.NOT_EQUALS,
                Operator.LIKE, Operator.NOT_LIKE,
                Operator.IN, Operator.NOT_IN,
                Operator.IS_NULL, Operator.IS_NOT_NULL
        ));

        private final String path;
        private final Class<?> type;
        private final Set<Operator> supportedOperators;

        TestPropertyRef(String path, Class<?> type, Set<Operator> supportedOperators) {
            this.path = path;
            this.type = type;
            this.supportedOperators = supportedOperators;
        }

        @Override public String getPath() { return path; }
        @Override public Class<?> getType() { return type; }
        @Override public Set<Operator> getSupportedOperators() { return supportedOperators; }
        @Override public void validateOperator(Operator operator) {
            if (!supportedOperators.contains(operator)) {
                throw new IllegalArgumentException("Operator " + operator + " not supported for " + this);
            }
        }
        @Override public void validateOperatorForValue(Operator operator, Object value) {
            validateOperator(operator);
        }
    }
}


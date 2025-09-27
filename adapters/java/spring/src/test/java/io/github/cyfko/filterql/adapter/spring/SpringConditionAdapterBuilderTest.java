package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.validation.Operator;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ConditionAdapterBuilderIntegrationTest {

    @Autowired
    private EntityManager em;

    private ConditionAdapterBuilder<User, UserPropertyRef> builder;

    @BeforeEach
    void setUp() {
        builder = new ConditionAdapterBuilder<User, UserPropertyRef>() {};

        em.persist(new User("Alice", 25, "alice@test.com"));
        em.persist(new User("Bob", 30, "bob@test.com"));
        em.persist(new User("Charlie", 40, "charlie@test.com"));
        em.flush();
    }

    private List<User> runSpec(Specification<User> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(spec.toPredicate(root, query, cb));
        return em.createQuery(query).getResultList();
    }

    @Test
    void testEqualsCondition() {
        var spec = builder.build(UserPropertyRef.NAME, Operator.EQUALS, "Alice").getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getName());
    }

    @Test
    void testNotEqualsCondition() {
        var spec = builder.build(UserPropertyRef.NAME, Operator.NOT_EQUALS, "Alice").getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void testGreaterThanCondition() {
        var spec = builder.build(UserPropertyRef.AGE, Operator.GREATER_THAN, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Charlie", results.get(0).getName());
    }

    @Test
    void testGreaterThanOrEqualCondition() {
        var spec = builder.build(UserPropertyRef.AGE, Operator.GREATER_THAN_OR_EQUAL, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Bob")));
    }

    @Test
    void testLessThanCondition() {
        var spec = builder.build(UserPropertyRef.AGE, Operator.LESS_THAN, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getName());
    }

    @Test
    void testLessThanOrEqualCondition() {
        var spec = builder.build(UserPropertyRef.AGE, Operator.LESS_THAN_OR_EQUAL, 30).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void testLikeCondition() {
        var spec = builder.build(UserPropertyRef.NAME, Operator.LIKE, "%li%").getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size()); // Alice + Charlie
    }

    @Test
    void testNotLikeCondition() {
        var spec = builder.build(UserPropertyRef.NAME, Operator.NOT_LIKE, "%li%").getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size()); // Bob
        assertEquals("Bob", results.get(0).getName());
    }

    @Test
    void testInCondition() {
        var spec = builder.build(UserPropertyRef.NAME, Operator.IN, List.of("Alice", "Charlie")).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size());
    }

    @Test
    void testNotInCondition() {
        var spec = builder.build(UserPropertyRef.NAME, Operator.NOT_IN, List.of("Alice", "Charlie")).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertEquals("Bob", results.get(0).getName());
    }

    @Test
    void testIsNullCondition() {
        em.persist(new User(null, 50, null));
        em.flush();

        var spec = builder.build(UserPropertyRef.NAME, Operator.IS_NULL, null).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size());
        assertNull(results.get(0).getName());
    }

    @Test
    void testIsNotNullCondition() {
        var spec = builder.build(UserPropertyRef.NAME, Operator.IS_NOT_NULL, null).getSpecification();
        var results = runSpec(spec);

        assertEquals(3, results.size());
    }

    @Test
    void testBetweenCondition() {
        var spec = builder.build(UserPropertyRef.AGE, Operator.BETWEEN, List.of(25, 35)).getSpecification();
        var results = runSpec(spec);

        assertEquals(2, results.size()); // Alice (25) + Bob (30)
    }

    @Test
    void testNotBetweenCondition() {
        var spec = builder.build(UserPropertyRef.AGE, Operator.NOT_BETWEEN, List.of(25, 35)).getSpecification();
        var results = runSpec(spec);

        assertEquals(1, results.size()); // Charlie (40)
    }
}


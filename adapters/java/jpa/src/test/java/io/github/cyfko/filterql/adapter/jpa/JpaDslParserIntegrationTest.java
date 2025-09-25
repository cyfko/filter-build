package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterTree;
import io.github.cyfko.filterql.core.Parser;
import io.github.cyfko.filterql.core.impl.DSLParser;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Operator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DSL parser with JPA adapter.
 * Tests parsing DSL expressions and executing them against the database.
 */
class JpaDslParserIntegrationTest {
    
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private JpaContextAdapter<UserEntity, UserPropertyRef> contextAdapter;
    private Parser parser;
    
    @BeforeEach
    void setUp() {
        // Create H2 in-memory database
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu");
        entityManager = entityManagerFactory.createEntityManager();
        
        // Create specification builder and context adapter
        UserSpecificationBuilder specificationBuilder = new UserSpecificationBuilder();
        contextAdapter = new JpaContextAdapter<>(UserEntity.class, entityManager, specificationBuilder);
        parser = new DSLParser();
        
        // Insert test data
        insertTestData();
    }
    
    @AfterEach
    void tearDown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
    
    private void insertTestData() {
        entityManager.getTransaction().begin();
        
        // Clear existing data
        entityManager.createQuery("DELETE FROM UserEntity").executeUpdate();
        
        // Insert test users with diverse data
        entityManager.persist(new UserEntity("John Doe", 25, "john@example.com", "ACTIVE"));
        entityManager.persist(new UserEntity("Jane Smith", 30, "jane@example.com", "ACTIVE"));
        entityManager.persist(new UserEntity("Bob Johnson", 22, "bob@example.com", "PENDING"));
        entityManager.persist(new UserEntity("Alice Brown", 35, "alice@example.com", "INACTIVE"));
        entityManager.persist(new UserEntity("Charlie Wilson", 28, "charlie@example.com", "ACTIVE"));
        entityManager.persist(new UserEntity("David Lee", 40, "david@example.com", "ACTIVE"));
        entityManager.persist(new UserEntity("Emma Davis", 27, "emma@example.com", "PENDING"));
        
        entityManager.getTransaction().commit();
    }
    
    /**
     * Utility method to execute a condition and return results.
     */
    private List<UserEntity> executeCondition(Condition condition) {
        CriteriaQuery<UserEntity> query = contextAdapter.getQuery();
        query.where(((JpaConditionAdapter<UserEntity>) condition).getPredicate());
        
        TypedQuery<UserEntity> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }
    
    @Test
    void testSimpleDslExpression_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: status = 'ACTIVE'
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        );
        
        contextAdapter.addCondition("status", statusFilter);
        
        // Parse DSL expression: "status"
        FilterTree filterTree = parser.parse("status");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(user -> "ACTIVE".equals(user.getStatus())));
    }
    
    @Test
    void testAndDslExpression_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: status = 'ACTIVE' AND age > 25
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        );
        
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        );
        
        contextAdapter.addCondition("status", statusFilter);
        contextAdapter.addCondition("age", ageFilter);
        
        // Parse DSL expression: "status & age"
        FilterTree filterTree = parser.parse("status & age");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(user -> 
            "ACTIVE".equals(user.getStatus()) && user.getAge() > 25));
    }
    
    @Test
    void testOrDslExpression_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: name LIKE 'J%' OR age > 30
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "J%"
        );
        
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            30
        );
        
        contextAdapter.addCondition("name", nameFilter);
        contextAdapter.addCondition("age", ageFilter);
        
        // Parse DSL expression: "name | age"
        FilterTree filterTree = parser.parse("name | age");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("J")));
        assertTrue(results.stream().anyMatch(user -> user.getAge() > 30));
    }
    
    @Test
    void testNotDslExpression_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: NOT status = 'INACTIVE'
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "INACTIVE"
        );
        
        contextAdapter.addCondition("status", statusFilter);
        
        // Parse DSL expression: "!status"
        FilterTree filterTree = parser.parse("!status");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(6, results.size());
        assertTrue(results.stream().noneMatch(user -> "INACTIVE".equals(user.getStatus())));
    }
    
    @Test
    void testComplexDslExpression_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: (status = 'ACTIVE' AND age > 25) OR name LIKE 'B%'
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        );
        
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        );
        
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "B%"
        );
        
        contextAdapter.addCondition("status", statusFilter);
        contextAdapter.addCondition("age", ageFilter);
        contextAdapter.addCondition("name", nameFilter);
        
        // Parse DSL expression: "(status & age) | name"
        FilterTree filterTree = parser.parse("(status & age) | name");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(4, results.size());
        
        // Verify users with ACTIVE status AND age > 25
        assertTrue(results.stream().anyMatch(user -> 
            "ACTIVE".equals(user.getStatus()) && user.getAge() > 25));
        
        // Verify users with name starting with 'B'
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("B")));
    }
    
    @Test
    void testNestedNotDslExpression_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: NOT (status = 'ACTIVE' OR age < 25)
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        );
        
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.LESS_THAN,
            25
        );
        
        contextAdapter.addCondition("status", statusFilter);
        contextAdapter.addCondition("age", ageFilter);
        
        // Parse DSL expression: "!(status | age)"
        FilterTree filterTree = parser.parse("!(status | age)");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(user -> 
            !"ACTIVE".equals(user.getStatus()) && user.getAge() >= 25));
    }
    
    @Test
    void testDslExpressionWithParentheses_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: status = 'ACTIVE' AND (age > 25 OR name LIKE 'J%')
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        );
        
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        );
        
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "J%"
        );
        
        contextAdapter.addCondition("status", statusFilter);
        contextAdapter.addCondition("age", ageFilter);
        contextAdapter.addCondition("name", nameFilter);
        
        // Parse DSL expression: "status & (age | name)"
        FilterTree filterTree = parser.parse("status & (age | name)");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(user -> "ACTIVE".equals(user.getStatus())));
        assertTrue(results.stream().anyMatch(user -> user.getAge() > 25));
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("J")));
    }
    
    @Test
    void testDslExpressionWithMultipleParentheses_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: (status = 'ACTIVE' OR status = 'PENDING') AND (age > 25 OR name LIKE 'J%')
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.IN,
            new String[]{"ACTIVE", "PENDING"}
        );
        
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        );
        
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "J%"
        );
        
        contextAdapter.addCondition("status", statusFilter);
        contextAdapter.addCondition("age", ageFilter);
        contextAdapter.addCondition("name", nameFilter);
        
        // Parse DSL expression: "status & (age | name)"
        FilterTree filterTree = parser.parse("status & (age | name)");
        Condition condition = filterTree.generate(contextAdapter);
        
        // Act
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(user -> 
            "ACTIVE".equals(user.getStatus()) || "PENDING".equals(user.getStatus())));
        assertTrue(results.stream().anyMatch(user -> user.getAge() > 25));
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("J")));
    }
}

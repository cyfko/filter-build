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
 * Integration tests for FilterRequest with JPA adapter.
 * Tests complete filter request processing from DSL to database execution.
 */
class JpaFilterRequestIntegrationTest {
    
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
    
    /**
     * Utility method to process a FilterRequest and return results.
     */
    private List<UserEntity> processFilterRequest(FilterRequest<UserPropertyRef> filterRequest) throws Exception {
        // Add all filter definitions to context
        for (Map.Entry<String, FilterDefinition<UserPropertyRef>> entry : filterRequest.getFilters().entrySet()) {
            contextAdapter.addCondition(entry.getKey(), entry.getValue());
        }
        
        // Parse DSL expression
        FilterTree filterTree = parser.parse(filterRequest.getCombineWith());
        
        // Generate condition and execute
        Condition condition = filterTree.generate(contextAdapter);
        return executeCondition(condition);
    }
    
    @Test
    void testSimpleFilterRequest_ShouldReturnMatchingUsers() throws Exception {
        // Arrange
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "status"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(user -> "ACTIVE".equals(user.getStatus())));
    }
    
    @Test
    void testAndFilterRequest_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: status = 'ACTIVE' AND age > 25
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        ));
        filters.put("age", new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "status & age"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(user -> 
            "ACTIVE".equals(user.getStatus()) && user.getAge() > 25));
    }
    
    @Test
    void testOrFilterRequest_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: name LIKE 'J%' OR age > 30
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("name", new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "J%"
        ));
        filters.put("age", new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            30
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "name | age"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("J")));
        assertTrue(results.stream().anyMatch(user -> user.getAge() > 30));
    }
    
    @Test
    void testNotFilterRequest_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: NOT status = 'INACTIVE'
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "INACTIVE"
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "!status"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(6, results.size());
        assertTrue(results.stream().noneMatch(user -> "INACTIVE".equals(user.getStatus())));
    }
    
    @Test
    void testComplexFilterRequest_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: (status = 'ACTIVE' AND age > 25) OR name LIKE 'B%'
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        ));
        filters.put("age", new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        ));
        filters.put("name", new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "B%"
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "(status & age) | name"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(4, results.size());
        
        // Verify users with ACTIVE status AND age > 25
        assertTrue(results.stream().anyMatch(user -> 
            "ACTIVE".equals(user.getStatus()) && user.getAge() > 25));
        
        // Verify users with name starting with 'B'
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("B")));
    }
    
    @Test
    void testFilterRequestWithInOperator_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: status IN ('ACTIVE', 'PENDING')
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.IN,
            new String[]{"ACTIVE", "PENDING"}
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "status"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(6, results.size());
        assertTrue(results.stream().allMatch(user -> 
            "ACTIVE".equals(user.getStatus()) || "PENDING".equals(user.getStatus())));
    }
    
    @Test
    void testFilterRequestWithLikeOperator_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: name LIKE 'J%' AND email LIKE '%example.com'
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("name", new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "J%"
        ));
        filters.put("email", new FilterDefinition<>(
            UserPropertyRef.USER_EMAIL,
            Operator.LIKE,
            "%example.com"
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "name & email"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(user -> 
            user.getName().startsWith("J") && user.getEmail().endsWith("example.com")));
    }
    
    @Test
    void testFilterRequestWithNullChecks_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: email IS NOT NULL AND status IS NOT NULL
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("email", new FilterDefinition<>(
            UserPropertyRef.USER_EMAIL,
            Operator.IS_NOT_NULL,
            null
        ));
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.IS_NOT_NULL,
            null
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "email & status"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(7, results.size());
        assertTrue(results.stream().allMatch(user -> 
            user.getEmail() != null && user.getStatus() != null));
    }
    
    @Test
    void testFilterRequestWithNestedExpressions_ShouldReturnMatchingUsers() throws Exception {
        // Arrange: (status = 'ACTIVE' OR status = 'PENDING') AND (age > 25 OR name LIKE 'J%')
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.IN,
            new String[]{"ACTIVE", "PENDING"}
        ));
        filters.put("age", new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        ));
        filters.put("name", new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "J%"
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "status & (age | name)"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(user -> 
            "ACTIVE".equals(user.getStatus()) || "PENDING".equals(user.getStatus())));
        assertTrue(results.stream().anyMatch(user -> user.getAge() > 25));
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("J")));
    }
    
    @Test
    void testFilterRequestWithEmptyResult_ShouldReturnEmptyList() throws Exception {
        // Arrange: status = 'NONEXISTENT'
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "NONEXISTENT"
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "status"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testFilterRequestWithAllUsers_ShouldReturnAllUsers() throws Exception {
        // Arrange: status IS NOT NULL (should match all users)
        Map<String, FilterDefinition<UserPropertyRef>> filters = new HashMap<>();
        filters.put("status", new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.IS_NOT_NULL,
            null
        ));
        
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            filters,
            "status"
        );
        
        // Act
        List<UserEntity> results = processFilterRequest(filterRequest);
        
        // Assert
        assertEquals(7, results.size());
        assertTrue(results.stream().allMatch(user -> user.getStatus() != null));
    }
}

package io.github.cyfko.filterql.adapter.jpa;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Operator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for condition combinations (AND, OR, NOT) using H2 in-memory database.
 */
class JpaCombinationIntegrationTest {
    
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private JpaContextAdapter<UserEntity, UserPropertyRef> contextAdapter;
    
    @BeforeEach
    void setUp() {
        // Create H2 in-memory database
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu");
        entityManager = entityManagerFactory.createEntityManager();
        
        // Create specification builder and context adapter
        UserSpecificationBuilder specificationBuilder = new UserSpecificationBuilder();
        contextAdapter = new JpaContextAdapter<>(UserEntity.class, entityManager, specificationBuilder);
        
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
    void testAndCombination_ShouldReturnMatchingUsers() {
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
        
        // Act
        contextAdapter.addCondition("statusFilter", statusFilter);
        contextAdapter.addCondition("ageFilter", ageFilter);
        
        Condition statusCondition = contextAdapter.getCondition("statusFilter");
        Condition ageCondition = contextAdapter.getCondition("ageFilter");
        Condition combinedCondition = statusCondition.and(ageCondition);
        
        List<UserEntity> results = executeCondition(combinedCondition);
        
        // Assert: Should return users with ACTIVE status AND age > 25
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(user -> 
            "ACTIVE".equals(user.getStatus()) && user.getAge() > 25));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "Jane Smith".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Charlie Wilson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "David Lee".equals(user.getName())));
    }
    
    @Test
    void testOrCombination_ShouldReturnMatchingUsers() {
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
        
        // Act
        contextAdapter.addCondition("nameFilter", nameFilter);
        contextAdapter.addCondition("ageFilter", ageFilter);
        
        Condition nameCondition = contextAdapter.getCondition("nameFilter");
        Condition ageCondition = contextAdapter.getCondition("ageFilter");
        Condition combinedCondition = nameCondition.or(ageCondition);
        
        List<UserEntity> results = executeCondition(combinedCondition);
        
        // Assert: Should return users with name starting with 'J' OR age > 30
        assertEquals(4, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("J")));
        assertTrue(results.stream().anyMatch(user -> user.getAge() > 30));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "John Doe".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Jane Smith".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Alice Brown".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "David Lee".equals(user.getName())));
    }
    
    @Test
    void testNotCombination_ShouldReturnMatchingUsers() {
        // Arrange: NOT status = 'INACTIVE'
        FilterDefinition<UserPropertyRef> statusFilter = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "INACTIVE"
        );
        
        // Act
        contextAdapter.addCondition("statusFilter", statusFilter);
        Condition statusCondition = contextAdapter.getCondition("statusFilter");
        Condition negatedCondition = statusCondition.not();
        
        List<UserEntity> results = executeCondition(negatedCondition);
        
        // Assert: Should return all users except those with INACTIVE status
        assertEquals(6, results.size());
        assertTrue(results.stream().noneMatch(user -> "INACTIVE".equals(user.getStatus())));
        
        // Verify specific users are included
        assertTrue(results.stream().anyMatch(user -> "John Doe".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Jane Smith".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Bob Johnson".equals(user.getName())));
    }
    
    @Test
    void testComplexCombination_ShouldReturnMatchingUsers() {
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
        
        // Act
        contextAdapter.addCondition("statusFilter", statusFilter);
        contextAdapter.addCondition("ageFilter", ageFilter);
        contextAdapter.addCondition("nameFilter", nameFilter);
        
        Condition statusCondition = contextAdapter.getCondition("statusFilter");
        Condition ageCondition = contextAdapter.getCondition("ageFilter");
        Condition nameCondition = contextAdapter.getCondition("nameFilter");
        
        Condition complexCondition = statusCondition.and(ageCondition).or(nameCondition);
        
        List<UserEntity> results = executeCondition(complexCondition);
        
        // Assert: Should return users with (ACTIVE status AND age > 25) OR name starting with 'B'
        assertEquals(4, results.size());
        
        // Verify users with ACTIVE status AND age > 25
        assertTrue(results.stream().anyMatch(user -> 
            "ACTIVE".equals(user.getStatus()) && user.getAge() > 25));
        
        // Verify users with name starting with 'B'
        assertTrue(results.stream().anyMatch(user -> user.getName().startsWith("B")));
    }
    
    @Test
    void testTripleAndCombination_ShouldReturnMatchingUsers() {
        // Arrange: status = 'ACTIVE' AND age > 25 AND name LIKE 'J%'
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
        
        // Act
        contextAdapter.addCondition("statusFilter", statusFilter);
        contextAdapter.addCondition("ageFilter", ageFilter);
        contextAdapter.addCondition("nameFilter", nameFilter);
        
        Condition statusCondition = contextAdapter.getCondition("statusFilter");
        Condition ageCondition = contextAdapter.getCondition("ageFilter");
        Condition nameCondition = contextAdapter.getCondition("nameFilter");
        
        Condition tripleCondition = statusCondition.and(ageCondition).and(nameCondition);
        
        List<UserEntity> results = executeCondition(tripleCondition);
        
        // Assert: Should return users with ACTIVE status AND age > 25 AND name starting with 'J'
        assertEquals(1, results.size());
        UserEntity user = results.get(0);
        assertEquals("Jane Smith", user.getName());
        assertEquals("ACTIVE", user.getStatus());
        assertTrue(user.getAge() > 25);
    }
    
    @Test
    void testNestedNotCombination_ShouldReturnMatchingUsers() {
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
        
        // Act
        contextAdapter.addCondition("statusFilter", statusFilter);
        contextAdapter.addCondition("ageFilter", ageFilter);
        
        Condition statusCondition = contextAdapter.getCondition("statusFilter");
        Condition ageCondition = contextAdapter.getCondition("ageFilter");
        Condition orCondition = statusCondition.or(ageCondition);
        Condition notCondition = orCondition.not();
        
        List<UserEntity> results = executeCondition(notCondition);
        
        // Assert: Should return users that are NOT (ACTIVE OR age < 25)
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(user -> 
            !"ACTIVE".equals(user.getStatus()) && user.getAge() >= 25));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "Alice Brown".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Emma Davis".equals(user.getName())));
    }
}

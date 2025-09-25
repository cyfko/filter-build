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
 * Simple integration tests using H2 in-memory database.
 * Tests individual conditions without complex combinations.
 */
class SimpleJpaIntegrationTest {
    
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
        
        // Insert test users
        entityManager.persist(new UserEntity("John Doe", 25, "john@example.com", "ACTIVE"));
        entityManager.persist(new UserEntity("Jane Smith", 30, "jane@example.com", "ACTIVE"));
        entityManager.persist(new UserEntity("Bob Johnson", 22, "bob@example.com", "PENDING"));
        entityManager.persist(new UserEntity("Alice Brown", 35, "alice@example.com", "INACTIVE"));
        entityManager.persist(new UserEntity("Charlie Wilson", 28, "charlie@example.com", "ACTIVE"));
        
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
    void testSingleCondition_Equals_ShouldReturnMatchingUsers() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "ACTIVE"
        );
        
        // Act
        contextAdapter.addCondition("statusFilter", filterDef);
        Condition condition = contextAdapter.getCondition("statusFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(user -> "ACTIVE".equals(user.getStatus())));
    }
    
    @Test
    void testSingleCondition_Like_ShouldReturnMatchingUsers() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.LIKE,
            "J%"
        );
        
        // Act
        contextAdapter.addCondition("nameFilter", filterDef);
        Condition condition = contextAdapter.getCondition("nameFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(user -> user.getName().startsWith("J")));
    }
    
    @Test
    void testSingleCondition_GreaterThan_ShouldReturnMatchingUsers() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        );
        
        // Act
        contextAdapter.addCondition("ageFilter", filterDef);
        Condition condition = contextAdapter.getCondition("ageFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(user -> user.getAge() > 25));
    }
    
    @Test
    void testSingleCondition_In_ShouldReturnMatchingUsers() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.IN,
            new String[]{"ACTIVE", "PENDING"}
        );
        
        // Act
        contextAdapter.addCondition("statusFilter", filterDef);
        Condition condition = contextAdapter.getCondition("statusFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(user -> 
            "ACTIVE".equals(user.getStatus()) || "PENDING".equals(user.getStatus())));
    }
    
    @Test
    void testSingleCondition_IsNull_ShouldReturnMatchingUsers() {
        // Arrange
        entityManager.getTransaction().begin();
        UserEntity userWithNullEmail = new UserEntity("Test User", 25, null, "ACTIVE");
        entityManager.persist(userWithNullEmail);
        entityManager.getTransaction().commit();
        
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_EMAIL,
            Operator.IS_NULL,
            null
        );
        
        // Act
        contextAdapter.addCondition("emailFilter", filterDef);
        Condition condition = contextAdapter.getCondition("emailFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(1, results.size());
        assertNull(results.get(0).getEmail());
    }
    
    @Test
    void testSingleCondition_IsNotNull_ShouldReturnMatchingUsers() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_EMAIL,
            Operator.IS_NOT_NULL,
            null
        );
        
        // Act
        contextAdapter.addCondition("emailFilter", filterDef);
        Condition condition = contextAdapter.getCondition("emailFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(user -> user.getEmail() != null));
    }
    
    @Test
    void testEmptyResult_ShouldReturnEmptyList() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.EQUALS,
            "NONEXISTENT"
        );
        
        // Act
        contextAdapter.addCondition("statusFilter", filterDef);
        Condition condition = contextAdapter.getCondition("statusFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testAllUsers_ShouldReturnAllUsers() {
        // Arrange - Create a condition that always returns true
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.IS_NOT_NULL,
            null
        );
        
        // Act
        contextAdapter.addCondition("statusFilter", filterDef);
        Condition condition = contextAdapter.getCondition("statusFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(5, results.size());
    }
}

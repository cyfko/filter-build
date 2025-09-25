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
 * Integration tests for all operators using H2 in-memory database.
 */
class JpaOperatorIntegrationTest {
    
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
    void testNotEquals_ShouldReturnMatchingUsers() {
        // Arrange: status != 'ACTIVE'
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.NOT_EQUALS,
            "ACTIVE"
        );
        
        // Act
        contextAdapter.addCondition("statusFilter", filterDef);
        Condition condition = contextAdapter.getCondition("statusFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(3, results.size());
        assertTrue(results.stream().noneMatch(user -> "ACTIVE".equals(user.getStatus())));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "Bob Johnson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Alice Brown".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Emma Davis".equals(user.getName())));
    }
    
    @Test
    void testLessThan_ShouldReturnMatchingUsers() {
        // Arrange: age < 30
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.LESS_THAN,
            30
        );
        
        // Act
        contextAdapter.addCondition("ageFilter", filterDef);
        Condition condition = contextAdapter.getCondition("ageFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(user -> user.getAge() < 30));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "John Doe".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Bob Johnson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Charlie Wilson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Emma Davis".equals(user.getName())));
    }
    
    @Test
    void testLessThanOrEqual_ShouldReturnMatchingUsers() {
        // Arrange: age <= 28
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.LESS_THAN_OR_EQUAL,
            28
        );
        
        // Act
        contextAdapter.addCondition("ageFilter", filterDef);
        Condition condition = contextAdapter.getCondition("ageFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(user -> user.getAge() <= 28));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "John Doe".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Bob Johnson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Charlie Wilson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Emma Davis".equals(user.getName())));
    }
    
    @Test
    void testGreaterThanOrEqual_ShouldReturnMatchingUsers() {
        // Arrange: age >= 30
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN_OR_EQUAL,
            30
        );
        
        // Act
        contextAdapter.addCondition("ageFilter", filterDef);
        Condition condition = contextAdapter.getCondition("ageFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(user -> user.getAge() >= 30));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "Jane Smith".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Alice Brown".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "David Lee".equals(user.getName())));
    }
    
    @Test
    void testNotLike_ShouldReturnMatchingUsers() {
        // Arrange: name NOT LIKE 'J%'
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_NAME,
            Operator.NOT_LIKE,
            "J%"
        );
        
        // Act
        contextAdapter.addCondition("nameFilter", filterDef);
        Condition condition = contextAdapter.getCondition("nameFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(5, results.size());
        assertTrue(results.stream().noneMatch(user -> user.getName().startsWith("J")));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "Bob Johnson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Alice Brown".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Charlie Wilson".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "David Lee".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Emma Davis".equals(user.getName())));
    }
    
    @Test
    void testNotIn_ShouldReturnMatchingUsers() {
        // Arrange: status NOT IN ('ACTIVE', 'PENDING')
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_STATUS,
            Operator.NOT_IN,
            new String[]{"ACTIVE", "PENDING"}
        );
        
        // Act
        contextAdapter.addCondition("statusFilter", filterDef);
        Condition condition = contextAdapter.getCondition("statusFilter");
        List<UserEntity> results = executeCondition(condition);
        
        // Assert
        assertEquals(1, results.size());
        assertTrue(results.stream().noneMatch(user -> 
            "ACTIVE".equals(user.getStatus()) || "PENDING".equals(user.getStatus())));
        
        // Verify specific user
        assertEquals("Alice Brown", results.get(0).getName());
        assertEquals("INACTIVE", results.get(0).getStatus());
    }
    
    @Test
    void testBetween_ShouldReturnMatchingUsers() {
        // Arrange: age BETWEEN 25 AND 30 (using >= and <=)
        FilterDefinition<UserPropertyRef> lowerFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN_OR_EQUAL,
            25
        );
        
        FilterDefinition<UserPropertyRef> upperFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.LESS_THAN_OR_EQUAL,
            30
        );
        
        // Act
        contextAdapter.addCondition("lowerFilter", lowerFilter);
        contextAdapter.addCondition("upperFilter", upperFilter);
        
        Condition lowerCondition = contextAdapter.getCondition("lowerFilter");
        Condition upperCondition = contextAdapter.getCondition("upperFilter");
        Condition betweenCondition = lowerCondition.and(upperCondition);
        
        List<UserEntity> results = executeCondition(betweenCondition);
        
        // Assert
        assertEquals(4, results.size());
        assertTrue(results.stream().allMatch(user -> user.getAge() >= 25 && user.getAge() <= 30));
        
        // Verify specific users
        assertTrue(results.stream().anyMatch(user -> "John Doe".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Jane Smith".equals(user.getName())));
        assertTrue(results.stream().anyMatch(user -> "Charlie Wilson".equals(user.getName())));
    }
    
    @Test
    void testMultipleOperatorsOnSameField_ShouldWorkCorrectly() {
        // Test different operators on the same field (age)
        
        // Test age > 25
        FilterDefinition<UserPropertyRef> greaterFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.GREATER_THAN,
            25
        );
        
        contextAdapter.addCondition("greaterFilter", greaterFilter);
        Condition greaterCondition = contextAdapter.getCondition("greaterFilter");
        List<UserEntity> greaterResults = executeCondition(greaterCondition);
        assertEquals(5, greaterResults.size());
        
        // Test age <= 25
        FilterDefinition<UserPropertyRef> lessEqualFilter = new FilterDefinition<>(
            UserPropertyRef.USER_AGE,
            Operator.LESS_THAN_OR_EQUAL,
            25
        );
        
        contextAdapter.addCondition("lessEqualFilter", lessEqualFilter);
        Condition lessEqualCondition = contextAdapter.getCondition("lessEqualFilter");
        List<UserEntity> lessEqualResults = executeCondition(lessEqualCondition);
        assertEquals(2, lessEqualResults.size());
        
        // Verify they are complementary (total should be 7)
        assertEquals(7, greaterResults.size() + lessEqualResults.size());
    }
}

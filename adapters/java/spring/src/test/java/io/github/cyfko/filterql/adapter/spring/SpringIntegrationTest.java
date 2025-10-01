package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.validation.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour l'adaptateur Spring complet.
 * Teste l'intégration avec Spring Data JPA et une base de données en mémoire.
 */
@DataJpaTest
class SpringIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private FilterContext<User, UserPropertyRef> filterContext;

    @BeforeEach
    void setUp() {
        filterContext = new FilterContext<>(User.class, UserPropertyRef.class, def -> switch (def.ref()) {
            case NAME -> "name";
            case AGE -> "age";
            case EMAIL -> "email";
            case ACTIVE -> "active";
            case CREATED_AT -> "createdAt";
        });
        
        // Créer des données de test
        createTestUsers();
    }

    @Test
    void testSimpleEqualsFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.EQ, "John Doe"
        );
        
        // Act
        Condition condition = filterContext.addCondition("nameFilter", filterDef);
        FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getName());
    }

    @Test
    void testLikeFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.MATCHES, "%John%"
        );
        
        // Act
        Condition condition = filterContext.addCondition("nameFilter", filterDef);
        FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> "John Doe".equals(u.getName())));
        assertTrue(results.stream().anyMatch(u -> "Alice Johnson".equals(u.getName())));
    }

    @Test
    void testGreaterThanFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.AGE, Op.GT, 25
        );
        
        // Act
        Condition condition = filterContext.addCondition("ageFilter", filterDef);
        FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(u -> u.getAge() > 25));
    }

    @Test
    void testInFilter() {
        // Arrange
        List<String> names = Arrays.asList("John Doe", "Bob Smith");
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.IN, names
        );
        
        // Act
        Condition condition = filterContext.addCondition("nameFilter", filterDef);
        FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> "John Doe".equals(u.getName())));
        assertTrue(results.stream().anyMatch(u -> "Bob Smith".equals(u.getName())));
    }

    @Test
    void testBetweenFilter() {
        // Arrange
        List<Integer> ageRange = Arrays.asList(25, 35);
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.AGE, Op.RANGE, ageRange
        );
        
        // Act
        Condition condition = filterContext.addCondition("ageFilter", filterDef);
        FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
        }

    @Test
    void testBooleanFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.ACTIVE, Op.EQ, true
        );
        
        // Act
        Condition condition = filterContext.addCondition("activeFilter", filterDef);
        FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(u -> Boolean.TRUE.equals(u.getActive())));
    }

    @Test
    void testCombinedAndFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.MATCHES, "%o%"
        );
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.AGE, Op.GT, 25
        );
        
        // Act
        Condition nameCondition = filterContext.addCondition("nameFilter", nameFilter);
        Condition ageCondition = filterContext.addCondition("ageFilter", ageFilter);
        Condition combined = nameCondition.and(ageCondition);
        FilterCondition<User> filterCondition = (FilterCondition<User>) combined;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(1, results.size());
        assertEquals("Bob Smith", results.get(0).getName());
    }

    @Test
    void testCombinedOrFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.EQ, "John Doe"
        );
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.AGE, Op.EQ, 22
        );
        
        // Act
        Condition nameCondition = filterContext.addCondition("nameFilter", nameFilter);
        Condition ageCondition = filterContext.addCondition("ageFilter", ageFilter);
        Condition combined = nameCondition.or(ageCondition);
        FilterCondition<User> filterCondition = (FilterCondition<User>) combined;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> "John Doe".equals(u.getName())));
        assertTrue(results.stream().anyMatch(u -> "Alice Johnson".equals(u.getName())));
    }

    @Test
    void testInvalidPropertyReference() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Op.EQ, "test"
        );

        // Act & Assert - Test invalid enum compatibility
        assertDoesNotThrow(() -> {
            filterContext.addCondition("testFilter", filterDef);
        });
    }

    @Test
    void testNonExistentFilterKey() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            filterContext.getCondition("nonExistent");
        });
    }

    @Test
    void testIsNullFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.EMAIL, Op.IS_NULL, null
        );
        
        // Act
        Condition condition = filterContext.addCondition("emailFilter", filterDef);
        FilterCondition<User> filterCondition = (FilterCondition<User>) condition;
        List<User> results = userRepository.findAll(filterCondition.getSpecification());

        // Assert
        assertEquals(1, results.size());
        assertNull(results.get(0).getEmail());
    }

    private void createTestUsers() {
        User user1 = new User("John Doe", 25, "john@example.com", LocalDateTime.now());
        user1.setActive(true);
        User user2 = new User("Jane Smith", 30, "jane@example.com", LocalDateTime.now());
        user2.setActive(true);
        User user3 = new User("Bob Smith", 35, null, LocalDateTime.now());
        user3.setActive(true);
        User user4 = new User("Alice Johnson", 22, "alice@example.com", LocalDateTime.now());
        user4.setActive(false);

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        entityManager.persistAndFlush(user4);
    }
}


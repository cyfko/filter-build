package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Operator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    private ContextAdapter<User, UserPropertyRef> contextAdapter;
    private ConditionAdapterBuilder<User, UserPropertyRef> conditionBuilder;

    @BeforeEach
    void setUp() {
        conditionBuilder = new ConditionAdapterBuilder<>() {};
        contextAdapter = new ContextAdapter<>(conditionBuilder);
        
        // Créer des données de test
        createTestUsers();
    }

    @Test
    void testSimpleEqualsFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Operator.EQUALS, "John Doe"
        );
        contextAdapter.addCondition("nameFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("nameFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getName());
    }

    @Test
    void testLikeFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Operator.LIKE, "%John%"
        );
        contextAdapter.addCondition("nameFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("nameFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(2, results.size());
        assertEquals("John Doe", results.get(0).getName());
        assertEquals("Alice Johnson", results.get(1).getName());
    }

    @Test
    void testGreaterThanFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.AGE, Operator.GREATER_THAN, 25
        );
        contextAdapter.addCondition("ageFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("ageFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(2, results.size()); // Jane (30) et Bob (35)
        assertTrue(results.stream().allMatch(user -> user.getAge() > 25));
    }

    @Test
    void testInFilter() {
        // Arrange
        List<String> names = Arrays.asList("John Doe", "Bob Smith");
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Operator.IN, names
        );
        contextAdapter.addCondition("nameFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("nameFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(user -> user.getName().equals("John Doe")));
        assertTrue(results.stream().anyMatch(user -> user.getName().equals("Bob Smith")));
    }

    @Test
    void testBetweenFilter() {
        // Arrange
        List<Integer> ageRange = Arrays.asList(25, 35);
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.AGE, Operator.BETWEEN, ageRange
        );
        contextAdapter.addCondition("ageFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("ageFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(3, results.size()); // John (25), Jane (30) et Bob (35)
        assertTrue(results.stream().allMatch(user -> user.getAge() >= 25 && user.getAge() <= 35));
    }

    @Test
    void testIsNullFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.EMAIL, Operator.IS_NULL, null
        );
        contextAdapter.addCondition("emailFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("emailFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertNull(results.get(0).getEmail());
    }

    @Test
    void testComplexCombinedFilters() {
        // Arrange - Créer deux conditions séparées
        FilterDefinition<UserPropertyRef> ageFilter = new FilterDefinition<>(
            UserPropertyRef.AGE, Operator.GREATER_THAN, 20
        );
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.NAME, Operator.LIKE, "%John%"
        );
        
        contextAdapter.addCondition("ageFilter", ageFilter);
        contextAdapter.addCondition("nameFilter", nameFilter);

        // Act - Combiner les conditions avec AND
        Condition ageCondition = contextAdapter.getCondition("ageFilter");
        Condition nameCondition = contextAdapter.getCondition("nameFilter");
        Condition combinedCondition = ageCondition.and(nameCondition);

        // Convertir en Specification
        Specification<User> spec = ((ConditionAdapter<User>) combinedCondition).getSpecification();
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getName());
        assertTrue(results.get(0).getAge() > 20);
    }

    @Test
    void testSpecificationBuilderWithDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Operator.GREATER_THAN_OR_EQUAL, 25),
                "nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Operator.LIKE, "%John%")
            ),
            "ageFilter & nameFilter"
        );

        // Act
        Specification<User> spec = SpecificationBuilder.toSpecification(filterRequest);
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getName());
        assertTrue(results.get(0).getAge() >= 25);
    }

    @Test
    void testSpecificationBuilderWithComplexDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "ageFilter", new FilterDefinition<>(UserPropertyRef.AGE, Operator.GREATER_THAN, 20),
                "nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Operator.LIKE, "%John%"),
                "emailFilter", new FilterDefinition<>(UserPropertyRef.EMAIL, Operator.IS_NOT_NULL, null)
            ),
            "(ageFilter & nameFilter) | emailFilter"
        );

        // Act
        Specification<User> spec = SpecificationBuilder.toSpecification(filterRequest);
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(3, results.size()); // Tous les utilisateurs sauf celui avec email null
    }

    @Test
    void testNestedPropertyPath() {
        // Arrange - Test avec une propriété imbriquée (si elle existait)
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Operator.EQUALS, "John Doe"
        );
        contextAdapter.addCondition("nameFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("nameFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getName());
    }

    @Test
    void testInvalidOperatorThrowsException() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.NAME, Operator.GREATER_THAN, "John Doe" // Opérateur non supporté pour String
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.addCondition("nameFilter", filterDef);
        });
    }

    @Test
    void testNonExistentFilterKey() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            Condition condition = contextAdapter.getCondition("nonExistent");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Specification<User> spec = contextAdapter.getSpecification("nonExistent");
        });
    }

    private void createTestUsers() {
        User user1 = new User("John Doe", 25, "john@example.com", LocalDateTime.now());
        User user2 = new User("Jane Smith", 30, "jane@example.com", LocalDateTime.now());
        User user3 = new User("Bob Smith", 35, null, LocalDateTime.now());
        User user4 = new User("Alice Johnson", 20, "alice@example.com", LocalDateTime.now());

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
        entityManager.persistAndFlush(user4);
    }
}


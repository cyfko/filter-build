package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.PropertyRef;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour l'adaptateur Spring complet.
 * Teste l'intégration avec Spring Data JPA et une base de données en mémoire.
 */
@DataJpaTest
@EnableAutoConfiguration
class SpringIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private SpringContextAdapter<User, UserPropertyRef> contextAdapter;
    private SpringConditionAdapterBuilder<User, UserPropertyRef> conditionBuilder;

    @BeforeEach
    void setUp() {
        conditionBuilder = new SpringConditionAdapterBuilder<User, UserPropertyRef>() {};
        contextAdapter = new SpringContextAdapter<>(conditionBuilder);
        
        // Créer des données de test
        createTestUsers();
    }

    @Test
    void testSimpleEqualsFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_NAME, Operator.EQUALS, "John Doe"
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
            UserPropertyRef.USER_NAME, Operator.LIKE, "%John%"
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
    void testGreaterThanFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 25
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
            UserPropertyRef.USER_NAME, Operator.IN, names
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
            UserPropertyRef.USER_AGE, Operator.BETWEEN, ageRange
        );
        contextAdapter.addCondition("ageFilter", filterDef);

        // Act
        Specification<User> spec = contextAdapter.getSpecification("ageFilter");
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(2, results.size()); // John (25) et Bob (35)
        assertTrue(results.stream().allMatch(user -> user.getAge() >= 25 && user.getAge() <= 35));
    }

    @Test
    void testIsNullFilter() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_EMAIL, Operator.IS_NULL, null
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
            UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 20
        );
        FilterDefinition<UserPropertyRef> nameFilter = new FilterDefinition<>(
            UserPropertyRef.USER_NAME, Operator.LIKE, "%John%"
        );
        
        contextAdapter.addCondition("ageFilter", ageFilter);
        contextAdapter.addCondition("nameFilter", nameFilter);

        // Act - Combiner les conditions avec AND
        Condition ageCondition = contextAdapter.getCondition("ageFilter");
        Condition nameCondition = contextAdapter.getCondition("nameFilter");
        Condition combinedCondition = ageCondition.and(nameCondition);

        // Convertir en Specification
        Specification<User> spec = ((SpringConditionAdapter<User>) combinedCondition).getSpecification();
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
                "ageFilter", new FilterDefinition<>(UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 25),
                "nameFilter", new FilterDefinition<>(UserPropertyRef.USER_NAME, Operator.LIKE, "%John%")
            ),
            "ageFilter AND nameFilter"
        );

        // Act
        Specification<User> spec = SpecificationBuilder.toSpecification(filterRequest);
        List<User> results = userRepository.findAll(spec);

        // Assert
        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getName());
        assertTrue(results.get(0).getAge() > 25);
    }

    @Test
    void testSpecificationBuilderWithComplexDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<>(
            Map.of(
                "ageFilter", new FilterDefinition<>(UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 20),
                "nameFilter", new FilterDefinition<>(UserPropertyRef.USER_NAME, Operator.LIKE, "%John%"),
                "emailFilter", new FilterDefinition<>(UserPropertyRef.USER_EMAIL, Operator.IS_NOT_NULL, null)
            ),
            "(ageFilter AND nameFilter) OR emailFilter"
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
            UserPropertyRef.USER_NAME, Operator.EQUALS, "John Doe"
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
            UserPropertyRef.USER_NAME, Operator.GREATER_THAN, "John Doe" // Opérateur non supporté pour String
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            contextAdapter.addCondition("nameFilter", filterDef);
        });
    }

    @Test
    void testNonExistentFilterKey() {
        // Act
        Condition condition = contextAdapter.getCondition("nonExistent");
        Specification<User> spec = contextAdapter.getSpecification("nonExistent");

        // Assert
        assertNull(condition);
        assertNull(spec);
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

    // Entity class
    @Entity
    @Table(name = "users")
    static class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name")
        private String name;

        @Column(name = "age")
        private Integer age;

        @Column(name = "email")
        private String email;

        @Column(name = "created_at")
        private LocalDateTime createdAt;

        public User() {}

        public User(String name, Integer age, String email, LocalDateTime createdAt) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.createdAt = createdAt;
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // Repository interface
    interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {}

    // Property reference enum
    enum UserPropertyRef implements PropertyRef, PathShape {
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

        UserPropertyRef(String path, Class<?> type, Set<Operator> supportedOperators) {
            this.path = path;
            this.type = type;
            this.supportedOperators = supportedOperators;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Set<Operator> getSupportedOperators() {
            return supportedOperators;
        }

        @Override
        public void validateOperator(Operator operator) {
            if (!supportedOperators.contains(operator)) {
                throw new IllegalArgumentException("Operator " + operator + " not supported for " + this);
            }
        }

        @Override
        public void validateOperatorForValue(Operator operator, Object value) {
            validateOperator(operator);
            // Additional value validation could be added here
        }
    }
}


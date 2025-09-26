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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de performance pour l'adaptateur Spring.
 * Teste les performances avec de gros volumes de données.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false" // Désactiver les logs SQL pour les tests de performance
})
class SpringPerformanceTest {

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
        
        // Créer un grand nombre de données de test
        createLargeTestDataset();
    }

    @Test
    void testPerformanceWithLargeDataset() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 25
        );
        contextAdapter.addCondition("ageFilter", filterDef);

        // Act
        long startTime = System.currentTimeMillis();
        Specification<User> spec = contextAdapter.getSpecification("ageFilter");
        List<User> results = userRepository.findAll(spec);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(results);
        assertTrue(results.size() > 0);
        
        long executionTime = endTime - startTime;
        System.out.println("Execution time for large dataset: " + executionTime + "ms");
        System.out.println("Number of results: " + results.size());
        
        // Vérifier que le temps d'exécution est raisonnable (moins de 5 secondes)
        assertTrue(executionTime < 5000, "Query execution time should be less than 5 seconds");
    }

    @Test
    void testPerformanceWithComplexDSL() throws DSLSyntaxException, FilterValidationException {
        // Arrange
        FilterRequest<UserPropertyRef> filterRequest = new FilterRequest<UserPropertyRef>(
            Map.of(
                "ageFilter1", new FilterDefinition<>(UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 20),
                "ageFilter2", new FilterDefinition<>(UserPropertyRef.USER_AGE, Operator.LESS_THAN, 50),
                "nameFilter", new FilterDefinition<>(UserPropertyRef.USER_NAME, Operator.LIKE, "%User%"),
                "emailFilter", new FilterDefinition<>(UserPropertyRef.USER_EMAIL, Operator.IS_NOT_NULL, null)
            ),
            "(ageFilter1 AND ageFilter2) AND (nameFilter OR emailFilter)"
        );

        // Act
        long startTime = System.currentTimeMillis();
        Specification<User> spec = SpecificationBuilder.toSpecification(filterRequest);
        List<User> results = userRepository.findAll(spec);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(results);
        
        long executionTime = endTime - startTime;
        System.out.println("Execution time for complex DSL: " + executionTime + "ms");
        System.out.println("Number of results: " + results.size());
        
        // Vérifier que le temps d'exécution est raisonnable
        assertTrue(executionTime < 5000, "Complex DSL execution time should be less than 5 seconds");
    }

    @Test
    void testPerformanceWithMultipleConditions() {
        // Arrange
        List<FilterDefinition<UserPropertyRef>> filters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            filters.add(new FilterDefinition<>(
                UserPropertyRef.USER_AGE, Operator.GREATER_THAN, i * 5
            ));
        }

        // Act
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < filters.size(); i++) {
            contextAdapter.addCondition("filter" + i, filters.get(i));
        }

        // Combiner toutes les conditions avec AND
        Condition combinedCondition = null;
        for (int i = 0; i < filters.size(); i++) {
            Condition condition = contextAdapter.getCondition("filter" + i);
            if (combinedCondition == null) {
                combinedCondition = condition;
            } else {
                combinedCondition = combinedCondition.and(condition);
            }
        }

        Specification<User> spec = ((SpringConditionAdapter<User>) combinedCondition).getSpecification();
        List<User> results = userRepository.findAll(spec);
        
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(results);
        
        long executionTime = endTime - startTime;
        System.out.println("Execution time for multiple conditions: " + executionTime + "ms");
        System.out.println("Number of results: " + results.size());
        
        // Vérifier que le temps d'exécution est raisonnable
        assertTrue(executionTime < 5000, "Multiple conditions execution time should be less than 5 seconds");
    }

    @Test
    void testPerformanceWithInOperator() {
        // Arrange
        List<String> names = IntStream.range(0, 100)
            .mapToObj(i -> "User" + i)
                .collect(Collectors.toList());
        
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_NAME, Operator.IN, names
        );
        contextAdapter.addCondition("nameFilter", filterDef);

        // Act
        long startTime = System.currentTimeMillis();
        Specification<User> spec = contextAdapter.getSpecification("nameFilter");
        List<User> results = userRepository.findAll(spec);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(results);
        
        long executionTime = endTime - startTime;
        System.out.println("Execution time for IN operator with 100 values: " + executionTime + "ms");
        System.out.println("Number of results: " + results.size());
        
        // Vérifier que le temps d'exécution est raisonnable
        assertTrue(executionTime < 5000, "IN operator execution time should be less than 5 seconds");
    }

    @Test
    void testPerformanceWithBetweenOperator() {
        // Arrange
        List<Integer> ageRange = Arrays.asList(25, 35);
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE, Operator.BETWEEN, ageRange
        );
        contextAdapter.addCondition("ageFilter", filterDef);

        // Act
        long startTime = System.currentTimeMillis();
        Specification<User> spec = contextAdapter.getSpecification("ageFilter");
        List<User> results = userRepository.findAll(spec);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(results);
        
        long executionTime = endTime - startTime;
        System.out.println("Execution time for BETWEEN operator: " + executionTime + "ms");
        System.out.println("Number of results: " + results.size());
        
        // Vérifier que le temps d'exécution est raisonnable
        assertTrue(executionTime < 5000, "BETWEEN operator execution time should be less than 5 seconds");
    }

    @Test
    void testMemoryUsageWithLargeDataset() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 25
        );
        contextAdapter.addCondition("ageFilter", filterDef);

        // Act
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        Specification<User> spec = contextAdapter.getSpecification("ageFilter");
        List<User> results = userRepository.findAll(spec);
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        // Assert
        assertNotNull(results);
        
        System.out.println("Memory used: " + memoryUsed + " bytes");
        System.out.println("Number of results: " + results.size());
        
        // Vérifier que l'utilisation mémoire est raisonnable (moins de 100MB)
        assertTrue(memoryUsed < 100 * 1024 * 1024, "Memory usage should be less than 100MB");
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.USER_AGE, Operator.GREATER_THAN, 25
        );
        contextAdapter.addCondition("ageFilter", filterDef);

        // Act
        List<Thread> threads = new ArrayList<>();
        List<List<User>> results = Collections.synchronizedList(new ArrayList<>());
        
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                Specification<User> spec = contextAdapter.getSpecification("ageFilter");
                List<User> threadResults = userRepository.findAll(spec);
                results.add(threadResults);
            });
            threads.add(thread);
        }

        long startTime = System.currentTimeMillis();
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();

        // Assert
        assertEquals(10, results.size());
        for (List<User> result : results) {
            assertNotNull(result);
            assertTrue(result.size() > 0);
        }
        
        long executionTime = endTime - startTime;
        System.out.println("Concurrent execution time: " + executionTime + "ms");
        
        // Vérifier que le temps d'exécution concurrent est raisonnable
        assertTrue(executionTime < 10000, "Concurrent execution time should be less than 10 seconds");
    }

    private void createLargeTestDataset() {
        // Créer 1000 utilisateurs de test
        for (int i = 0; i < 1000; i++) {
            User user = new User(
                "User" + i,
                20 + (i % 50), // Âge entre 20 et 69
                i % 2 == 0 ? "user" + i + "@example.com" : null, // Email alterné
                LocalDateTime.now().minusDays(i % 365)
            );
            entityManager.persist(user);
        }
        entityManager.flush();
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


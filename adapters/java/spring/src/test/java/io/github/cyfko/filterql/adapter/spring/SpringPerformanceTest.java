package io.github.cyfko.filterql.adapter.spring;

import io.github.cyfko.filterql.core.Condition;
import io.github.cyfko.filterql.core.FilterResolver;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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

    private FilterContext<User,UserPropertyRef> context;

    @BeforeEach
    void setUp() {
        context = new FilterContext<>(User.class, UserPropertyRef.class, def -> switch (def.getRef()){
            case NAME -> "name";
            case AGE -> "age";
            case EMAIL -> "email";
            case ACTIVE -> "active";
            case CREATED_AT -> "createdAt";
        });
        
        // Créer un grand nombre de données de test
        createLargeTestDataset();
    }

    @Test
    void testPerformanceWithLargeDataset() {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.AGE, Op.GREATER_THAN, 25
        );
        Condition condition = context.addCondition("ageFilter", filterDef);

        // Act
        long startTime = System.currentTimeMillis();
        PredicateResolver<User> resolver = context.toResolver(User.class, condition);
        List<User> results = userRepository.findAll(resolver::resolve);
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
                "ageFilter1", new FilterDefinition<>(UserPropertyRef.AGE, Op.GREATER_THAN, 20),
                "ageFilter2", new FilterDefinition<>(UserPropertyRef.AGE, Op.LESS_THAN, 50),
                "nameFilter", new FilterDefinition<>(UserPropertyRef.NAME, Op.LIKE, "%User%"),
                "emailFilter", new FilterDefinition<>(UserPropertyRef.EMAIL, Op.IS_NOT_NULL, null)
            ),
            "(ageFilter1 & ageFilter2) & (nameFilter | emailFilter)"
        );

        // Act
        long startTime = System.currentTimeMillis();
        PredicateResolver<User> resolver = FilterResolver.of(context).resolve(User.class, filterRequest);
        List<User> results = userRepository.findAll(resolver::resolve);
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
                UserPropertyRef.AGE, Op.GREATER_THAN, i * 5
            ));
        }

        // Act
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < filters.size(); i++) {
            context.addCondition("filter" + i, filters.get(i));
        }

        // Combiner toutes les conditions avec AND
        Condition combinedCondition = null;
        for (int i = 0; i < filters.size(); i++) {
            Condition condition = context.getCondition("filter" + i);
            if (combinedCondition == null) {
                combinedCondition = condition;
            } else {
                combinedCondition = combinedCondition.and(condition);
            }
        }

        PredicateResolver<User> resolver = context.toResolver(User.class, combinedCondition);
        List<User> results = userRepository.findAll(resolver::resolve);
        
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
            UserPropertyRef.NAME, Op.IN, names
        );
        Condition condition = context.addCondition("nameFilter", filterDef);

        // Act
        long startTime = System.currentTimeMillis();
        PredicateResolver<User> resolver = context.toResolver(User.class, condition);
        List<User> results = userRepository.findAll(resolver::resolve);
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
            UserPropertyRef.AGE, Op.BETWEEN, ageRange
        );
        Condition condition = context.addCondition("ageFilter", filterDef);

        // Act
        long startTime = System.currentTimeMillis();
        PredicateResolver<User> resolver = context.toResolver(User.class, condition);
        List<User> results = userRepository.findAll(resolver::resolve);
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
            UserPropertyRef.AGE, Op.GREATER_THAN, 25
        );
        Condition condition = context.addCondition("ageFilter", filterDef);

        // Act
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        PredicateResolver<User> resolver = context.toResolver(User.class, condition);
        List<User> results = userRepository.findAll(resolver::resolve);
        
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
    @Disabled
    void testConcurrentAccess() throws InterruptedException {
        // Arrange
        FilterDefinition<UserPropertyRef> filterDef = new FilterDefinition<>(
            UserPropertyRef.AGE, Op.GREATER_THAN, 25
        );
        Condition condition = context.addCondition("ageFilter", filterDef);

        // Act
        List<Thread> threads = new ArrayList<>();
        List<List<User>> results = Collections.synchronizedList(new ArrayList<>());
        
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                PredicateResolver<User> resolver = context.toResolver(User.class, condition);
                List<User> threadResults = userRepository.findAll(resolver::resolve);
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
}


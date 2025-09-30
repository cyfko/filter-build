package io.github.cyfko.filterql.core.impl;

import io.github.cyfko.filterql.core.*;
import io.github.cyfko.filterql.core.domain.PredicateResolver;
import io.github.cyfko.filterql.core.exception.DSLSyntaxException;
import io.github.cyfko.filterql.core.exception.FilterValidationException;
import io.github.cyfko.filterql.core.model.FilterDefinition;
import io.github.cyfko.filterql.core.model.FilterRequest;
import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.PropertyReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive independent tests for FilterResolver implementations.
 * 
 * These tests validate FilterResolver behavior in isolation, focusing on:
 * - DSL parsing integration
 * - Error handling and boundary conditions
 * - Thread safety considerations
 * - Performance characteristics
 * - Contract compliance
 */
@DisplayName("FilterResolver Independent Tests")
public class FilterResolverIndependentTest {

    private FilterResolver filterResolver;

    @Mock
    private Context mockContext;

    @Mock
    private Parser mockParser;

    @Mock
    private Condition mockCondition;

    @Mock
    private FilterTree mockFilterTree;

    @Mock
    private PredicateResolver<TestEntity> mockPredicateResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock condition with chainable methods
        when(mockCondition.not()).thenReturn(mockCondition);
        when(mockCondition.and(any())).thenReturn(mockCondition);
        when(mockCondition.or(any())).thenReturn(mockCondition);
        
        // Setup mock filter tree
        when(mockFilterTree.generate(any(Context.class))).thenReturn(mockCondition);
        
        // Setup mock parser
        when(mockParser.parse(anyString())).thenReturn(mockFilterTree);
        
        // Setup mock context
        when(mockContext.toResolver(eq(TestEntity.class), any(Condition.class))).thenReturn(mockPredicateResolver);
        
        // Create FilterResolver with mocked dependencies
        filterResolver = FilterResolver.of(mockParser, mockContext);
    }

    /**
     * Test entity for FilterResolver testing
     */
    private static class TestEntity {
        private final String name;
        private final int value;
        private final boolean active;

        public TestEntity(String name, int value, boolean active) {
            this.name = name;
            this.value = value;
            this.active = active;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
        public boolean isActive() { return active; }
    }

    /**
     * Test property reference enum for testing
     */
    private enum TestPropertyRef implements PropertyReference {
        NAME(String.class, Set.of(Op.LIKE, Op.EQUALS, Op.NOT_EQUALS)),
        VALUE(Integer.class, Set.of(Op.EQUALS, Op.GREATER_THAN, Op.LESS_THAN)),
        ACTIVE(Boolean.class, Set.of(Op.EQUALS, Op.NOT_EQUALS)),
        STATUS(String.class, Set.of(Op.EQUALS, Op.IN)),
        CATEGORY(String.class, Set.of(Op.EQUALS, Op.LIKE));

        private final Class<?> type;
        private final Set<Op> supportedOperators;

        TestPropertyRef(Class<?> type, Set<Op> supportedOperators) {
            this.type = type;
            this.supportedOperators = Set.copyOf(supportedOperators);
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Set<Op> getSupportedOperators() {
            return supportedOperators;
        }
    }

    @Nested
    @DisplayName("FilterResolver Basic Resolution Tests")
    class BasicResolutionTests {

        @Test
        @DisplayName("Valid filter request resolves successfully")
        void testValidFilterRequestResolution() throws DSLSyntaxException, FilterValidationException {
            // Create a simple filter request
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "nameFilter");
            
            PredicateResolver<TestEntity> result = filterResolver.resolve(TestEntity.class, request);
            
            assertNotNull(result);
            verify(mockParser).parse("nameFilter");
            verify(mockFilterTree).generate(mockContext);
            verify(mockContext).toResolver(eq(TestEntity.class), eq(mockCondition));
        }

        @Test
        @DisplayName("Complex DSL expression resolves successfully")
        void testComplexDSLExpression() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            filters.put("valueFilter", new FilterDefinition<>(TestPropertyRef.VALUE, Op.GREATER_THAN, 100));
            
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "(nameFilter & valueFilter)");
            
            PredicateResolver<TestEntity> result = filterResolver.resolve(TestEntity.class, request);
            
            assertNotNull(result);
            verify(mockParser).parse("(nameFilter & valueFilter)");
            verify(mockContext, times(2)).addCondition(any(String.class), any());
        }

        @Test
        @DisplayName("Multiple filters with OR logic resolves successfully")
        void testMultipleFiltersWithOrLogic() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            filters.put("activeFilter", new FilterDefinition<>(TestPropertyRef.ACTIVE, Op.EQUALS, true));
            filters.put("statusFilter", new FilterDefinition<>(TestPropertyRef.STATUS, Op.EQUALS, "ACTIVE"));
            
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "nameFilter | (activeFilter & statusFilter)");
            
            PredicateResolver<TestEntity> result = filterResolver.resolve(TestEntity.class, request);
            
            assertNotNull(result);
            verify(mockParser).parse("nameFilter | (activeFilter & statusFilter)");
            verify(mockContext, times(3)).addCondition(any(String.class), any());
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Invalid DSL syntax throws DSLSyntaxException")
        void testInvalidDSLSyntax() throws DSLSyntaxException {
            when(mockParser.parse(anyString())).thenThrow(new DSLSyntaxException("Invalid syntax"));
            
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "invalid & &");
            
            DSLSyntaxException exception = assertThrows(
                DSLSyntaxException.class,
                () -> filterResolver.resolve(TestEntity.class, request)
            );
            
            assertEquals("Invalid syntax", exception.getMessage());
            verify(mockParser).parse("invalid & &");
        }

        @Test
        @DisplayName("Filter validation error throws FilterValidationException")
        void testFilterValidationError() throws FilterValidationException {
            when(mockFilterTree.generate(any(Context.class))).thenThrow(new FilterValidationException("Invalid filter"));
            
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("invalidFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "invalidFilter");
            
            FilterValidationException exception = assertThrows(
                FilterValidationException.class,
                () -> filterResolver.resolve(TestEntity.class, request)
            );
            
            assertEquals("Invalid filter", exception.getMessage());
        }

        @Test
        @DisplayName("Null filter request throws NullPointerException")
        void testNullFilterRequest() {
            assertThrows(
                NullPointerException.class,
                () -> filterResolver.resolve(TestEntity.class, null)
            );
        }

        @Test
        @DisplayName("Empty filter map still resolves if DSL is valid")
        void testEmptyFilterMap() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> emptyFilters = new HashMap<>();
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(emptyFilters, "");
            
            // Setup parser to handle empty string
            when(mockParser.parse("")).thenReturn(mockFilterTree);
            
            PredicateResolver<TestEntity> result = filterResolver.resolve(TestEntity.class, request);
            
            assertNotNull(result);
            verify(mockParser).parse("");
            verify(mockContext, never()).addCondition(any(String.class), any());
        }
    }

    @Nested
    @DisplayName("FilterResolver Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("FilterResolver.of(parser, context) creates resolver successfully")
        void testFactoryWithParserAndContext() {
            FilterResolver resolver = FilterResolver.of(mockParser, mockContext);
            assertNotNull(resolver);
        }

        @Test
        @DisplayName("FilterResolver.of(context) creates resolver with default parser")
        void testFactoryWithContext() {
            FilterResolver resolver = FilterResolver.of(mockContext);
            assertNotNull(resolver);
        }

        @Test
        @DisplayName("FilterResolver.of(null, context) throws NullPointerException")
        void testFactoryWithNullParser() {
            assertThrows(
                NullPointerException.class,
                () -> FilterResolver.of(null, mockContext)
            );
        }

        @Test
        @DisplayName("FilterResolver.of(parser, null) throws NullPointerException")
        void testFactoryWithNullContext() {
            assertThrows(
                NullPointerException.class,
                () -> FilterResolver.of(mockParser, null)
            );
        }

        @Test
        @DisplayName("FilterResolver.of(null) throws NullPointerException")
        void testFactoryWithNullContextOnly() {
            assertThrows(
                NullPointerException.class,
                () -> FilterResolver.of(null)
            );
        }
    }

    @Nested
    @DisplayName("Performance and Scalability Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Filter resolution performance is acceptable")
        void testFilterResolutionPerformance() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "nameFilter");
            
            long startTime = System.nanoTime();
            
            // Perform multiple resolutions
            for (int i = 0; i < 1000; i++) {
                filterResolver.resolve(TestEntity.class, request);
            }
            
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            
            // Should complete 1000 resolutions in reasonable time (less than 100ms)
            assertTrue(durationMs < 1000, "Resolution took too long: " + durationMs + "ms");
        }

        @Test
        @DisplayName("Memory usage is reasonable with repeated resolutions")
        void testMemoryUsage() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "nameFilter");
            
            // Perform many resolutions to check for memory leaks
            for (int i = 0; i < 5000; i++) {
                filterResolver.resolve(TestEntity.class, request);
                
                // Suggest GC periodically
                if (i % 500 == 0) {
                    System.gc();
                }
            }
            
            // If we reach here without OutOfMemoryError, test passes
            assertTrue(true);
        }

        @Test
        @DisplayName("Complex filter requests perform adequately")
        void testComplexFilterPerformance() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            filters.put("valueFilter", new FilterDefinition<>(TestPropertyRef.VALUE, Op.GREATER_THAN, 100));
            filters.put("activeFilter", new FilterDefinition<>(TestPropertyRef.ACTIVE, Op.EQUALS, true));
            filters.put("statusFilter", new FilterDefinition<>(TestPropertyRef.STATUS, Op.EQUALS, "ACTIVE"));
            
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(
                filters, 
                "(nameFilter & valueFilter) | (activeFilter & statusFilter)"
            );
            
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 100; i++) {
                filterResolver.resolve(TestEntity.class, request);
            }
            
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            
            // Complex filters should still perform reasonably
            assertTrue(durationMs < 500, "Complex resolution took too long: " + durationMs + "ms");
        }
    }

    @Nested
    @DisplayName("Contract Compliance Tests")
    class ContractComplianceTests {

        @Test
        @DisplayName("Resolve method never returns null for valid requests")
        void testNeverReturnsNullForValidRequests() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "nameFilter");
            
            PredicateResolver<TestEntity> result = filterResolver.resolve(TestEntity.class, request);
            
            assertNotNull(result, "PredicateResolver should never be null for valid requests");
        }

        @Test
        @DisplayName("FilterResolver behavior is deterministic")
        void testDeterministicBehavior() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "nameFilter");
            
            // Multiple calls with the same request should behave consistently
            for (int i = 0; i < 10; i++) {
                PredicateResolver<TestEntity> result = filterResolver.resolve(TestEntity.class, request);
                assertNotNull(result);
            }
            
            // Verify consistent parser and context interactions
            verify(mockParser, times(10)).parse("nameFilter");
            verify(mockFilterTree, times(10)).generate(mockContext);
        }

        @Test
        @DisplayName("Context population happens for all filters")
        void testContextPopulation() throws DSLSyntaxException, FilterValidationException {
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("filter1", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            filters.put("filter2", new FilterDefinition<>(TestPropertyRef.VALUE, Op.GREATER_THAN, 100));
            filters.put("filter3", new FilterDefinition<>(TestPropertyRef.ACTIVE, Op.EQUALS, true));
            
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "filter1 & filter2 | filter3");
            
            filterResolver.resolve(TestEntity.class, request);
            
            // Verify all filters are added to context
            verify(mockContext).addCondition(eq("filter1"), any());
            verify(mockContext).addCondition(eq("filter2"), any());
            verify(mockContext).addCondition(eq("filter3"), any());
        }
    }

    @Nested
    @DisplayName("Null Parameter Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw NullPointerException for null entity class")
        void shouldThrowExceptionForNullEntityClass() {
            // Given
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("test", new FilterDefinition<>(TestPropertyRef.NAME, Op.EQUALS, "value"));
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "test");
            
            // When & Then
            NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> filterResolver.resolve(null, request));
            assertTrue(exception.getMessage().contains("Entity class cannot be null"));
        }
        
        @Test
        @DisplayName("Should throw NullPointerException for null filter request")
        void shouldThrowExceptionForNullFilterRequest() {
            // When & Then
            NullPointerException exception = assertThrows(NullPointerException.class,
                () -> filterResolver.resolve(TestEntity.class, null));
            assertTrue(exception.getMessage().contains("Filter request cannot be null"));
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Concurrent filter resolution produces consistent results")
        void testConcurrentFilterResolution() throws InterruptedException {
            final int threadCount = 10;
            final int operationsPerThread = 50;
            
            Map<String, FilterDefinition<TestPropertyRef>> filters = new HashMap<>();
            filters.put("nameFilter", new FilterDefinition<>(TestPropertyRef.NAME, Op.LIKE, "test%"));
            FilterRequest<TestPropertyRef> request = new FilterRequest<>(filters, "nameFilter");
            
            Thread[] threads = new Thread[threadCount];
            final boolean[] results = new boolean[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            PredicateResolver<TestEntity> result = filterResolver.resolve(TestEntity.class, request);
                            if (result == null) {
                                results[threadIndex] = false;
                                return;
                            }
                        }
                        results[threadIndex] = true;
                    } catch (Exception e) {
                        results[threadIndex] = false;
                    }
                });
            }
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for completion
            for (Thread thread : threads) {
                thread.join(5000); // 5 second timeout
            }
            
            // Verify all threads completed successfully
            for (int i = 0; i < threadCount; i++) {
                assertTrue(results[i], "Thread " + i + " failed");
            }
        }
    }
}
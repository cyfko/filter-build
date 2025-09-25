package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.DefinedPropertyRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class FilterRequestTest {

    @Test
    @DisplayName("Should create FilterRequest with filters and combination")
    void shouldCreateFilterRequestWithFiltersAndCombination() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyRef>> filters = new HashMap<>();
        filters.put("filter1", new FilterDefinition<>(DefinedPropertyRef.USER_NAME, Operator.EQUALS, "value1"));
        filters.put("filter2", new FilterDefinition<>(DefinedPropertyRef.USER_AGE, Operator.GREATER_THAN, 10));
        String combineWith = "AND";

        // When
        FilterRequest<DefinedPropertyRef> filterRequest = new FilterRequest<>(filters, combineWith);

        // Then
        assertEquals(filters, filterRequest.getFilters());
        assertEquals(combineWith, filterRequest.getCombineWith());
        assertEquals(2, filterRequest.getFilters().size());
    }

    @Test
    @DisplayName("Should create FilterRequest with empty filters")
    void shouldCreateFilterRequestWithEmptyFilters() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyRef>> filters = new HashMap<>();
        String combineWith = "OR";

        // When
        FilterRequest<DefinedPropertyRef> filterRequest = new FilterRequest<>(filters, combineWith);

        // Then
        assertEquals(filters, filterRequest.getFilters());
        assertEquals(combineWith, filterRequest.getCombineWith());
        assertTrue(filterRequest.getFilters().isEmpty());
    }

    @Test
    @DisplayName("Should create FilterRequest with null combineWith")
    void shouldCreateFilterRequestWithNullCombineWith() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyRef>> filters = new HashMap<>();
        filters.put("filter1", new FilterDefinition<>(DefinedPropertyRef.USER_NAME, Operator.EQUALS, "value"));
        String combineWith = null;

        // When
        FilterRequest<DefinedPropertyRef> filterRequest = new FilterRequest<>(filters, combineWith);

        // Then
        assertEquals(filters, filterRequest.getFilters());
        assertNull(filterRequest.getCombineWith());
    }

    @Test
    @DisplayName("Should handle complex filter scenarios")
    void shouldHandleComplexFilterScenarios() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyRef>> filters = new HashMap<>();
        filters.put("nameFilter", new FilterDefinition<>(DefinedPropertyRef.USER_NAME, Operator.LIKE, "John%"));
        filters.put("ageFilter", new FilterDefinition<>(DefinedPropertyRef.USER_AGE, Operator.BETWEEN, new Object[]{18, 65}));
        filters.put("statusFilter", new FilterDefinition<>(DefinedPropertyRef.USER_STATUS, Operator.IN, new String[]{"ACTIVE", "PENDING"}));
        filters.put("nullFilter", new FilterDefinition<>(DefinedPropertyRef.USER_EMAIL, Operator.IS_NOT_NULL, null));

        // When
        FilterRequest<DefinedPropertyRef> filterRequest = new FilterRequest<>(filters, "AND");

        // Then
        assertEquals(4, filterRequest.getFilters().size());
        assertTrue(filterRequest.getFilters().containsKey("nameFilter"));
        assertTrue(filterRequest.getFilters().containsKey("ageFilter"));
        assertTrue(filterRequest.getFilters().containsKey("statusFilter"));
        assertTrue(filterRequest.getFilters().containsKey("nullFilter"));
        assertEquals("AND", filterRequest.getCombineWith());
    }

    @Test
    @DisplayName("Should handle toString method")
    void shouldHandleToStringMethod() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyRef>> filters = new HashMap<>();
        filters.put("test", new FilterDefinition<>(DefinedPropertyRef.USER_NAME, Operator.EQUALS, "value"));
        FilterRequest<DefinedPropertyRef> filterRequest = new FilterRequest<>(filters, "AND");

        // When
        String result = filterRequest.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("FilterRequest"));
    }
}

package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.DefinedPropertyReference;
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
        Map<String, FilterDefinition<DefinedPropertyReference>> filters = new HashMap<>();
        filters.put("filter1", new FilterDefinition<>(DefinedPropertyReference.USER_NAME, Op.EQUALS, "value1"));
        filters.put("filter2", new FilterDefinition<>(DefinedPropertyReference.USER_AGE, Op.GREATER_THAN, 10));
        String combineWith = "AND";

        // When
        FilterRequest<DefinedPropertyReference> filterRequest = new FilterRequest<>(filters, combineWith);

        // Then
        assertEquals(filters, filterRequest.getFilters());
        assertEquals(combineWith, filterRequest.getCombineWith());
        assertEquals(2, filterRequest.getFilters().size());
    }

    @Test
    @DisplayName("Should create FilterRequest with empty filters")
    void shouldCreateFilterRequestWithEmptyFilters() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyReference>> filters = new HashMap<>();
        String combineWith = "OR";

        // When
        FilterRequest<DefinedPropertyReference> filterRequest = new FilterRequest<>(filters, combineWith);

        // Then
        assertEquals(filters, filterRequest.getFilters());
        assertEquals(combineWith, filterRequest.getCombineWith());
        assertTrue(filterRequest.getFilters().isEmpty());
    }

    @Test
    @DisplayName("Should create FilterRequest with null combineWith")
    void shouldCreateFilterRequestWithNullCombineWith() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyReference>> filters = new HashMap<>();
        filters.put("filter1", new FilterDefinition<>(DefinedPropertyReference.USER_NAME, Op.EQUALS, "value"));
        String combineWith = null;

        // When
        FilterRequest<DefinedPropertyReference> filterRequest = new FilterRequest<>(filters, combineWith);

        // Then
        assertEquals(filters, filterRequest.getFilters());
        assertNull(filterRequest.getCombineWith());
    }

    @Test
    @DisplayName("Should handle complex filter scenarios")
    void shouldHandleComplexFilterScenarios() {
        // Given
        Map<String, FilterDefinition<DefinedPropertyReference>> filters = new HashMap<>();
        filters.put("nameFilter", new FilterDefinition<>(DefinedPropertyReference.USER_NAME, Op.LIKE, "John%"));
        filters.put("ageFilter", new FilterDefinition<>(DefinedPropertyReference.USER_AGE, Op.BETWEEN, new Object[]{18, 65}));
        filters.put("statusFilter", new FilterDefinition<>(DefinedPropertyReference.USER_STATUS, Op.IN, new String[]{"ACTIVE", "PENDING"}));
        filters.put("nullFilter", new FilterDefinition<>(DefinedPropertyReference.USER_EMAIL, Op.IS_NOT_NULL, null));

        // When
        FilterRequest<DefinedPropertyReference> filterRequest = new FilterRequest<>(filters, "AND");

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
        Map<String, FilterDefinition<DefinedPropertyReference>> filters = new HashMap<>();
        filters.put("test", new FilterDefinition<>(DefinedPropertyReference.USER_NAME, Op.EQUALS, "value"));
        FilterRequest<DefinedPropertyReference> filterRequest = new FilterRequest<>(filters, "AND");

        // When
        String result = filterRequest.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("FilterRequest"));
    }
}

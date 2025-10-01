package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Op;
import io.github.cyfko.filterql.core.validation.DefinedPropertyReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class FilterDefinitionTest {

    @Test
    @DisplayName("Should create FilterDefinition with valid parameters")
    void shouldCreateFilterDefinitionWithValidParameters() {
        // Given
        DefinedPropertyReference ref = DefinedPropertyReference.USER_NAME;
        Op operator = Op.EQ;
        Object value = "test";

        // When
        FilterDefinition<DefinedPropertyReference> filterDefinition = new FilterDefinition<>(ref, operator, value);

        // Then
        assertEquals(ref, filterDefinition.ref());
        assertEquals(operator, filterDefinition.operator());
        assertEquals(value, filterDefinition.value());
    }

    @Test
    @DisplayName("Should create FilterDefinition with null value")
    void shouldCreateFilterDefinitionWithNullValue() {
        // Given
        DefinedPropertyReference ref = DefinedPropertyReference.USER_NAME;
        Op operator = Op.IS_NULL;
        Object value = null;

        // When
        FilterDefinition<DefinedPropertyReference> filterDefinition = new FilterDefinition<>(ref, operator, value);

        // Then
        assertEquals(ref, filterDefinition.ref());
        assertEquals(operator, filterDefinition.operator());
        assertNull(filterDefinition.value());
    }

    @Test
    @DisplayName("Should create FilterDefinition with different value types")
    void shouldCreateFilterDefinitionWithDifferentValueTypes() {
        // Test with String
        FilterDefinition<DefinedPropertyReference> stringFilter = new FilterDefinition<>(DefinedPropertyReference.USER_NAME, Op.MATCHES, "pattern%");
        assertEquals("pattern%", stringFilter.value());

        // Test with Integer
        FilterDefinition<DefinedPropertyReference> intFilter = new FilterDefinition<>(DefinedPropertyReference.USER_AGE, Op.GT, 42);
        assertEquals(42, intFilter.value());

        // Test with Boolean
        FilterDefinition<DefinedPropertyReference> booleanFilter = new FilterDefinition<>(DefinedPropertyReference.USER_STATUS, Op.EQ, true);
        assertEquals(true, booleanFilter.value());
    }

    @Test
    @DisplayName("Should handle toString method")
    void shouldHandleToStringMethod() {
        // Given
        FilterDefinition<DefinedPropertyReference> filterDefinition = new FilterDefinition<>(
            DefinedPropertyReference.USER_NAME,
            Op.EQ,
            "test"
        );

        // When
        String result = filterDefinition.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("FilterDefinition"));
    }
}

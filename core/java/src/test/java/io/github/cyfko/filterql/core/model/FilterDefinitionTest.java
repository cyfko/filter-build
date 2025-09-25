package io.github.cyfko.filterql.core.model;

import io.github.cyfko.filterql.core.validation.Operator;
import io.github.cyfko.filterql.core.validation.DefinedPropertyRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class FilterDefinitionTest {

    @Test
    @DisplayName("Should create FilterDefinition with valid parameters")
    void shouldCreateFilterDefinitionWithValidParameters() {
        // Given
        DefinedPropertyRef ref = DefinedPropertyRef.USER_NAME;
        Operator operator = Operator.EQUALS;
        Object value = "test";

        // When
        FilterDefinition<DefinedPropertyRef> filterDefinition = new FilterDefinition<>(ref, operator, value);

        // Then
        assertEquals(ref, filterDefinition.getRef());
        assertEquals(operator, filterDefinition.getOperator());
        assertEquals(value, filterDefinition.getValue());
    }

    @Test
    @DisplayName("Should create FilterDefinition with null value")
    void shouldCreateFilterDefinitionWithNullValue() {
        // Given
        DefinedPropertyRef ref = DefinedPropertyRef.USER_NAME;
        Operator operator = Operator.IS_NULL;
        Object value = null;

        // When
        FilterDefinition<DefinedPropertyRef> filterDefinition = new FilterDefinition<>(ref, operator, value);

        // Then
        assertEquals(ref, filterDefinition.getRef());
        assertEquals(operator, filterDefinition.getOperator());
        assertNull(filterDefinition.getValue());
    }

    @Test
    @DisplayName("Should create FilterDefinition with different value types")
    void shouldCreateFilterDefinitionWithDifferentValueTypes() {
        // Test with String
        FilterDefinition<DefinedPropertyRef> stringFilter = new FilterDefinition<>(DefinedPropertyRef.USER_NAME, Operator.LIKE, "pattern%");
        assertEquals("pattern%", stringFilter.getValue());

        // Test with Integer
        FilterDefinition<DefinedPropertyRef> intFilter = new FilterDefinition<>(DefinedPropertyRef.USER_AGE, Operator.GREATER_THAN, 42);
        assertEquals(42, intFilter.getValue());

        // Test with Boolean
        FilterDefinition<DefinedPropertyRef> booleanFilter = new FilterDefinition<>(DefinedPropertyRef.USER_STATUS, Operator.EQUALS, true);
        assertEquals(true, booleanFilter.getValue());
    }

    @Test
    @DisplayName("Should handle toString method")
    void shouldHandleToStringMethod() {
        // Given
        FilterDefinition<DefinedPropertyRef> filterDefinition = new FilterDefinition<>(
            DefinedPropertyRef.USER_NAME, 
            Operator.EQUALS, 
            "test"
        );

        // When
        String result = filterDefinition.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("FilterDefinition"));
    }
}

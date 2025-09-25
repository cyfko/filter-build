package io.github.cyfko.filterql.core.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class PropertyRefTest {

    // Tests pour BasePropertyRef.BASE
    @Test
    @DisplayName("Should test BasePropertyRef BASE properties")
    void shouldTestBasePropertyRefProperties() {
        // Given & When
        BasePropertyRef baseRef = BasePropertyRef.BASE;

        // Then
        assertEquals(Object.class, baseRef.getType());
        assertNotNull(baseRef.getSupportedOperators());
        assertTrue(baseRef.getSupportedOperators().isEmpty());
    }

    @Test
    @DisplayName("Should test BasePropertyRef enum behavior")
    void shouldTestBasePropertyRefEnumBehavior() {
        // When
        BasePropertyRef base = BasePropertyRef.valueOf("BASE");
        BasePropertyRef[] values = BasePropertyRef.values();

        // Then
        assertEquals(BasePropertyRef.BASE, base);
        assertEquals(1, values.length);
        assertEquals("BASE", BasePropertyRef.BASE.toString());
    }

    @Test
    @DisplayName("Should throw exception for unsupported operators on BASE")
    void shouldThrowExceptionForUnsupportedOperatorsOnBase() {
        // Given
        BasePropertyRef baseRef = BasePropertyRef.BASE;

        // When & Then - BASE supports no operators
        assertThrows(IllegalArgumentException.class, () -> baseRef.validateOperator(Operator.EQUALS));
        assertThrows(IllegalArgumentException.class, () -> baseRef.validateOperator(Operator.LIKE));
        assertThrows(NullPointerException.class, () -> baseRef.validateOperator(null));
    }

    // Tests pour DefinedPropertyRef
    @Test
    @DisplayName("Should test DefinedPropertyRef values and properties")
    void shouldTestDefinedPropertyRefValues() {
        // When & Then
        assertEquals(String.class, DefinedPropertyRef.USER_NAME.getType());
        assertEquals(Integer.class, DefinedPropertyRef.USER_AGE.getType());
        assertEquals(String.class, DefinedPropertyRef.USER_EMAIL.getType());
        assertEquals(String.class, DefinedPropertyRef.USER_STATUS.getType());

        // Test enum behavior
        assertEquals(4, DefinedPropertyRef.values().length);
        assertEquals(DefinedPropertyRef.USER_NAME, DefinedPropertyRef.valueOf("USER_NAME"));
    }

    @Test
    @DisplayName("Should validate operators correctly for defined properties")
    void shouldValidateOperatorsForDefinedProperties() {
        // Test USER_NAME supports string operators
        assertDoesNotThrow(() -> DefinedPropertyRef.USER_NAME.validateOperator(Operator.EQUALS));
        assertDoesNotThrow(() -> DefinedPropertyRef.USER_NAME.validateOperator(Operator.LIKE));
        assertDoesNotThrow(() -> DefinedPropertyRef.USER_NAME.validateOperator(Operator.IN));
        
        // Test USER_NAME does not support numeric operators
        assertThrows(IllegalArgumentException.class, 
                    () -> DefinedPropertyRef.USER_NAME.validateOperator(Operator.GREATER_THAN));

        // Test USER_AGE supports numeric operators
        assertDoesNotThrow(() -> DefinedPropertyRef.USER_AGE.validateOperator(Operator.EQUALS));
        assertDoesNotThrow(() -> DefinedPropertyRef.USER_AGE.validateOperator(Operator.GREATER_THAN));
        assertDoesNotThrow(() -> DefinedPropertyRef.USER_AGE.validateOperator(Operator.BETWEEN));
        
        // Test USER_AGE does not support string operators
        assertThrows(IllegalArgumentException.class, 
                    () -> DefinedPropertyRef.USER_AGE.validateOperator(Operator.LIKE));
    }

    @Test
    @DisplayName("Should return correct supported operators for defined properties")
    void shouldReturnCorrectSupportedOperatorsForDefinedProperties() {
        // Test USER_NAME operators
        Set<Operator> userNameOps = DefinedPropertyRef.USER_NAME.getSupportedOperators();
        assertTrue(userNameOps.contains(Operator.EQUALS));
        assertTrue(userNameOps.contains(Operator.LIKE));
        assertTrue(userNameOps.contains(Operator.IN));
        assertFalse(userNameOps.contains(Operator.GREATER_THAN));

        // Test USER_AGE operators
        Set<Operator> userAgeOps = DefinedPropertyRef.USER_AGE.getSupportedOperators();
        assertTrue(userAgeOps.contains(Operator.EQUALS));
        assertTrue(userAgeOps.contains(Operator.GREATER_THAN));
        assertTrue(userAgeOps.contains(Operator.LESS_THAN));
        assertTrue(userAgeOps.contains(Operator.BETWEEN));
        assertFalse(userAgeOps.contains(Operator.LIKE));

        // Test USER_STATUS operators
        Set<Operator> userStatusOps = DefinedPropertyRef.USER_STATUS.getSupportedOperators();
        assertTrue(userStatusOps.contains(Operator.EQUALS));
        assertTrue(userStatusOps.contains(Operator.NOT_EQUALS));
        assertTrue(userStatusOps.contains(Operator.IN));
        assertFalse(userStatusOps.contains(Operator.LIKE));
    }

    @Test
    @DisplayName("Should handle null operator validation for defined properties")
    void shouldHandleNullOperatorValidationForDefinedProperties() {
        // When & Then
        assertThrows(NullPointerException.class, 
                    () -> DefinedPropertyRef.USER_NAME.validateOperator(null));
        assertThrows(NullPointerException.class, 
                    () -> DefinedPropertyRef.USER_AGE.validateOperator(null));
    }

    @Test
    @DisplayName("Should ensure operator sets are immutable")
    void shouldEnsureOperatorSetsAreImmutable() {
        // Given
        Set<Operator> userNameOps = DefinedPropertyRef.USER_NAME.getSupportedOperators();
        Set<Operator> baseOps = BasePropertyRef.BASE.getSupportedOperators();

        // When & Then - Should not be able to modify the sets
        assertThrows(UnsupportedOperationException.class, 
                    () -> userNameOps.add(Operator.GREATER_THAN));
        assertThrows(UnsupportedOperationException.class, 
                    () -> baseOps.add(Operator.EQUALS));
    }

    @Test
    @DisplayName("Should test behavior of different property ref types")
    void shouldTestDifferentPropertyRefTypes() {
        // Given - Different property ref types
        BasePropertyRef baseProperty = BasePropertyRef.BASE;
        DefinedPropertyRef definedProperty = DefinedPropertyRef.USER_NAME;

        // When & Then
        assertEquals(Object.class, baseProperty.getType());
        assertEquals(String.class, definedProperty.getType());
        
        assertTrue(baseProperty.getSupportedOperators().isEmpty());
        assertFalse(definedProperty.getSupportedOperators().isEmpty());
    }

    @Test
    @DisplayName("Should test toString methods")
    void shouldTestToStringMethods() {
        // When
        String baseString = BasePropertyRef.BASE.toString();
        String definedString = DefinedPropertyRef.USER_NAME.toString();

        // Then
        assertEquals("BASE", baseString);
        assertEquals("USER_NAME", definedString);
    }

    @Test
    @DisplayName("Should test equality and hashCode")
    void shouldTestEqualityAndHashCode() {
        // Given
        BasePropertyRef base1 = BasePropertyRef.BASE;
        BasePropertyRef base2 = BasePropertyRef.BASE;
        DefinedPropertyRef defined1 = DefinedPropertyRef.USER_NAME;
        DefinedPropertyRef defined2 = DefinedPropertyRef.USER_NAME;
        DefinedPropertyRef defined3 = DefinedPropertyRef.USER_AGE;

        // When & Then
        assertEquals(base1, base2);
        assertEquals(base1.hashCode(), base2.hashCode());
        
        assertEquals(defined1, defined2);
        assertEquals(defined1.hashCode(), defined2.hashCode());
        
        assertNotEquals(defined1, defined3);
        // Note: BasePropertyRef and DefinedPropertyRef are different enum types
    }

    @Test
    @DisplayName("Should test supportsOperator method")
    void shouldTestSupportsOperatorMethod() {
        // When & Then
        assertFalse(BasePropertyRef.BASE.supportsOperator(Operator.EQUALS));
        assertFalse(BasePropertyRef.BASE.supportsOperator(Operator.LIKE));
        
        assertTrue(DefinedPropertyRef.USER_NAME.supportsOperator(Operator.EQUALS));
        assertTrue(DefinedPropertyRef.USER_NAME.supportsOperator(Operator.LIKE));
        assertFalse(DefinedPropertyRef.USER_NAME.supportsOperator(Operator.GREATER_THAN));
        
        assertTrue(DefinedPropertyRef.USER_AGE.supportsOperator(Operator.GREATER_THAN));
        assertFalse(DefinedPropertyRef.USER_AGE.supportsOperator(Operator.LIKE));
    }
}
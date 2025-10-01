package io.github.cyfko.filterql.core.validation;

import io.github.cyfko.filterql.core.exception.FilterValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class PropertyReferenceTest {

    // Tests pour BasePropertyRef.BASE
    @Test
    @DisplayName("Should test BasePropertyRef BASE properties")
    void shouldTestBasePropertyRefProperties() {
        // Given & When
        BasePropertyReference baseRef = BasePropertyReference.BASE;

        // Then
        assertEquals(Object.class, baseRef.getType());
        assertNotNull(baseRef.getSupportedOperators());
        assertTrue(baseRef.getSupportedOperators().isEmpty());
    }

    @Test
    @DisplayName("Should test BasePropertyRef enum behavior")
    void shouldTestBasePropertyRefEnumBehavior() {
        // When
        BasePropertyReference base = BasePropertyReference.valueOf("BASE");
        BasePropertyReference[] values = BasePropertyReference.values();

        // Then
        assertEquals(BasePropertyReference.BASE, base);
        assertEquals(1, values.length);
        assertEquals("BASE", BasePropertyReference.BASE.toString());
    }

    @Test
    @DisplayName("Should throw exception for unsupported operators on BASE")
    void shouldThrowExceptionForUnsupportedOperatorsOnBase() {
        // Given
        BasePropertyReference baseRef = BasePropertyReference.BASE;

        // When & Then - BASE supports no operators
        assertThrows(FilterValidationException.class, () -> baseRef.validateOperator(Op.EQUALS));
        assertThrows(FilterValidationException.class, () -> baseRef.validateOperator(Op.LIKE));
        assertThrows(NullPointerException.class, () -> baseRef.validateOperator(null));
    }

    // Tests pour DefinedPropertyRef
    @Test
    @DisplayName("Should test DefinedPropertyRef values and properties")
    void shouldTestDefinedPropertyRefValues() {
        // When & Then
        assertEquals(String.class, DefinedPropertyReference.USER_NAME.getType());
        assertEquals(Integer.class, DefinedPropertyReference.USER_AGE.getType());
        assertEquals(String.class, DefinedPropertyReference.USER_EMAIL.getType());
        assertEquals(String.class, DefinedPropertyReference.USER_STATUS.getType());

        // Test enum behavior
        assertEquals(4, DefinedPropertyReference.values().length);
        assertEquals(DefinedPropertyReference.USER_NAME, DefinedPropertyReference.valueOf("USER_NAME"));
    }

    @Test
    @DisplayName("Should validate operators correctly for defined properties")
    void shouldValidateOperatorsForDefinedProperties() {
        // Test USER_NAME supports string operators
        assertDoesNotThrow(() -> DefinedPropertyReference.USER_NAME.validateOperator(Op.EQUALS));
        assertDoesNotThrow(() -> DefinedPropertyReference.USER_NAME.validateOperator(Op.LIKE));
        assertDoesNotThrow(() -> DefinedPropertyReference.USER_NAME.validateOperator(Op.IN));
        
        // Test USER_NAME does not support numeric operators
        assertThrows(FilterValidationException.class,
                    () -> DefinedPropertyReference.USER_NAME.validateOperator(Op.GREATER_THAN));

        // Test USER_AGE supports numeric operators
        assertDoesNotThrow(() -> DefinedPropertyReference.USER_AGE.validateOperator(Op.EQUALS));
        assertDoesNotThrow(() -> DefinedPropertyReference.USER_AGE.validateOperator(Op.GREATER_THAN));
        assertDoesNotThrow(() -> DefinedPropertyReference.USER_AGE.validateOperator(Op.BETWEEN));
        
        // Test USER_AGE does not support string operators
        assertThrows(FilterValidationException.class,
                    () -> DefinedPropertyReference.USER_AGE.validateOperator(Op.LIKE));
    }

    @Test
    @DisplayName("Should return correct supported operators for defined properties")
    void shouldReturnCorrectSupportedOperatorsForDefinedProperties() {
        // Test USER_NAME operators
        Set<Op> userNameOps = DefinedPropertyReference.USER_NAME.getSupportedOperators();
        assertTrue(userNameOps.contains(Op.EQUALS));
        assertTrue(userNameOps.contains(Op.LIKE));
        assertTrue(userNameOps.contains(Op.IN));
        assertFalse(userNameOps.contains(Op.GREATER_THAN));

        // Test USER_AGE operators
        Set<Op> userAgeOps = DefinedPropertyReference.USER_AGE.getSupportedOperators();
        assertTrue(userAgeOps.contains(Op.EQUALS));
        assertTrue(userAgeOps.contains(Op.GREATER_THAN));
        assertTrue(userAgeOps.contains(Op.LESS_THAN));
        assertTrue(userAgeOps.contains(Op.BETWEEN));
        assertFalse(userAgeOps.contains(Op.LIKE));

        // Test USER_STATUS operators
        Set<Op> userStatusOps = DefinedPropertyReference.USER_STATUS.getSupportedOperators();
        assertTrue(userStatusOps.contains(Op.EQUALS));
        assertTrue(userStatusOps.contains(Op.NOT_EQUALS));
        assertTrue(userStatusOps.contains(Op.IN));
        assertFalse(userStatusOps.contains(Op.LIKE));
    }

    @Test
    @DisplayName("Should handle null operator validation for defined properties")
    void shouldHandleNullOperatorValidationForDefinedProperties() {
        // When & Then
        assertThrows(NullPointerException.class, 
                    () -> DefinedPropertyReference.USER_NAME.validateOperator(null));
        assertThrows(NullPointerException.class, 
                    () -> DefinedPropertyReference.USER_AGE.validateOperator(null));
    }

    @Test
    @DisplayName("Should ensure operator sets are immutable")
    void shouldEnsureOperatorSetsAreImmutable() {
        // Given
        Set<Op> userNameOps = DefinedPropertyReference.USER_NAME.getSupportedOperators();
        Set<Op> baseOps = BasePropertyReference.BASE.getSupportedOperators();

        // When & Then - Should not be able to modify the sets
        assertThrows(UnsupportedOperationException.class, 
                    () -> userNameOps.add(Op.GREATER_THAN));
        assertThrows(UnsupportedOperationException.class, 
                    () -> baseOps.add(Op.EQUALS));
    }

    @Test
    @DisplayName("Should test behavior of different property ref types")
    void shouldTestDifferentPropertyRefTypes() {
        // Given - Different property ref types
        BasePropertyReference baseProperty = BasePropertyReference.BASE;
        DefinedPropertyReference definedProperty = DefinedPropertyReference.USER_NAME;

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
        String baseString = BasePropertyReference.BASE.toString();
        String definedString = DefinedPropertyReference.USER_NAME.toString();

        // Then
        assertEquals("BASE", baseString);
        assertEquals("USER_NAME", definedString);
    }

    @Test
    @DisplayName("Should test equality and hashCode")
    void shouldTestEqualityAndHashCode() {
        // Given
        BasePropertyReference base1 = BasePropertyReference.BASE;
        BasePropertyReference base2 = BasePropertyReference.BASE;
        DefinedPropertyReference defined1 = DefinedPropertyReference.USER_NAME;
        DefinedPropertyReference defined2 = DefinedPropertyReference.USER_NAME;
        DefinedPropertyReference defined3 = DefinedPropertyReference.USER_AGE;

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
        assertFalse(BasePropertyReference.BASE.supportsOperator(Op.EQUALS));
        assertFalse(BasePropertyReference.BASE.supportsOperator(Op.LIKE));
        
        assertTrue(DefinedPropertyReference.USER_NAME.supportsOperator(Op.EQUALS));
        assertTrue(DefinedPropertyReference.USER_NAME.supportsOperator(Op.LIKE));
        assertFalse(DefinedPropertyReference.USER_NAME.supportsOperator(Op.GREATER_THAN));
        
        assertTrue(DefinedPropertyReference.USER_AGE.supportsOperator(Op.GREATER_THAN));
        assertFalse(DefinedPropertyReference.USER_AGE.supportsOperator(Op.LIKE));
    }
}
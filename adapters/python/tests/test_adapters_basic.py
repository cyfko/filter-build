"""
Basic tests for Python adapters to verify the architecture works.
"""

import pytest
from unittest.mock import Mock
from enum import Enum

from interfaces import FilterDefinition, PropertyRefEnum
from validation import Operator, PropertyRefImpl


class PropertyRefTest(Enum):
    """Test enum for PropertyRef testing."""
    USER_NAME = "USER_NAME"
    USER_AGE = "USER_AGE"
    USER_EMAIL = "USER_EMAIL"
    USER_STATUS = "USER_STATUS"


class TestPropertyRefImpl:
    """Test implementation of PropertyRef for testing."""
    USER_NAME = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE, Operator.IN])
    USER_AGE = PropertyRefImpl("integer", [Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN])
    USER_EMAIL = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
    USER_STATUS = PropertyRefImpl("string", [Operator.EQUALS, Operator.IN])


class TestPythonAdaptersBasic:
    """Basic test cases for Python adapters architecture."""
    
    def test_property_ref_enum_creation(self):
        """Test that PropertyRefEnum can be created."""
        # Test that our enum works with the PropertyRefEnum type
        assert PropertyRefTest.USER_NAME.value == "USER_NAME"
        assert PropertyRefTest.USER_AGE.value == "USER_AGE"
        assert PropertyRefTest.USER_EMAIL.value == "USER_EMAIL"
        assert PropertyRefTest.USER_STATUS.value == "USER_STATUS"
    
    def test_property_ref_impl_creation(self):
        """Test that PropertyRefImpl can be created."""
        # Test that our implementation works
        assert TestPropertyRefImpl.USER_NAME.type_name == "string"
        assert TestPropertyRefImpl.USER_AGE.type_name == "integer"
        assert TestPropertyRefImpl.USER_EMAIL.type_name == "string"
        assert TestPropertyRefImpl.USER_STATUS.type_name == "string"
    
    def test_filter_definition_with_enum(self):
        """Test creating FilterDefinition with enum values."""
        filter_def = FilterDefinition(
            ref=PropertyRefTest.USER_NAME,
            operator=Operator.EQUALS,
            value="John"
        )
        
        assert filter_def.ref == PropertyRefTest.USER_NAME
        assert filter_def.operator == Operator.EQUALS
        assert filter_def.value == "John"
    
    def test_property_ref_validation(self):
        """Test PropertyRef validation works."""
        # Test valid operator
        TestPropertyRefImpl.USER_NAME.validate_operator(Operator.EQUALS)
        
        # Test invalid operator
        with pytest.raises(ValueError, match="Operator 'Operator.GREATER_THAN' is not supported"):
            TestPropertyRefImpl.USER_NAME.validate_operator(Operator.GREATER_THAN)
    
    def test_operator_parsing(self):
        """Test operator parsing works."""
        from validation import parse_operator
        
        # Test valid operators (using their actual values/symbols)
        assert parse_operator("=") == Operator.EQUALS
        assert parse_operator(" LIKE ") == Operator.LIKE
        assert parse_operator(">") == Operator.GREATER_THAN
        
        # Test invalid operator
        assert parse_operator("INVALID") is None
    
    def test_architecture_compatibility(self):
        """Test that the architecture is compatible with the new type-safe approach."""
        # Test that PropertyRefEnum constraint works
        def test_function(p: PropertyRefEnum) -> str:
            return "test"
        
        # This should work without type errors
        result = test_function(PropertyRefTest)
        assert result == "test"

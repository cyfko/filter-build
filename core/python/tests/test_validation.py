"""
Unit tests for validation module.
"""

import pytest
from enum import Enum
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from validation import (
    Operator, 
    PropertyRef, 
    PropertyRefImpl,
    parse_operator,
    operator_requires_value,
    operator_supports_multiple_values
)


class TestOperator:
    """Test cases for Operator enum."""
    
    def test_operator_values(self):
        """Test that all operators have correct string values."""
        assert Operator.EQUALS.value == "="
        assert Operator.NOT_EQUALS.value == "!="
        assert Operator.GREATER_THAN.value == ">"
        assert Operator.GREATER_THAN_OR_EQUAL.value == ">="
        assert Operator.LESS_THAN.value == "<"
        assert Operator.LESS_THAN_OR_EQUAL.value == "<="
        assert Operator.LIKE.value == "LIKE"
        assert Operator.NOT_LIKE.value == "NOT LIKE"
        assert Operator.IN.value == "IN"
        assert Operator.NOT_IN.value == "NOT IN"
        assert Operator.IS_NULL.value == "IS NULL"
        assert Operator.IS_NOT_NULL.value == "IS NOT NULL"
        assert Operator.BETWEEN.value == "BETWEEN"
        assert Operator.NOT_BETWEEN.value == "NOT BETWEEN"
    
    def test_operator_enumeration(self):
        """Test that all operators can be enumerated."""
        operators = list(Operator)
        assert len(operators) == 14
        assert Operator.EQUALS in operators
        assert Operator.LIKE in operators
        assert Operator.IS_NULL in operators


class TestPropertyRefImpl:
    """Test cases for PropertyRefImpl class."""
    
    def test_property_ref_creation(self):
        """Test creating a PropertyRefImpl instance."""
        ref = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
        
        assert ref.type_name == "string"
        assert ref.supported_operators == [Operator.EQUALS, Operator.LIKE]
    
    def test_supports_operator(self):
        """Test operator support checking."""
        ref = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
        
        assert ref.supports_operator(Operator.EQUALS) is True
        assert ref.supports_operator(Operator.LIKE) is True
        assert ref.supports_operator(Operator.GREATER_THAN) is False
    
    def test_validate_operator_success(self):
        """Test successful operator validation."""
        ref = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
        
        # Should not raise exception
        ref.validate_operator(Operator.EQUALS)
        ref.validate_operator(Operator.LIKE)
    
    def test_validate_operator_failure(self):
        """Test operator validation failure."""
        ref = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
        
        with pytest.raises(ValueError, match="Operator 'Operator.GREATER_THAN' is not supported"):
            ref.validate_operator(Operator.GREATER_THAN)
    
    def test_get_description(self):
        """Test getting property description."""
        ref = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
        
        description = ref.get_description()
        assert description == "PropertyRef(string)"
    
    def test_str_representation(self):
        """Test string representation."""
        ref = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
        
        str_repr = str(ref)
        assert "PropertyRef" in str_repr
        assert "string" in str_repr
        assert "EQUALS" in str_repr
        assert "LIKE" in str_repr


class TestParseOperator:
    """Test cases for parse_operator function."""
    
    def test_parse_operator_exact_match(self):
        """Test parsing operators with exact matches."""
        assert parse_operator("=") == Operator.EQUALS
        assert parse_operator("!=") == Operator.NOT_EQUALS
        assert parse_operator(">") == Operator.GREATER_THAN
        assert parse_operator("LIKE") == Operator.LIKE
        assert parse_operator("IS NULL") == Operator.IS_NULL
    
    def test_parse_operator_whitespace(self):
        """Test parsing operators with whitespace."""
        assert parse_operator(" = ") == Operator.EQUALS
        assert parse_operator(" LIKE ") == Operator.LIKE
        assert parse_operator(" IS NULL ") == Operator.IS_NULL
    
    def test_parse_operator_case_insensitive(self):
        """Test parsing operators with different cases."""
        assert parse_operator("like") == Operator.LIKE
        assert parse_operator("Like") == Operator.LIKE
        assert parse_operator("LIKE") == Operator.LIKE
    
    def test_parse_operator_invalid(self):
        """Test parsing invalid operators."""
        assert parse_operator("INVALID") is None
        assert parse_operator("") is None
        assert parse_operator(None) is None
    
    def test_parse_operator_none_empty(self):
        """Test parsing None and empty strings."""
        assert parse_operator("") is None
        assert parse_operator("   ") is None


class TestOperatorUtilityFunctions:
    """Test cases for operator utility functions."""
    
    def test_operator_requires_value(self):
        """Test checking if operators require values."""
        # Operators that require values
        assert operator_requires_value(Operator.EQUALS) is True
        assert operator_requires_value(Operator.LIKE) is True
        assert operator_requires_value(Operator.GREATER_THAN) is True
        assert operator_requires_value(Operator.IN) is True
        assert operator_requires_value(Operator.BETWEEN) is True
        
        # Operators that don't require values
        assert operator_requires_value(Operator.IS_NULL) is False
        assert operator_requires_value(Operator.IS_NOT_NULL) is False
    
    def test_operator_supports_multiple_values(self):
        """Test checking if operators support multiple values."""
        # Operators that support multiple values
        assert operator_supports_multiple_values(Operator.IN) is True
        assert operator_supports_multiple_values(Operator.NOT_IN) is True
        assert operator_supports_multiple_values(Operator.BETWEEN) is True
        assert operator_supports_multiple_values(Operator.NOT_BETWEEN) is True
        
        # Operators that don't support multiple values
        assert operator_supports_multiple_values(Operator.EQUALS) is False
        assert operator_supports_multiple_values(Operator.LIKE) is False
        assert operator_supports_multiple_values(Operator.GREATER_THAN) is False
        assert operator_supports_multiple_values(Operator.IS_NULL) is False


class TestPropertyRefProtocol:
    """Test cases for PropertyRef protocol compliance."""
    
    def test_property_ref_impl_protocol_compliance(self):
        """Test that PropertyRefImpl implements PropertyRef protocol."""
        ref = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
        
        # Check that it has required properties
        assert hasattr(ref, 'type_name')
        assert hasattr(ref, 'supported_operators')
        
        # Check property types
        assert isinstance(ref.type_name, str)
        assert isinstance(ref.supported_operators, list)
        
        # Check that it can be used as PropertyRef
        def use_property_ref(prop_ref: PropertyRef) -> str:
            return f"{prop_ref.type_name}: {prop_ref.supported_operators}"
        
        result = use_property_ref(ref)
        assert "string" in result
        assert "EQUALS" in result

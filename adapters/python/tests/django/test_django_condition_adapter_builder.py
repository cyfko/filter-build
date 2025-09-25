"""
Tests for Django Condition Adapter Builder.
"""

import pytest
from unittest.mock import Mock
from enum import Enum

from interfaces import PropertyRefEnum
from validation import Operator
from django_condition_adapter_builder import DjangoConditionAdapterBuilder
from django_condition_adapter import DjangoConditionAdapter


class TestPropertyRef(Enum):
    """Test enum for PropertyRef testing."""
    USER_NAME = "USER_NAME"
    USER_AGE = "USER_AGE"
    USER_EMAIL = "USER_EMAIL"
    USER_STATUS = "USER_STATUS"


class MockDjangoConditionAdapterBuilder(DjangoConditionAdapterBuilder[str, TestPropertyRef]):
    """Mock implementation of DjangoConditionAdapterBuilder for testing."""
    
    def build(self, ref: str, op: Operator, value: any) -> DjangoConditionAdapter[str]:
        """Builds a mock Django condition adapter."""
        mock_condition = Mock(spec=DjangoConditionAdapter)
        mock_condition.ref = ref
        mock_condition.operator = op
        mock_condition.value = value
        return mock_condition


class TestDjangoConditionAdapterBuilder:
    """Test cases for DjangoConditionAdapterBuilder class."""
    
    def test_builder_creation(self):
        """Test creating a DjangoConditionAdapterBuilder instance."""
        builder = MockDjangoConditionAdapterBuilder()
        assert builder is not None
    
    def test_build_method_signature(self):
        """Test that the build method has the correct signature."""
        builder = MockDjangoConditionAdapterBuilder()
        
        # Test that build method accepts the correct parameters
        result = builder.build("USER_NAME", Operator.EQUALS, "John")
        
        assert result is not None
        assert hasattr(result, 'ref')
        assert hasattr(result, 'operator')
        assert hasattr(result, 'value')
        assert result.ref == "USER_NAME"
        assert result.operator == Operator.EQUALS
        assert result.value == "John"
    
    def test_build_with_different_operators(self):
        """Test building conditions with different operators."""
        builder = MockDjangoConditionAdapterBuilder()
        
        # Test EQUALS operator
        result1 = builder.build("USER_NAME", Operator.EQUALS, "John")
        assert result1.operator == Operator.EQUALS
        
        # Test LIKE operator
        result2 = builder.build("USER_NAME", Operator.LIKE, "John%")
        assert result2.operator == Operator.LIKE
        
        # Test IN operator
        result3 = builder.build("USER_STATUS", Operator.IN, ["ACTIVE", "PENDING"])
        assert result3.operator == Operator.IN
    
    def test_build_with_different_value_types(self):
        """Test building conditions with different value types."""
        builder = MockDjangoConditionAdapterBuilder()
        
        # String value
        result1 = builder.build("USER_NAME", Operator.EQUALS, "John")
        assert result1.value == "John"
        
        # Integer value
        result2 = builder.build("USER_AGE", Operator.GREATER_THAN, 25)
        assert result2.value == 25
        
        # List value
        result3 = builder.build("USER_STATUS", Operator.IN, ["ACTIVE", "PENDING"])
        assert result3.value == ["ACTIVE", "PENDING"]
    
    def test_build_with_different_property_refs(self):
        """Test building conditions with different property references."""
        builder = MockDjangoConditionAdapterBuilder()
        
        # Test different property refs
        property_refs = ["USER_NAME", "USER_AGE", "USER_EMAIL", "USER_STATUS"]
        
        for ref in property_refs:
            result = builder.build(ref, Operator.EQUALS, "test")
            assert result.ref == ref
    
    def test_builder_is_abstract(self):
        """Test that DjangoConditionAdapterBuilder is abstract."""
        with pytest.raises(TypeError):
            DjangoConditionAdapterBuilder()
    
    def test_builder_generic_types(self):
        """Test that the builder works with different generic types."""
        # Test with string type
        builder_str = MockDjangoConditionAdapterBuilder()
        result_str = builder_str.build("USER_NAME", Operator.EQUALS, "John")
        assert isinstance(result_str, DjangoConditionAdapter)
        
        # Test that the builder is properly typed
        assert hasattr(builder_str, 'build')

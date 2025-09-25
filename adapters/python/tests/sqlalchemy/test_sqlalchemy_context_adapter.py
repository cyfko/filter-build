"""
Tests for SQLAlchemy Context Adapter.
"""

import pytest
from unittest.mock import Mock, MagicMock
from enum import Enum

from interfaces import FilterDefinition, PropertyRefEnum
from validation import Operator, PropertyRefImpl
from sqlalchemy_context_adapter import SQLAlchemyContextAdapter
from sqlalchemy_condition_adapter_builder import SQLAlchemyConditionAdapterBuilder
from sqlalchemy_condition_adapter import SQLAlchemyConditionAdapter


class TestPropertyRef(Enum):
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


class MockSQLAlchemyConditionAdapterBuilder(SQLAlchemyConditionAdapterBuilder[str, TestPropertyRef]):
    """Mock implementation of SQLAlchemyConditionAdapterBuilder for testing."""
    
    def build(self, ref: str, op: Operator, value: any) -> SQLAlchemyConditionAdapter[str]:
        """Builds a mock SQLAlchemy condition adapter."""
        mock_condition = Mock(spec=SQLAlchemyConditionAdapter)
        mock_condition.ref = ref
        mock_condition.operator = op
        mock_condition.value = value
        return mock_condition


class TestSQLAlchemyContextAdapter:
    """Test cases for SQLAlchemyContextAdapter class."""
    
    def test_context_adapter_creation(self):
        """Test creating a SQLAlchemyContextAdapter instance."""
        builder = MockSQLAlchemyConditionAdapterBuilder()
        adapter = SQLAlchemyContextAdapter(builder, TestPropertyRefImpl)
        
        assert adapter.filters == {}
        assert adapter.condition_adapter_builder == builder
        assert adapter.property_ref_impl == TestPropertyRefImpl
    
    def test_add_condition_success(self):
        """Test adding a condition successfully."""
        builder = MockSQLAlchemyConditionAdapterBuilder()
        adapter = SQLAlchemyContextAdapter(builder, TestPropertyRefImpl)
        
        filter_def = FilterDefinition(
            ref=TestPropertyRef.USER_NAME,
            operator=Operator.EQUALS,
            value="John"
        )
        
        adapter.add_condition("nameFilter", filter_def)
        
        assert "nameFilter" in adapter.filters
        condition = adapter.filters["nameFilter"]
        assert condition.ref == TestPropertyRef.USER_NAME
        assert condition.operator == Operator.EQUALS
        assert condition.value == "John"
    
    def test_add_condition_invalid_property_ref(self):
        """Test adding a condition with invalid property reference."""
        builder = MockSQLAlchemyConditionAdapterBuilder()
        adapter = SQLAlchemyContextAdapter(builder, TestPropertyRefImpl)
        
        # Create a mock property ref implementation that doesn't have the property
        mock_property_ref_impl = Mock()
        mock_property_ref_impl.INVALID_PROPERTY = None
        
        adapter.property_ref_impl = mock_property_ref_impl
        
        filter_def = FilterDefinition(
            ref="INVALID_PROPERTY",
            operator=Operator.EQUALS,
            value="test"
        )
        
        with pytest.raises(ValueError, match="Invalid PropertyRef: INVALID_PROPERTY"):
            adapter.add_condition("invalidFilter", filter_def)
    
    def test_add_condition_unsupported_operator(self):
        """Test adding a condition with unsupported operator."""
        builder = MockSQLAlchemyConditionAdapterBuilder()
        adapter = SQLAlchemyContextAdapter(builder, TestPropertyRefImpl)
        
        filter_def = FilterDefinition(
            ref=TestPropertyRef.USER_NAME,
            operator=Operator.GREATER_THAN,  # Not supported for USER_NAME
            value="John"
        )
        
        with pytest.raises(ValueError, match="Operator 'GREATER_THAN' is not supported"):
            adapter.add_condition("invalidFilter", filter_def)
    
    def test_get_condition_existing(self):
        """Test getting an existing condition."""
        builder = MockSQLAlchemyConditionAdapterBuilder()
        adapter = SQLAlchemyContextAdapter(builder, TestPropertyRefImpl)
        
        filter_def = FilterDefinition(
            ref=TestPropertyRef.USER_NAME,
            operator=Operator.EQUALS,
            value="John"
        )
        
        adapter.add_condition("nameFilter", filter_def)
        condition = adapter.get_condition("nameFilter")
        
        assert condition is not None
        assert condition.ref == TestPropertyRef.USER_NAME
    
    def test_get_condition_nonexistent(self):
        """Test getting a non-existent condition."""
        builder = MockSQLAlchemyConditionAdapterBuilder()
        adapter = SQLAlchemyContextAdapter(builder, TestPropertyRefImpl)
        
        condition = adapter.get_condition("nonexistentFilter")
        assert condition is None
    
    def test_multiple_conditions(self):
        """Test adding multiple conditions."""
        builder = MockSQLAlchemyConditionAdapterBuilder()
        adapter = SQLAlchemyContextAdapter(builder, TestPropertyRefImpl)
        
        # Add first condition
        filter_def1 = FilterDefinition(
            ref=TestPropertyRef.USER_NAME,
            operator=Operator.EQUALS,
            value="John"
        )
        adapter.add_condition("nameFilter", filter_def1)
        
        # Add second condition
        filter_def2 = FilterDefinition(
            ref=TestPropertyRef.USER_AGE,
            operator=Operator.GREATER_THAN,
            value=18
        )
        adapter.add_condition("ageFilter", filter_def2)
        
        assert len(adapter.filters) == 2
        assert "nameFilter" in adapter.filters
        assert "ageFilter" in adapter.filters
        
        # Verify conditions
        name_condition = adapter.get_condition("nameFilter")
        age_condition = adapter.get_condition("ageFilter")
        
        assert name_condition.ref == TestPropertyRef.USER_NAME
        assert age_condition.ref == TestPropertyRef.USER_AGE

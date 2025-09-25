"""
Unit tests for interfaces module.
"""

import pytest
from enum import Enum
from typing import Dict, List, Optional, Any
from dataclasses import dataclass

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from interfaces import (
    FilterDefinition,
    FilterRequest,
    Condition,
    Context,
    ContextAdapter,
    ConditionAdapterBuilder,
    FilterTree,
    Parser,
    FilterExecutor,
    DSLSyntaxException,
    FilterValidationException,
    PropertyRefEnum
)
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
    USER_AGE = PropertyRefImpl("int", [Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN, Operator.BETWEEN])
    USER_EMAIL = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
    USER_STATUS = PropertyRefImpl("string", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN])


class TestFilterDefinition:
    """Test cases for FilterDefinition class."""
    
    def test_filter_definition_creation(self):
        """Test creating a FilterDefinition instance."""
        filter_def = FilterDefinition(
            ref=PropertyRefTest.USER_NAME,
            operator=Operator.EQUALS,
            value="John"
        )
        
        assert filter_def.ref == PropertyRefTest.USER_NAME
        assert filter_def.operator == Operator.EQUALS
        assert filter_def.value == "John"
    
    def test_filter_definition_with_different_types(self):
        """Test FilterDefinition with different value types."""
        # String value
        string_filter = FilterDefinition(
            ref=PropertyRefTest.USER_NAME,
            operator=Operator.LIKE,
            value="John%"
        )
        assert string_filter.value == "John%"
        
        # Integer value
        int_filter = FilterDefinition(
            ref=PropertyRefTest.USER_AGE,
            operator=Operator.GREATER_THAN,
            value=18
        )
        assert int_filter.value == 18
        
        # List value
        list_filter = FilterDefinition(
            ref=PropertyRefTest.USER_STATUS,
            operator=Operator.IN,
            value=["ACTIVE", "PENDING"]
        )
        assert list_filter.value == ["ACTIVE", "PENDING"]
    
    def test_filter_definition_equality(self):
        """Test FilterDefinition equality."""
        filter1 = FilterDefinition(
            ref=PropertyRefTest.USER_NAME,
            operator=Operator.EQUALS,
            value="John"
        )
        filter2 = FilterDefinition(
            ref=PropertyRefTest.USER_NAME,
            operator=Operator.EQUALS,
            value="John"
        )
        filter3 = FilterDefinition(
            ref=PropertyRefTest.USER_NAME,
            operator=Operator.LIKE,
            value="John"
        )
        
        assert filter1 == filter2
        assert filter1 != filter3


class TestFilterRequest:
    """Test cases for FilterRequest class."""
    
    def test_filter_request_creation(self):
        """Test creating a FilterRequest instance."""
        filters = {
            "nameFilter": FilterDefinition(
                ref=PropertyRefTest.USER_NAME,
                operator=Operator.LIKE,
                value="John%"
            ),
            "ageFilter": FilterDefinition(
                ref=PropertyRefTest.USER_AGE,
                operator=Operator.GREATER_THAN,
                value=18
            )
        }
        
        request = FilterRequest(
            filters=filters,
            combine_with="AND"
        )
        
        assert len(request.filters) == 2
        assert "nameFilter" in request.filters
        assert "ageFilter" in request.filters
        assert request.combine_with == "AND"
    
    def test_filter_request_empty_filters(self):
        """Test FilterRequest with empty filters."""
        request = FilterRequest(
            filters={},
            combine_with="OR"
        )
        
        assert request.filters == {}
        assert request.combine_with == "OR"


class TestCondition:
    """Test cases for Condition interface."""
    
    def test_condition_interface_methods(self):
        """Test that Condition interface has required methods."""
        # Create a mock implementation
        class MockCondition(Condition):
            def and_(self, other: Condition) -> Condition:
                return self
            
            def or_(self, other: Condition) -> Condition:
                return self
            
            def not_(self) -> Condition:
                return self
        
        condition = MockCondition()
        
        # Test that methods exist and are callable
        assert hasattr(condition, 'and_')
        assert hasattr(condition, 'or_')
        assert hasattr(condition, 'not_')
        assert callable(condition.and_)
        assert callable(condition.or_)
        assert callable(condition.not_)


class TestContext:
    """Test cases for Context interface."""
    
    def test_context_interface_methods(self):
        """Test that Context interface has required methods."""
        # Create a mock implementation
        class MockContext(Context):
            def get_condition(self, filter_key: str) -> Optional[Condition]:
                return None
        
        context = MockContext()
        
        # Test that methods exist and are callable
        assert hasattr(context, 'get_condition')
        assert callable(context.get_condition)


class TestContextAdapter:
    """Test cases for ContextAdapter interface."""
    
    def test_context_adapter_interface_methods(self):
        """Test that ContextAdapter interface has required methods."""
        # Create a mock implementation
        class MockContextAdapter(ContextAdapter[Any, PropertyRefTest]):
            def add_condition(self, filter_key: str, definition: FilterDefinition[PropertyRefTest]) -> None:
                pass
            
            def get_condition(self, filter_key: str) -> Optional[Condition]:
                return None
        
        adapter = MockContextAdapter()
        
        # Test that methods exist and are callable
        assert hasattr(adapter, 'add_condition')
        assert hasattr(adapter, 'get_condition')
        assert callable(adapter.add_condition)
        assert callable(adapter.get_condition)


class TestConditionAdapterBuilder:
    """Test cases for ConditionAdapterBuilder interface."""
    
    def test_condition_adapter_builder_interface_methods(self):
        """Test that ConditionAdapterBuilder interface has required methods."""
        # Create a mock implementation
        class MockConditionAdapterBuilder(ConditionAdapterBuilder[Any, PropertyRefTest]):
            def build(self, ref: PropertyRefTest, operator: Operator, value: Any) -> Condition:
                return MockCondition()
        
        class MockCondition(Condition):
            def and_(self, other: Condition) -> Condition:
                return self
            
            def or_(self, other: Condition) -> Condition:
                return self
            
            def not_(self) -> Condition:
                return self
        
        builder = MockConditionAdapterBuilder()
        
        # Test that methods exist and are callable
        assert hasattr(builder, 'build')
        assert callable(builder.build)


class TestFilterTree:
    """Test cases for FilterTree interface."""
    
    def test_filter_tree_interface_methods(self):
        """Test that FilterTree interface has required methods."""
        # Create a mock implementation
        class MockFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                return MockCondition()
        
        class MockCondition(Condition):
            def and_(self, other: Condition) -> Condition:
                return self
            
            def or_(self, other: Condition) -> Condition:
                return self
            
            def not_(self) -> Condition:
                return self
        
        tree = MockFilterTree()
        
        # Test that methods exist and are callable
        assert hasattr(tree, 'generate')
        assert callable(tree.generate)


class TestParser:
    """Test cases for Parser interface."""
    
    def test_parser_interface_methods(self):
        """Test that Parser interface has required methods."""
        # Create a mock implementation
        class MockParser(Parser):
            def parse(self, dsl_expression: str) -> FilterTree:
                return MockFilterTree()
        
        class MockFilterTree(FilterTree):
            def generate(self, context: Context) -> Condition:
                return MockCondition()
        
        class MockCondition(Condition):
            def and_(self, other: Condition) -> Condition:
                return self
            
            def or_(self, other: Condition) -> Condition:
                return self
            
            def not_(self) -> Condition:
                return self
        
        parser = MockParser()
        
        # Test that methods exist and are callable
        assert hasattr(parser, 'parse')
        assert callable(parser.parse)


class TestFilterExecutor:
    """Test cases for FilterExecutor interface."""
    
    def test_filter_executor_interface_methods(self):
        """Test that FilterExecutor interface has required methods."""
        # Create a mock implementation
        class MockFilterExecutor(FilterExecutor[Any]):
            def execute(self, global_condition: Condition, entity_class: type) -> List[Any]:
                return []
        
        executor = MockFilterExecutor()
        
        # Test that methods exist and are callable
        assert hasattr(executor, 'execute')
        assert callable(executor.execute)


class TestExceptions:
    """Test cases for custom exceptions."""
    
    def test_dsl_syntax_exception(self):
        """Test DSLSyntaxException creation and properties."""
        exception = DSLSyntaxException("Invalid syntax")
        
        assert str(exception) == "Invalid syntax"
        assert isinstance(exception, Exception)
    
    def test_filter_validation_exception(self):
        """Test FilterValidationException creation and properties."""
        exception = FilterValidationException("Invalid filter")
        
        assert str(exception) == "Invalid filter"
        assert isinstance(exception, Exception)
    
    def test_exceptions_with_cause(self):
        """Test exceptions with cause."""
        cause = ValueError("Root cause")
        
        dsl_exception = DSLSyntaxException("DSL error")
        filter_exception = FilterValidationException("Filter error")
        
        assert str(dsl_exception) == "DSL error"
        assert str(filter_exception) == "Filter error"
        assert isinstance(dsl_exception, Exception)
        assert isinstance(filter_exception, Exception)


class TestPropertyRefEnum:
    """Test cases for PropertyRefEnum."""
    
    def test_property_ref_enum_inheritance(self):
        """Test that PropertyRefEnum can be inherited."""
        class TestEnum(PropertyRefEnum):
            VALUE1 = "VALUE1"
            VALUE2 = "VALUE2"
        
        assert TestEnum.VALUE1.value == "VALUE1"
        assert TestEnum.VALUE2.value == "VALUE2"
        assert issubclass(TestEnum, PropertyRefEnum)

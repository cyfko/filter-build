"""
SQLAlchemy Context Adapter implementing the correct pattern.
This adapter uses SQLAlchemyConditionAdapterBuilder to create conditions.
"""

from typing import Dict, Optional, TypeVar, Generic, Any
from enum import Enum
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', '..', '..', 'core', 'python', 'src'))
from interfaces import Condition, ContextAdapter, FilterDefinition, PropertyRefEnum
from validation import Operator, PropertyRefUtils
from .sqlalchemy-condition-adapter import SQLAlchemyConditionAdapter
from .sqlalchemy-condition-adapter-builder import SQLAlchemyConditionAdapterBuilder

T = TypeVar('T')
P = TypeVar('P', bound=PropertyRefEnum)


class SQLAlchemyContextAdapter(ContextAdapter[T, P]):
    """SQLAlchemy implementation of the ContextAdapter interface using the correct pattern."""
    
    def __init__(self, condition_adapter_builder: SQLAlchemyConditionAdapterBuilder[T, P], property_ref_impl: Any):
        self.filters: Dict[str, SQLAlchemyConditionAdapter[T]] = {}
        self.condition_adapter_builder = condition_adapter_builder
        self.property_ref_impl = property_ref_impl
    
    def add_condition(self, filter_key: str, definition: FilterDefinition[P]) -> None:
        """Adds a condition using the builder pattern."""
        # Get PropertyRef and Operator directly (type-safe, no resolution needed)
        property_ref_key = definition.ref
        operator = definition.operator
        
        # Get the PropertyRef implementation from the enum
        property_ref = getattr(self.property_ref_impl, property_ref_key, None)
        if not property_ref:
            raise ValueError(f"Invalid PropertyRef: {property_ref_key}")
        
        # Validate that the property supports this operator using PropertyRefUtils
        PropertyRefUtils.validate_operator(property_ref, operator)
        
        # Build condition using the builder and store it
        condition = self.condition_adapter_builder.build(property_ref_key, operator, definition.value)
        self.filters[filter_key] = condition
    
    def get_condition(self, filter_key: str) -> Optional[Condition]:
        """Retrieves the condition associated with the given filter key."""
        return self.filters.get(filter_key)


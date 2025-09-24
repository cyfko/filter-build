"""
Django Context Adapter implementing the correct pattern.
This adapter uses DjangoConditionAdapterBuilder to create conditions.
"""

from typing import Dict, Optional, TypeVar, Generic
from dynamic_filter_core import Condition, Context, FilterDefinition, Operator, PropertyRef
from .django-condition-adapter import DjangoConditionAdapter
from .django-condition-adapter-builder import DjangoConditionAdapterBuilder

T = TypeVar('T')
P = TypeVar('P', bound=PropertyRef)


class DjangoContextAdapter(Context, Generic[T, P]):
    """Django implementation of the Context interface using the correct pattern."""
    
    def __init__(self, condition_adapter_builder: DjangoConditionAdapterBuilder[T, P]):
        self.filters: Dict[str, DjangoConditionAdapter[T]] = {}
        self.condition_adapter_builder = condition_adapter_builder
    
    def add_condition(self, filter_key: str, definition: FilterDefinition[P]) -> None:
        """Adds a condition using the builder pattern."""
        # Get PropertyRef and Operator directly (type-safe, no resolution needed)
        property_ref = definition.ref
        operator = definition.operator
        
        # Validate that the property supports this operator
        property_ref.validate_operator(operator)
        
        # Build condition using the builder and store it
        condition = self.condition_adapter_builder.build(property_ref, operator, definition.value)
        self.filters[filter_key] = condition
    
    def get_condition(self, filter_key: str) -> Optional[Condition]:
        """Retrieves the condition associated with the given filter key."""
        condition = self.filters.get(filter_key)
        if not condition:
            raise ValueError(f"No condition found for key: {filter_key}")
        return condition
    

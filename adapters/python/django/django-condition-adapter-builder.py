"""
Builder interface for creating Django condition adapters.
Each implementation defines how to build a Django condition from PropertyRef, Operator, and value.
"""

from abc import ABC, abstractmethod
from typing import Any, TypeVar, Generic
from dynamic_filter_core import Operator, PropertyRef
from .django-condition-adapter import DjangoConditionAdapter

T = TypeVar('T')
P = TypeVar('P', bound=PropertyRef)


class DjangoConditionAdapterBuilder(ABC, Generic[T, P]):
    """
    Builder interface for creating Django condition adapters.
    Each implementation defines how to build a Django condition from PropertyRef, Operator, and value.
    """
    
    @abstractmethod
    def build(self, ref: P, op: Operator, value: Any) -> DjangoConditionAdapter[T]:
        """
        Builds a Django condition adapter from the given parameters.
        
        Args:
            ref: The property reference (type-safe)
            op: The operator
            value: The value as object
            
        Returns:
            A Django condition adapter
        """
        pass

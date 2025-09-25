"""
Builder interface for creating SQLAlchemy condition adapters.
Each implementation defines how to build a SQLAlchemy condition from PropertyRef, Operator, and value.
"""

from abc import ABC, abstractmethod
from typing import Any, TypeVar, Generic
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', '..', '..', 'core', 'python', 'src'))
from interfaces import PropertyRefEnum
from validation import Operator
from .sqlalchemy-condition-adapter import SQLAlchemyConditionAdapter

T = TypeVar('T')
P = TypeVar('P', bound=PropertyRefEnum)


class SQLAlchemyConditionAdapterBuilder(ABC, Generic[T, P]):
    """
    Builder interface for creating SQLAlchemy condition adapters.
    Each implementation defines how to build a SQLAlchemy condition from PropertyRef, Operator, and value.
    """
    
    @abstractmethod
    def build(self, ref: str, op: Operator, value: Any) -> SQLAlchemyConditionAdapter[T]:
        """
        Builds a SQLAlchemy condition adapter from the given parameters.
        
        Args:
            ref: The property reference key (string)
            op: The operator
            value: The value as object
            
        Returns:
            A SQLAlchemy condition adapter
        """
        pass


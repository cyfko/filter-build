"""
Core interfaces for the dynamic filtering system.
These interfaces define the contract that all implementations must follow.
"""

from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional, TypeVar, Generic, Union
from dataclasses import dataclass
from enum import Enum

T = TypeVar('T')
P = TypeVar('P', bound=Enum)

class PropertyRefEnum(Enum):
    """Base enum for PropertyRef implementations."""
    pass

@dataclass
class FilterDefinition(Generic[P]):
    """Represents a single filter definition with property reference, operator, and value."""
    ref: P
    operator: 'Operator'
    value: Any

@dataclass
class FilterRequest(Generic[P]):
    """Represents a complete filter request containing multiple filter definitions and a DSL expression."""
    filters: Dict[str, FilterDefinition[P]]
    combine_with: str

class Condition(ABC):
    """Interface representing a filter condition that can be combined with other conditions."""
    
    @abstractmethod
    def and_(self, other: 'Condition') -> 'Condition':
        """Creates a new condition representing the logical AND of this condition and another."""
        pass
    
    @abstractmethod
    def or_(self, other: 'Condition') -> 'Condition':
        """Creates a new condition representing the logical OR of this condition and another."""
        pass
    
    @abstractmethod
    def not_(self) -> 'Condition':
        """Creates a new condition representing the logical negation of this condition."""
        pass

class Context(ABC):
    """Interface for providing conditions by filter key."""
    
    @abstractmethod
    def get_condition(self, filter_key: str) -> Optional[Condition]:
        """Retrieves the condition associated with the given filter key."""
        pass

class ContextAdapter(ABC, Generic[T, P]):
    """Context adapter interface for type-safe filter building.
    
    Args:
        T: The entity type (e.g., User, Product)
        P: The PropertyRef enum for this entity
    """
    
    @abstractmethod
    def add_condition(self, filter_key: str, definition: FilterDefinition[P]) -> None:
        """Adds a condition for the given filter key."""
        pass
    
    @abstractmethod
    def get_condition(self, filter_key: str) -> Optional[Condition]:
        """Retrieves the condition associated with the given filter key."""
        pass

class ConditionAdapterBuilder(ABC, Generic[T, P]):
    """Builder interface for creating condition adapters.
    
    Each implementation defines how to build a condition from PropertyRef, Operator, and value.
    
    Args:
        T: The entity type (e.g., User, Product)
        P: The PropertyRef enum for this entity
    """
    
    @abstractmethod
    def build(self, ref: P, operator: 'Operator', value: Any) -> Condition:
        """Builds a condition adapter from the given parameters."""
        pass

class FilterTree(ABC):
    """Interface representing a parsed DSL expression tree."""
    
    @abstractmethod
    def generate(self, context: Context) -> Condition:
        """Generates a global condition by resolving all filter references through the context."""
        pass

class Parser(ABC):
    """Interface for parsing DSL expressions into FilterTree structures."""
    
    @abstractmethod
    def parse(self, dsl_expression: str) -> FilterTree:
        """Parses a DSL expression string into a FilterTree."""
        pass

class FilterExecutor(ABC, Generic[T]):
    """Interface for executing filter conditions against a data source."""
    
    @abstractmethod
    def execute(self, global_condition: Condition, entity_class: type) -> List[T]:
        """Executes the filtering operation using the provided global condition."""
        pass

class DSLSyntaxException(Exception):
    """Exception thrown when a DSL expression contains syntax errors or invalid references."""
    pass

class FilterValidationException(Exception):
    """Exception thrown when a filter condition cannot be validated or constructed."""
    pass

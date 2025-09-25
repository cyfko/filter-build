"""
Validation utilities for the dynamic filtering system.
"""

from enum import Enum
from typing import Dict, List, Optional, Set, Protocol, TypeVar, Generic
from dataclasses import dataclass

class Operator(Enum):
    """Enumeration of supported filter operators."""
    EQUALS = "="
    NOT_EQUALS = "!="
    GREATER_THAN = ">"
    GREATER_THAN_OR_EQUAL = ">="
    LESS_THAN = "<"
    LESS_THAN_OR_EQUAL = "<="
    LIKE = "LIKE"
    NOT_LIKE = "NOT LIKE"
    IN = "IN"
    NOT_IN = "NOT IN"
    IS_NULL = "IS NULL"
    IS_NOT_NULL = "IS NOT NULL"
    BETWEEN = "BETWEEN"
    NOT_BETWEEN = "NOT BETWEEN"

class PropertyRef(Protocol):
    """
    Protocol for property references in dynamic filtering.
    
    Developers should create their own enums implementing this protocol to define
    the properties available for their entities.
    
    Example usage:
    ```python
    class UserPropertyRef(Enum):
        USER_NAME = "USER_NAME"
        USER_AGE = "USER_AGE"
        USER_EMAIL = "USER_EMAIL"
    
    # PropertyRef implementation
    class UserPropertyRefImpl:
        USER_NAME = PropertyRefImpl("string", [Operator.LIKE, Operator.EQUALS])
        USER_AGE = PropertyRefImpl("int", [Operator.EQUALS, Operator.GREATER_THAN])
        USER_EMAIL = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
    ```
    """
    
    @property
    def type_name(self) -> str:
        """The type of this property."""
        ...
    
    @property
    def supported_operators(self) -> List[Operator]:
        """The set of operators supported by this property."""
        ...

class PropertyRefImpl:
    """Implementation of PropertyRef protocol for enum values."""
    
    def __init__(self, type_name: str, supported_operators: List[Operator]):
        self._type_name = type_name
        self._supported_operators = supported_operators
    
    @property
    def type_name(self) -> str:
        return self._type_name
    
    @property
    def supported_operators(self) -> List[Operator]:
        return self._supported_operators
    
    def supports_operator(self, operator: Operator) -> bool:
        """Checks if this property supports the given operator."""
        return operator in self._supported_operators
    
    def validate_operator(self, operator: Operator) -> None:
        """Validates that the given operator is supported by this property."""
        if not self.supports_operator(operator):
            raise ValueError(
                f"Operator '{operator}' is not supported for this property. "
                f"Supported operators: {', '.join(str(op) for op in self._supported_operators)}"
            )
    
    def get_description(self) -> str:
        """Gets a human-readable description of this property reference."""
        return f"PropertyRef({self._type_name})"
    
    def __str__(self) -> str:
        return f"PropertyRef{{type={self._type_name}, supportedOperators={self._supported_operators}}}"


def parse_operator(value: str) -> Optional[Operator]:
    """Finds an operator by its symbol or code."""
    if not value:
        return None
    
    trimmed = value.strip().upper()
    
    for op in Operator:
        if op.value == value or op.value == trimmed:
            return op
    
    return None

def operator_requires_value(operator: Operator) -> bool:
    """Checks if this operator requires a value."""
    return operator not in (Operator.IS_NULL, Operator.IS_NOT_NULL)

def operator_supports_multiple_values(operator: Operator) -> bool:
    """Checks if this operator supports multiple values."""
    return operator in (Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN)

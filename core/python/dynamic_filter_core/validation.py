"""
Validation utilities for the dynamic filtering system.
"""

from enum import Enum
from typing import Dict, List, Optional, Set
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

@dataclass
class PropertyRef:
    """Represents a validated property reference that maps to an actual entity property."""
    name: str
    type_name: str
    nullable: bool = True

class PropertyRegistry:
    """Registry for managing allowed property references."""
    
    def __init__(self):
        self._properties: Dict[str, PropertyRef] = {}
    
    def register_property(self, name: str, type_name: str, nullable: bool = True) -> None:
        """Registers a property reference."""
        self._properties[name] = PropertyRef(name, type_name, nullable)
    
    def get_property(self, name: str) -> Optional[PropertyRef]:
        """Gets a property reference by name."""
        return self._properties.get(name)
    
    def has_property(self, name: str) -> bool:
        """Checks if a property is registered."""
        return name in self._properties
    
    def get_property_names(self) -> Set[str]:
        """Gets all registered property names."""
        return set(self._properties.keys())
    
    def get_all_properties(self) -> Dict[str, PropertyRef]:
        """Gets all registered property references."""
        return self._properties.copy()
    
    def clear(self) -> None:
        """Clears all registered properties."""
        self._properties.clear()
    
    def size(self) -> int:
        """Gets the number of registered properties."""
        return len(self._properties)

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

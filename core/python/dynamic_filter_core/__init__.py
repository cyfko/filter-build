"""
Dynamic Filter Core - Framework-agnostic core module for dynamic filtering with DSL support.
"""

from .interfaces import (
    FilterDefinition,
    FilterRequest,
    Condition,
    Context,
    FilterTree,
    Parser,
    FilterExecutor,
    DSLSyntaxException,
    FilterValidationException
)

from .validation import (
    Operator,
    PropertyRef,
    PropertyRegistry,
    parse_operator,
    operator_requires_value,
    operator_supports_multiple_values
)

from .parser import DSLParser

__version__ = "1.0.0"
__author__ = "Frank KOSSI"
__email__ = "frank.kossi@kunrin.com"

__all__ = [
    # Interfaces
    "FilterDefinition",
    "FilterRequest", 
    "Condition",
    "Context",
    "FilterTree",
    "Parser",
    "FilterExecutor",
    "DSLSyntaxException",
    "FilterValidationException",
    
    # Validation
    "Operator",
    "PropertyRef",
    "PropertyRegistry",
    "parse_operator",
    "operator_requires_value",
    "operator_supports_multiple_values",
    
    # Parser
    "DSLParser"
]

"""
SQLAlchemy Condition Adapter implementing the Condition interface.
This adapter wraps SQLAlchemy query conditions.
"""

from typing import TYPE_CHECKING, Any
from dynamic_filter_core import Condition

if TYPE_CHECKING:
    from .sqlalchemy_condition_adapter import SQLAlchemyConditionAdapter


class SQLAlchemyConditionAdapter(Condition):
    """SQLAlchemy implementation of the Condition interface using query conditions."""
    
    def __init__(self, where_condition: Any):
        self.where_condition = where_condition
    
    def and_(self, other: 'Condition') -> 'Condition':
        """Creates a new condition representing the logical AND of this condition and another."""
        if not isinstance(other, SQLAlchemyConditionAdapter):
            raise ValueError("Cannot combine with non-SQLAlchemy condition")
        
        other_sqlalchemy = other
        return SQLAlchemyConditionAdapter(self.where_condition & other_sqlalchemy.where_condition)
    
    def or_(self, other: 'Condition') -> 'Condition':
        """Creates a new condition representing the logical OR of this condition and another."""
        if not isinstance(other, SQLAlchemyConditionAdapter):
            raise ValueError("Cannot combine with non-SQLAlchemy condition")
        
        other_sqlalchemy = other
        return SQLAlchemyConditionAdapter(self.where_condition | other_sqlalchemy.where_condition)
    
    def not_(self) -> 'Condition':
        """Creates a new condition representing the logical negation of this condition."""
        return SQLAlchemyConditionAdapter(~self.where_condition)
    
    def get_where_condition(self) -> Any:
        """Gets the underlying SQLAlchemy where condition."""
        return self.where_condition


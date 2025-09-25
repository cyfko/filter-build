"""
Django Condition Adapter implementing the Condition interface.
This adapter wraps Django Q objects.
"""

from typing import TYPE_CHECKING
from django.db.models import Q
from dynamic_filter_core import Condition

if TYPE_CHECKING:
    from .django_condition_adapter import DjangoConditionAdapter


class DjangoConditionAdapter(Condition):
    """Django implementation of the Condition interface using Q objects."""
    
    def __init__(self, q_object: Q):
        self.q_object = q_object
    
    def and_(self, other: 'Condition') -> 'Condition':
        """Creates a new condition representing the logical AND of this condition and another."""
        if not isinstance(other, DjangoConditionAdapter):
            raise ValueError("Cannot combine with non-Django condition")
        
        other_django = other
        return DjangoConditionAdapter(self.q_object & other_django.q_object)
    
    def or_(self, other: 'Condition') -> 'Condition':
        """Creates a new condition representing the logical OR of this condition and another."""
        if not isinstance(other, DjangoConditionAdapter):
            raise ValueError("Cannot combine with non-Django condition")
        
        other_django = other
        return DjangoConditionAdapter(self.q_object | other_django.q_object)
    
    def not_(self) -> 'Condition':
        """Creates a new condition representing the logical negation of this condition."""
        return DjangoConditionAdapter(~self.q_object)
    
    def get_q_object(self) -> Q:
        """Gets the underlying Django Q object."""
        return self.q_object

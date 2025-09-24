"""
Django ORM adapter for dynamic filtering.
Converts core conditions into Django Q objects.
"""

from typing import Any, Dict, List, Optional, Type, TypeVar
from django.db.models import Q, QuerySet
from django.db import models

from dynamic_filter_core import (
    Condition, Context, FilterDefinition, FilterExecutor,
    Operator, PropertyRegistry, parse_operator
)

T = TypeVar('T')

class DjangoCondition(Condition):
    """Django implementation of the Condition interface using Q objects."""
    
    def __init__(self, q_object: Q):
        self.q_object = q_object
    
    def get_q_object(self) -> Q:
        """Gets the underlying Django Q object."""
        return self.q_object
    
    def and_(self, other: Condition) -> Condition:
        if not isinstance(other, DjangoCondition):
            raise ValueError("Cannot combine with non-Django condition")
        
        return DjangoCondition(self.q_object & other.q_object)
    
    def or_(self, other: Condition) -> Condition:
        if not isinstance(other, DjangoCondition):
            raise ValueError("Cannot combine with non-Django condition")
        
        return DjangoCondition(self.q_object | other.q_object)
    
    def not_(self) -> Condition:
        return DjangoCondition(~self.q_object)

class DjangoContextAdapter(Context):
    """Django implementation of the Context interface."""
    
    def __init__(self, filters: Dict[str, FilterDefinition], property_registry: PropertyRegistry):
        self.filters = filters
        self.property_registry = property_registry
    
    def get_condition(self, filter_key: str) -> Optional[Condition]:
        filter_def = self.filters.get(filter_key)
        if not filter_def:
            raise ValueError(f"No filter found for key: {filter_key}")
        
        return self._create_condition(filter_def)
    
    def _create_condition(self, filter_def: FilterDefinition) -> Condition:
        # Validate property reference
        property_ref = self.property_registry.get_property(filter_def.ref)
        if not property_ref:
            raise ValueError(f"Property not found: {filter_def.ref}")
        
        # Validate operator
        operator = parse_operator(filter_def.operator)
        if not operator:
            raise ValueError(f"Invalid operator: {filter_def.operator}")
        
        # Create Django Q object
        q_object = self._create_q_object(filter_def.ref, operator, filter_def.value, property_ref.type_name)
        
        return DjangoCondition(q_object)
    
    def _create_q_object(self, property_ref: str, operator: Operator, value: Any, expected_type: str) -> Q:
        """Creates a Django Q object based on the operator and value."""
        
        if operator == Operator.EQUALS:
            return Q(**{property_ref: value})
        elif operator == Operator.NOT_EQUALS:
            return ~Q(**{property_ref: value})
        elif operator == Operator.GREATER_THAN:
            return Q(**{f"{property_ref}__gt": value})
        elif operator == Operator.GREATER_THAN_OR_EQUAL:
            return Q(**{f"{property_ref}__gte": value})
        elif operator == Operator.LESS_THAN:
            return Q(**{f"{property_ref}__lt": value})
        elif operator == Operator.LESS_THAN_OR_EQUAL:
            return Q(**{f"{property_ref}__lte": value})
        elif operator == Operator.LIKE:
            return Q(**{f"{property_ref}__icontains": value})
        elif operator == Operator.NOT_LIKE:
            return ~Q(**{f"{property_ref}__icontains": value})
        elif operator == Operator.IN:
            values = value if isinstance(value, list) else [value]
            return Q(**{f"{property_ref}__in": values})
        elif operator == Operator.NOT_IN:
            values = value if isinstance(value, list) else [value]
            return ~Q(**{f"{property_ref}__in": values})
        elif operator == Operator.IS_NULL:
            return Q(**{f"{property_ref}__isnull": True})
        elif operator == Operator.IS_NOT_NULL:
            return Q(**{f"{property_ref}__isnull": False})
        elif operator == Operator.BETWEEN:
            if isinstance(value, list) and len(value) == 2:
                return Q(**{f"{property_ref}__range": (value[0], value[1])})
            else:
                raise ValueError("BETWEEN operator requires exactly 2 values")
        elif operator == Operator.NOT_BETWEEN:
            if isinstance(value, list) and len(value) == 2:
                return ~Q(**{f"{property_ref}__range": (value[0], value[1])})
            else:
                raise ValueError("NOT BETWEEN operator requires exactly 2 values")
        else:
            raise ValueError(f"Unsupported operator: {operator}")

class DjangoFilterExecutor(FilterExecutor[T]):
    """Django implementation of the FilterExecutor interface."""
    
    def __init__(self, model_class: Type[models.Model]):
        self.model_class = model_class
    
    def execute(self, global_condition: Condition, entity_class: Type[T]) -> List[T]:
        if not isinstance(global_condition, DjangoCondition):
            raise ValueError("Condition must be a DjangoCondition")
        
        queryset = self.model_class.objects.filter(global_condition.get_q_object())
        return list(queryset)

class DjangoFilterService:
    """Main service class for Django-based dynamic filtering."""
    
    def __init__(self, parser, property_registry: PropertyRegistry, model_class: Type[models.Model]):
        self.parser = parser
        self.property_registry = property_registry
        self.model_class = model_class
    
    def execute_filter(self, filter_request, entity_class: Type[T]) -> List[T]:
        """Executes a filter request against the specified entity class."""
        # Parse DSL expression
        filter_tree = self.parser.parse(filter_request.combine_with)
        
        # Create Django context
        context = DjangoContextAdapter(filter_request.filters, self.property_registry)
        
        # Generate global condition
        global_condition = filter_tree.generate(context)
        
        # Execute filter
        executor = DjangoFilterExecutor(self.model_class)
        return executor.execute(global_condition, entity_class)
    
    def create_filtered_queryset(self, filter_request) -> QuerySet:
        """Creates a QuerySet with the applied filter conditions."""
        # Parse DSL expression
        filter_tree = self.parser.parse(filter_request.combine_with)
        
        # Create Django context
        context = DjangoContextAdapter(filter_request.filters, self.property_registry)
        
        # Generate global condition
        global_condition = filter_tree.generate(context)
        
        # Apply condition to queryset
        if isinstance(global_condition, DjangoCondition):
            return self.model_class.objects.filter(global_condition.get_q_object())
        
        raise ValueError("Invalid condition type")

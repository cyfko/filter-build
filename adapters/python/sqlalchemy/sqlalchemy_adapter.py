"""
SQLAlchemy adapter for dynamic filtering.
Converts core conditions into SQLAlchemy query conditions.
"""

from typing import Any, Dict, List, Optional, Type, TypeVar
from sqlalchemy import and_, or_, not_, Column
from sqlalchemy.orm import Query, Session
from sqlalchemy.sql import ClauseElement

from dynamic_filter_core import (
    Condition, Context, FilterDefinition, FilterExecutor,
    Operator, PropertyRegistry, parse_operator
)

T = TypeVar('T')

class SQLAlchemyCondition(Condition):
    """SQLAlchemy implementation of the Condition interface."""
    
    def __init__(self, clause: ClauseElement):
        self.clause = clause
    
    def get_clause(self) -> ClauseElement:
        """Gets the underlying SQLAlchemy clause."""
        return self.clause
    
    def and_(self, other: Condition) -> Condition:
        if not isinstance(other, SQLAlchemyCondition):
            raise ValueError("Cannot combine with non-SQLAlchemy condition")
        
        return SQLAlchemyCondition(and_(self.clause, other.clause))
    
    def or_(self, other: Condition) -> Condition:
        if not isinstance(other, SQLAlchemyCondition):
            raise ValueError("Cannot combine with non-SQLAlchemy condition")
        
        return SQLAlchemyCondition(or_(self.clause, other.clause))
    
    def not_(self) -> Condition:
        return SQLAlchemyCondition(not_(self.clause))

class SQLAlchemyContextAdapter(Context):
    """SQLAlchemy implementation of the Context interface."""
    
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
        
        # Create SQLAlchemy condition
        clause = self._create_clause(filter_def.ref, operator, filter_def.value, property_ref.type_name)
        
        return SQLAlchemyCondition(clause)
    
    def _create_clause(self, property_ref: str, operator: Operator, value: Any, expected_type: str) -> ClauseElement:
        # This is a simplified implementation
        # In a real implementation, you would need to map property references to actual SQLAlchemy columns
        
        if operator == Operator.EQUALS:
            return Column(property_ref) == value
        elif operator == Operator.NOT_EQUALS:
            return Column(property_ref) != value
        elif operator == Operator.GREATER_THAN:
            return Column(property_ref) > value
        elif operator == Operator.GREATER_THAN_OR_EQUAL:
            return Column(property_ref) >= value
        elif operator == Operator.LESS_THAN:
            return Column(property_ref) < value
        elif operator == Operator.LESS_THAN_OR_EQUAL:
            return Column(property_ref) <= value
        elif operator == Operator.LIKE:
            return Column(property_ref).like(f"%{value}%")
        elif operator == Operator.NOT_LIKE:
            return ~Column(property_ref).like(f"%{value}%")
        elif operator == Operator.IN:
            values = value if isinstance(value, list) else [value]
            return Column(property_ref).in_(values)
        elif operator == Operator.NOT_IN:
            values = value if isinstance(value, list) else [value]
            return ~Column(property_ref).in_(values)
        elif operator == Operator.IS_NULL:
            return Column(property_ref).is_(None)
        elif operator == Operator.IS_NOT_NULL:
            return Column(property_ref).isnot(None)
        elif operator == Operator.BETWEEN:
            if isinstance(value, list) and len(value) == 2:
                return Column(property_ref).between(value[0], value[1])
            else:
                raise ValueError("BETWEEN operator requires exactly 2 values")
        elif operator == Operator.NOT_BETWEEN:
            if isinstance(value, list) and len(value) == 2:
                return ~Column(property_ref).between(value[0], value[1])
            else:
                raise ValueError("NOT BETWEEN operator requires exactly 2 values")
        else:
            raise ValueError(f"Unsupported operator: {operator}")

class SQLAlchemyFilterExecutor(FilterExecutor[T]):
    """SQLAlchemy implementation of the FilterExecutor interface."""
    
    def __init__(self, session: Session):
        self.session = session
    
    def execute(self, global_condition: Condition, entity_class: Type[T]) -> List[T]:
        if not isinstance(global_condition, SQLAlchemyCondition):
            raise ValueError("Condition must be a SQLAlchemyCondition")
        
        query = self.session.query(entity_class)
        query = query.filter(global_condition.get_clause())
        
        return query.all()

class SQLAlchemyFilterService:
    """Main service class for SQLAlchemy-based dynamic filtering."""
    
    def __init__(self, parser, property_registry: PropertyRegistry, session: Session):
        self.parser = parser
        self.property_registry = property_registry
        self.session = session
    
    def execute_filter(self, filter_request, entity_class: Type[T]) -> List[T]:
        """Executes a filter request against the specified entity class."""
        # Parse DSL expression
        filter_tree = self.parser.parse(filter_request.combine_with)
        
        # Create SQLAlchemy context
        context = SQLAlchemyContextAdapter(filter_request.filters, self.property_registry)
        
        # Generate global condition
        global_condition = filter_tree.generate(context)
        
        # Execute filter
        executor = SQLAlchemyFilterExecutor(self.session)
        return executor.execute(global_condition, entity_class)
    
    def create_filtered_query(self, filter_request, entity_class: Type[T]) -> Query:
        """Creates a Query with the applied filter conditions."""
        # Parse DSL expression
        filter_tree = self.parser.parse(filter_request.combine_with)
        
        # Create SQLAlchemy context
        context = SQLAlchemyContextAdapter(filter_request.filters, self.property_registry)
        
        # Generate global condition
        global_condition = filter_tree.generate(context)
        
        # Apply condition to query
        if isinstance(global_condition, SQLAlchemyCondition):
            query = self.session.query(entity_class)
            return query.filter(global_condition.get_clause())
        
        raise ValueError("Invalid condition type")

"""
Example usage of Python adapters with the new type-safe architecture
"""

from dynamic_filter_core import FilterDefinition, FilterRequest, ContextAdapter, ConditionAdapterBuilder, Condition
from dynamic_filter_core.validation import Operator
from dynamic_filter_core.test_types import TestPropertyRef, TestPropertyRefImpl

# Django imports
from .django.django_context_adapter import DjangoContextAdapter
from .django.django_condition_adapter_builder import DjangoConditionAdapterBuilder
from .django.django_condition_adapter import DjangoConditionAdapter

# SQLAlchemy imports
from .sqlalchemy.sqlalchemy_context_adapter import SQLAlchemyContextAdapter
from .sqlalchemy.sqlalchemy_condition_adapter_builder import SQLAlchemyConditionAdapterBuilder
from .sqlalchemy.sqlalchemy_condition_adapter import SQLAlchemyConditionAdapter

# Example entity type
from typing import Any

class User:
    """Example entity type."""
    def __init__(self, id: int, name: str, age: int, email: str, status: str):
        self.id = id
        self.name = name
        self.age = age
        self.email = email
        self.status = status

# Example Django condition adapter builder implementation
class ExampleDjangoConditionAdapterBuilder(DjangoConditionAdapterBuilder[User, TestPropertyRef]):
    def build(self, ref: TestPropertyRef, op: Operator, value: Any) -> DjangoConditionAdapter[User]:
        from django.db.models import Q
        
        # Convert enum key to entity field
        field_name = ref.name.lower().replace('_', '_')
        
        if op == Operator.EQUALS:
            q_object = Q(**{field_name: value})
        elif op == Operator.LIKE:
            q_object = Q(**{f"{field_name}__icontains": value})
        elif op == Operator.GREATER_THAN:
            q_object = Q(**{f"{field_name}__gt": value})
        else:
            raise ValueError(f"Unsupported operator: {op}")
        
        return DjangoConditionAdapter[User](q_object)

# Example SQLAlchemy condition adapter builder implementation
class ExampleSQLAlchemyConditionAdapterBuilder(SQLAlchemyConditionAdapterBuilder[User, TestPropertyRef]):
    def build(self, ref: TestPropertyRef, op: Operator, value: Any) -> SQLAlchemyConditionAdapter[User]:
        from sqlalchemy import and_, or_, not_
        
        # Convert enum key to entity field
        field_name = ref.name.lower().replace('_', '_')
        
        if op == Operator.EQUALS:
            condition = getattr(User, field_name) == value
        elif op == Operator.LIKE:
            condition = getattr(User, field_name).like(f"%{value}%")
        elif op == Operator.GREATER_THAN:
            condition = getattr(User, field_name) > value
        else:
            raise ValueError(f"Unsupported operator: {op}")
        
        return SQLAlchemyConditionAdapter[User](condition)

# Example usage
def demonstrate_adapters():
    # Create filter definitions
    name_filter = FilterDefinition[TestPropertyRef](
        ref=TestPropertyRef.USER_NAME,
        operator=Operator.LIKE,
        value="John%"
    )

    age_filter = FilterDefinition[TestPropertyRef](
        ref=TestPropertyRef.USER_AGE,
        operator=Operator.GREATER_THAN,
        value=18
    )

    # Django adapter usage
    django_builder = ExampleDjangoConditionAdapterBuilder()
    django_adapter = DjangoContextAdapter[User, TestPropertyRef](
        django_builder, 
        TestPropertyRefImpl
    )

    django_adapter.add_condition("nameFilter", name_filter)
    django_adapter.add_condition("ageFilter", age_filter)

    # SQLAlchemy adapter usage
    sqlalchemy_builder = ExampleSQLAlchemyConditionAdapterBuilder()
    sqlalchemy_adapter = SQLAlchemyContextAdapter[User, TestPropertyRef](
        sqlalchemy_builder,
        TestPropertyRefImpl
    )

    sqlalchemy_adapter.add_condition("nameFilter", name_filter)
    sqlalchemy_adapter.add_condition("ageFilter", age_filter)

    print("✅ Python adapters validation successful!")
    
    return { 
        "django_adapter": django_adapter, 
        "sqlalchemy_adapter": sqlalchemy_adapter 
    }

if __name__ == "__main__":
    demonstrate_adapters()


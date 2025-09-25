"""
Example usage of the new Python architecture
"""

from dynamic_filter_core.validation import Operator
from dynamic_filter_core.interfaces import FilterDefinition, FilterRequest, ContextAdapter, ConditionAdapterBuilder, Condition
from dynamic_filter_core.test_types import TestPropertyRef, TestPropertyRefImpl, get_property_ref

# Example 1: Creating a FilterDefinition
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

# Example 2: Creating a FilterRequest
filter_request = FilterRequest[TestPropertyRef](
    filters={
        "nameFilter": name_filter,
        "ageFilter": age_filter
    },
    combine_with="AND"
)

# Example 3: Type safety validation
def validate_filter(definition: FilterDefinition[TestPropertyRef]) -> None:
    """Validates that the filter definition is type-safe."""
    ref = definition.ref
    property_ref = get_property_ref(ref)
    
    if not property_ref.supports_operator(definition.operator):
        raise ValueError(
            f"Operator {definition.operator} not supported for property {ref}"
        )

# Test validation
try:
    validate_filter(name_filter)
    validate_filter(age_filter)
    print("✅ Python validation successful!")
except ValueError as e:
    print(f"❌ Python validation failed: {e}")

# Example 4: ContextAdapter usage
class ExampleContextAdapter(ContextAdapter[object, TestPropertyRef]):
    """Example implementation of ContextAdapter."""
    
    def __init__(self):
        self._conditions = {}
    
    def add_condition(self, filter_key: str, definition: FilterDefinition[TestPropertyRef]) -> None:
        """Adds a condition for the given filter key."""
        # Validate the filter
        validate_filter(definition)
        
        # Store the condition (simplified)
        self._conditions[filter_key] = definition
    
    def get_condition(self, filter_key: str) -> Condition:
        """Retrieves the condition associated with the given filter key."""
        return self._conditions.get(filter_key)

# Example usage
adapter = ExampleContextAdapter()
adapter.add_condition("nameFilter", name_filter)
adapter.add_condition("ageFilter", age_filter)

print("✅ Python ContextAdapter usage successful!")

if __name__ == "__main__":
    print("Python core architecture validation completed!")


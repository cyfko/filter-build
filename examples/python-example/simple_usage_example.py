"""
Example showing how simple it is to use the PropertyRef class approach.
Developers only need to extend PropertyRef and call super() - that's it!
"""

from dynamic_filter_core.validation import PropertyRegistry, Operator
from user_property_ref import UserPropertyRef


def main():
    # Using UserPropertyRef - all methods are inherited from PropertyRef!
    print("=== User Properties ===")
    demonstrate_property_ref(UserPropertyRef.USER_NAME)
    demonstrate_property_ref(UserPropertyRef.USER_AGE)
    demonstrate_property_ref(UserPropertyRef.USER_STATUS)
    
    print("\n=== Product Properties ===")
    # You can create other property refs similarly
    demonstrate_property_ref(UserPropertyRef.USER_NAME)
    demonstrate_property_ref(UserPropertyRef.USER_AGE)
    demonstrate_property_ref(UserPropertyRef.USER_IS_ACTIVE)
    
    # Test operator validation
    print("\n=== Operator Validation ===")
    test_operator_validation()
    
    # Test PropertyRegistry
    print("\n=== PropertyRegistry Usage ===")
    test_property_registry()


def demonstrate_property_ref(property_ref):
    print(f"Property: {property_ref}")
    print(f"  Entity Field: {property_ref.entity_field}")
    print(f"  Type: {property_ref.type_name}")
    print(f"  Supports LIKE: {property_ref.supports_operator(Operator.LIKE)}")
    print(f"  Supports >: {property_ref.supports_operator(Operator.GREATER_THAN)}")
    print(f"  Description: {property_ref.get_description()}")
    print()


def test_operator_validation():
    # Valid operations
    print("Testing valid operations:")
    assert_does_not_throw(lambda: UserPropertyRef.USER_NAME.validate_operator(Operator.LIKE))
    assert_does_not_throw(lambda: UserPropertyRef.USER_AGE.validate_operator(Operator.GREATER_THAN))
    assert_does_not_throw(lambda: UserPropertyRef.USER_SALARY.validate_operator(Operator.BETWEEN))
    print("✓ All valid operations passed")
    
    # Invalid operations
    print("Testing invalid operations:")
    try:
        UserPropertyRef.USER_NAME.validate_operator(Operator.GREATER_THAN)
        print("✗ Should have thrown exception")
    except ValueError as error:
        print(f"✓ Correctly rejected: {error}")
    
    try:
        UserPropertyRef.USER_AGE.validate_operator(Operator.LIKE)
        print("✗ Should have thrown exception")
    except ValueError as error:
        print(f"✓ Correctly rejected: {error}")


def test_property_registry():
    registry = PropertyRegistry()
    
    # Register all properties from UserPropertyRef
    registry.register_all(UserPropertyRef)
    
    print(f"Registered {registry.size()} properties")
    print("Property names:", registry.get_property_names())
    
    # Test property lookup
    user_name_prop = registry.get_property("userName")
    print(f"Found userName property: {user_name_prop.get_description() if user_name_prop else 'Not found'}")
    
    # Test operator validation through registry
    try:
        registry.validate_property_operator("userName", Operator.LIKE)
        print("✓ Registry validation passed for userName + LIKE")
    except ValueError as error:
        print(f"✗ Registry validation failed: {error}")
    
    try:
        registry.validate_property_operator("userName", Operator.GREATER_THAN)
        print("✗ Should have failed for userName + GREATER_THAN")
    except ValueError as error:
        print(f"✓ Registry correctly rejected userName + GREATER_THAN")


def assert_does_not_throw(fn):
    try:
        fn()
    except Exception as error:
        raise AssertionError(f"Expected no exception, but got: {error}")


if __name__ == "__main__":
    main()

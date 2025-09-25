"""
Test types for Python core validation
"""

from enum import Enum
from typing import Dict, List
from .validation import Operator, PropertyRef, PropertyRefImpl

class TestPropertyRef(Enum):
    """Test enum implementing PropertyRef for testing purposes."""
    USER_NAME = "USER_NAME"
    USER_AGE = "USER_AGE"
    USER_EMAIL = "USER_EMAIL"
    USER_STATUS = "USER_STATUS"

class TestPropertyRefImpl:
    """PropertyRef implementation for TestPropertyRef values."""
    
    USER_NAME = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE, Operator.IN])
    USER_AGE = PropertyRefImpl("int", [Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN, Operator.BETWEEN])
    USER_EMAIL = PropertyRefImpl("string", [Operator.EQUALS, Operator.LIKE])
    USER_STATUS = PropertyRefImpl("string", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN])

def get_property_ref(ref: TestPropertyRef) -> PropertyRef:
    """Helper function to get PropertyRef implementation for a TestPropertyRef value."""
    impl_map = {
        TestPropertyRef.USER_NAME: TestPropertyRefImpl.USER_NAME,
        TestPropertyRef.USER_AGE: TestPropertyRefImpl.USER_AGE,
        TestPropertyRef.USER_EMAIL: TestPropertyRefImpl.USER_EMAIL,
        TestPropertyRef.USER_STATUS: TestPropertyRefImpl.USER_STATUS,
    }
    return impl_map[ref]

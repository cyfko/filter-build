"""
Example PropertyRef enum for User entity.
This shows how developers should define their own property references.
They only need to extend PropertyRef and call super() - all methods are inherited!
"""

from dynamic_filter_core.validation import PropertyRef, Operator


class UserPropertyRef(PropertyRef):
    """Example PropertyRef enum for User entity."""
    
    # User entity properties
    USER_NAME = PropertyRef("userName", "str", [Operator.LIKE, Operator.EQUALS, Operator.IN, Operator.NOT_IN])
    USER_EMAIL = PropertyRef("email", "str", [Operator.LIKE, Operator.EQUALS, Operator.IN, Operator.NOT_IN])
    USER_STATUS = PropertyRef("status", "str", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.IN, Operator.NOT_IN])
    USER_AGE = PropertyRef("age", "int", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.IN, Operator.NOT_IN, Operator.BETWEEN, Operator.NOT_BETWEEN])
    USER_CREATED_DATE = PropertyRef("createdDate", "datetime", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN])
    USER_IS_ACTIVE = PropertyRef("active", "bool", [Operator.EQUALS, Operator.NOT_EQUALS])
    USER_SALARY = PropertyRef("salary", "float", [Operator.EQUALS, Operator.NOT_EQUALS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL, Operator.BETWEEN, Operator.NOT_BETWEEN])

    @classmethod
    def find_by_entity_field(cls, entity_field: str) -> PropertyRef:
        """
        Finds a UserPropertyRef by its entity field name.
        
        Args:
            entity_field: The entity field name to search for
            
        Returns:
            The matching UserPropertyRef or None if not found
        """
        all_properties = [
            cls.USER_NAME,
            cls.USER_EMAIL,
            cls.USER_STATUS,
            cls.USER_AGE,
            cls.USER_CREATED_DATE,
            cls.USER_IS_ACTIVE,
            cls.USER_SALARY
        ]

        for prop in all_properties:
            if prop.entity_field == entity_field:
                return prop
        return None

    @classmethod
    def get_all_properties(cls) -> list[PropertyRef]:
        """
        Gets all UserPropertyRef values.
        
        Returns:
            A list of all UserPropertyRef values
        """
        return [
            cls.USER_NAME,
            cls.USER_EMAIL,
            cls.USER_STATUS,
            cls.USER_AGE,
            cls.USER_CREATED_DATE,
            cls.USER_IS_ACTIVE,
            cls.USER_SALARY
        ]

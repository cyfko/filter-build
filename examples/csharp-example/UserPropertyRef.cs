using DynamicFilter.Core.Validation;

namespace DynamicFilter.Example
{
    /// <summary>
    /// Example PropertyRef enum for User entity.
    /// This shows how developers should define their own property references.
    /// They only need to extend PropertyRef and call super() - all methods are inherited!
    /// </summary>
    public class UserPropertyRef : PropertyRef
    {
        // User entity properties
        public static readonly UserPropertyRef UserName = new UserPropertyRef("userName", "string", new[] { Operator.Like, Operator.Equals, Operator.In, Operator.NotIn });
        public static readonly UserPropertyRef UserEmail = new UserPropertyRef("email", "string", new[] { Operator.Like, Operator.Equals, Operator.In, Operator.NotIn });
        public static readonly UserPropertyRef UserStatus = new UserPropertyRef("status", "string", new[] { Operator.Equals, Operator.NotEquals, Operator.In, Operator.NotIn });
        public static readonly UserPropertyRef UserAge = new UserPropertyRef("age", "int", new[] { Operator.Equals, Operator.NotEquals, Operator.GreaterThan, Operator.GreaterThanOrEqual, Operator.LessThan, Operator.LessThanOrEqual, Operator.In, Operator.NotIn, Operator.Between, Operator.NotBetween });
        public static readonly UserPropertyRef UserCreatedDate = new UserPropertyRef("createdDate", "DateTime", new[] { Operator.Equals, Operator.NotEquals, Operator.GreaterThan, Operator.GreaterThanOrEqual, Operator.LessThan, Operator.LessThanOrEqual, Operator.Between, Operator.NotBetween });
        public static readonly UserPropertyRef UserIsActive = new UserPropertyRef("active", "bool", new[] { Operator.Equals, Operator.NotEquals });
        public static readonly UserPropertyRef UserSalary = new UserPropertyRef("salary", "double", new[] { Operator.Equals, Operator.NotEquals, Operator.GreaterThan, Operator.GreaterThanOrEqual, Operator.LessThan, Operator.LessThanOrEqual, Operator.Between, Operator.NotBetween });

        private UserPropertyRef(string entityField, string type, Operator[] supportedOperators) 
            : base(entityField, type, supportedOperators)
        {
        }

        /// <summary>
        /// Finds a UserPropertyRef by its entity field name.
        /// </summary>
        /// <param name="entityField">The entity field name to search for</param>
        /// <returns>The matching UserPropertyRef or null if not found</returns>
        public static UserPropertyRef FindByEntityField(string entityField)
        {
            var allProperties = new[]
            {
                UserName,
                UserEmail,
                UserStatus,
                UserAge,
                UserCreatedDate,
                UserIsActive,
                UserSalary
            };

            return allProperties.FirstOrDefault(prop => prop.EntityField == entityField);
        }

        /// <summary>
        /// Gets all UserPropertyRef values.
        /// </summary>
        /// <returns>An array of all UserPropertyRef values</returns>
        public static UserPropertyRef[] GetAllProperties()
        {
            return new[]
            {
                UserName,
                UserEmail,
                UserStatus,
                UserAge,
                UserCreatedDate,
                UserIsActive,
                UserSalary
            };
        }
    }
}

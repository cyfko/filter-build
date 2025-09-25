using System;
using System.Collections.Generic;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Core.TestTypes
{
    /// <summary>
    /// Test enum implementing PropertyRef for testing purposes.
    /// </summary>
    public enum TestPropertyRef
    {
        UserName,
        UserAge,
        UserEmail,
        UserStatus
    }

    /// <summary>
    /// PropertyRef implementation for TestPropertyRef values.
    /// </summary>
    public static class TestPropertyRefImpl
    {
        public static readonly IPropertyRef UserName = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like, Operator.In });
        public static readonly IPropertyRef UserAge = new PropertyRefImpl("int", new[] { Operator.Equals, Operator.GreaterThan, Operator.LessThan, Operator.Between });
        public static readonly IPropertyRef UserEmail = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });
        public static readonly IPropertyRef UserStatus = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.NotEquals, Operator.In });

        /// <summary>
        /// Gets the PropertyRef implementation for a TestPropertyRef value.
        /// </summary>
        /// <param name="ref">The PropertyRef enum value</param>
        /// <returns>The PropertyRef implementation</returns>
        public static IPropertyRef GetPropertyRef(TestPropertyRef @ref)
        {
            return @ref switch
            {
                TestPropertyRef.UserName => UserName,
                TestPropertyRef.UserAge => UserAge,
                TestPropertyRef.UserEmail => UserEmail,
                TestPropertyRef.UserStatus => UserStatus,
                _ => throw new ArgumentException($"Unknown PropertyRef: {@ref}")
            };
        }
    }
}


using System;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Core.Tests.TestTypes
{
    /// <summary>
    /// Test enum for PropertyRef testing.
    /// </summary>
    public enum TestPropertyRef
    {
        UserName,
        UserAge,
        UserEmail,
        UserStatus
    }

    /// <summary>
    /// Test implementation of PropertyRef for testing.
    /// </summary>
    public static class TestPropertyRefImpl
    {
        public static readonly IPropertyRef UserName = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like, Operator.In });
        public static readonly IPropertyRef UserAge = new PropertyRefImpl("int", new[] { Operator.Equals, Operator.GreaterThan, Operator.LessThan });
        public static readonly IPropertyRef UserEmail = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });
        public static readonly IPropertyRef UserStatus = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.In });
    }
}

using System;
using Xunit;
using DynamicFilter.Core.Validation;

namespace DynamicFilter.Core.Tests.Validation
{
    public class OperatorTests
    {
        [Fact]
        public void Operator_Values_ShouldBeCorrect()
        {
            // Assert
            Assert.Equal("=", Operator.Equals.ToString());
            Assert.Equal("!=", Operator.NotEquals.ToString());
            Assert.Equal(">", Operator.GreaterThan.ToString());
            Assert.Equal(">=", Operator.GreaterThanOrEqual.ToString());
            Assert.Equal("<", Operator.LessThan.ToString());
            Assert.Equal("<=", Operator.LessThanOrEqual.ToString());
            Assert.Equal("LIKE", Operator.Like.ToString());
            Assert.Equal("NOT LIKE", Operator.NotLike.ToString());
            Assert.Equal("IN", Operator.In.ToString());
            Assert.Equal("NOT IN", Operator.NotIn.ToString());
            Assert.Equal("BETWEEN", Operator.Between.ToString());
            Assert.Equal("NOT BETWEEN", Operator.NotBetween.ToString());
            Assert.Equal("IS NULL", Operator.IsNull.ToString());
            Assert.Equal("IS NOT NULL", Operator.IsNotNull.ToString());
        }

        [Fact]
        public void Operator_Enumeration_ShouldContainAllOperators()
        {
            // Act
            var operators = Enum.GetValues<Operator>();

            // Assert
            Assert.Equal(14, operators.Length);
            Assert.Contains(Operator.Equals, operators);
            Assert.Contains(Operator.NotEquals, operators);
            Assert.Contains(Operator.GreaterThan, operators);
            Assert.Contains(Operator.GreaterThanOrEqual, operators);
            Assert.Contains(Operator.LessThan, operators);
            Assert.Contains(Operator.LessThanOrEqual, operators);
            Assert.Contains(Operator.Like, operators);
            Assert.Contains(Operator.NotLike, operators);
            Assert.Contains(Operator.In, operators);
            Assert.Contains(Operator.NotIn, operators);
            Assert.Contains(Operator.Between, operators);
            Assert.Contains(Operator.NotBetween, operators);
            Assert.Contains(Operator.IsNull, operators);
            Assert.Contains(Operator.IsNotNull, operators);
        }
    }
}

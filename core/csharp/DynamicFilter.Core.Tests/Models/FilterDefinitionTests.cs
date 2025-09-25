using System;
using Xunit;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;
using DynamicFilter.Core.Tests.TestTypes;

namespace DynamicFilter.Core.Tests.Models
{
    public class FilterDefinitionTests
    {
        [Fact]
        public void FilterDefinition_Creation_ShouldSucceed()
        {
            // Arrange & Act
            var filterDef = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserName,
                Operator.Equals,
                "John"
            );

            // Assert
            Assert.Equal(TestPropertyRef.UserName, filterDef.Ref);
            Assert.Equal(Operator.Equals, filterDef.Operator);
            Assert.Equal("John", filterDef.Value);
        }

        [Fact]
        public void FilterDefinition_WithDifferentTypes_ShouldSucceed()
        {
            // String value
            var stringFilter = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserName,
                Operator.Like,
                "John%"
            );
            Assert.Equal("John%", stringFilter.Value);

            // Integer value
            var intFilter = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserAge,
                Operator.GreaterThan,
                18
            );
            Assert.Equal(18, intFilter.Value);

            // Array value
            var arrayFilter = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserStatus,
                Operator.In,
                new[] { "ACTIVE", "PENDING" }
            );
            Assert.Equal(new[] { "ACTIVE", "PENDING" }, arrayFilter.Value);
        }

        [Fact]
        public void FilterDefinition_WithNullValue_ShouldSucceed()
        {
            // Arrange & Act
            var filterDef = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserName,
                Operator.IsNull,
                null
            );

            // Assert
            Assert.Equal(TestPropertyRef.UserName, filterDef.Ref);
            Assert.Equal(Operator.IsNull, filterDef.Operator);
            Assert.Null(filterDef.Value);
        }

        [Fact]
        public void FilterDefinition_ToString_ShouldReturnCorrectString()
        {
            // Arrange
            var filterDef = new FilterDefinition<TestPropertyRef>(
                TestPropertyRef.UserName,
                Operator.Equals,
                "John"
            );

            // Act
            var result = filterDef.ToString();

            // Assert
            Assert.Contains("FilterDefinition{Ref=UserName", result);
            Assert.Contains("Operator='Equals'", result);
            Assert.Contains("Value=John", result);
        }
    }
}

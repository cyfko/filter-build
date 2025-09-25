using System;
using System.Collections.Generic;
using Xunit;
using DynamicFilter.Core.Models;
using DynamicFilter.Core.Validation;
using DynamicFilter.Core.Tests.TestTypes;

namespace DynamicFilter.Core.Tests.Models
{
    public class FilterRequestTests
    {
        [Fact]
        public void FilterRequest_Creation_ShouldSucceed()
        {
            // Arrange
            var filters = new Dictionary<string, FilterDefinition<TestPropertyRef>>
            {
                ["nameFilter"] = new FilterDefinition<TestPropertyRef>(
                    TestPropertyRef.UserName,
                    Operator.Like,
                    "John%"
                ),
                ["ageFilter"] = new FilterDefinition<TestPropertyRef>(
                    TestPropertyRef.UserAge,
                    Operator.GreaterThan,
                    18
                )
            };

            // Act
            var request = new FilterRequest<TestPropertyRef>(filters, "AND");

            // Assert
            Assert.Equal(2, request.Filters.Count);
            Assert.Equal("AND", request.CombineWith);
            Assert.Contains("nameFilter", request.Filters.Keys);
            Assert.Contains("ageFilter", request.Filters.Keys);
        }

        [Fact]
        public void FilterRequest_EmptyFilters_ShouldSucceed()
        {
            // Arrange
            var filters = new Dictionary<string, FilterDefinition<TestPropertyRef>>();

            // Act
            var request = new FilterRequest<TestPropertyRef>(filters, "OR");

            // Assert
            Assert.Empty(request.Filters);
            Assert.Equal("OR", request.CombineWith);
        }

        [Fact]
        public void FilterRequest_NullFilters_ShouldThrowArgumentNullException()
        {
            // Act & Assert
            Assert.Throws<ArgumentNullException>(() => new FilterRequest<TestPropertyRef>(null!, "AND"));
        }

        [Fact]
        public void FilterRequest_NullCombineWith_ShouldThrowArgumentNullException()
        {
            // Arrange
            var filters = new Dictionary<string, FilterDefinition<TestPropertyRef>>();

            // Act & Assert
            Assert.Throws<ArgumentNullException>(() => new FilterRequest<TestPropertyRef>(filters, null!));
        }

        [Fact]
        public void FilterRequest_ToString_ShouldReturnCorrectString()
        {
            // Arrange
            var filters = new Dictionary<string, FilterDefinition<TestPropertyRef>>
            {
                ["nameFilter"] = new FilterDefinition<TestPropertyRef>(
                    TestPropertyRef.UserName,
                    Operator.Equals,
                    "John"
                )
            };

            // Act
            var request = new FilterRequest<TestPropertyRef>(filters, "AND");
            var result = request.ToString();

            // Assert
            Assert.Contains("FilterRequest{Filters=1", result);
            Assert.Contains("CombineWith='AND'", result);
        }
    }
}

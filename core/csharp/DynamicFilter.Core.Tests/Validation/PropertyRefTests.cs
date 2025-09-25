using System;
using Xunit;
using DynamicFilter.Core.Validation;
using DynamicFilter.Core.Tests.TestTypes;

namespace DynamicFilter.Core.Tests.Validation
{
    public class PropertyRefTests
    {
        [Fact]
        public void PropertyRefImpl_Creation_ShouldSucceed()
        {
            // Arrange & Act
            var propertyRef = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });

            // Assert
            Assert.Equal("string", propertyRef.Type);
            Assert.Equal(2, propertyRef.SupportedOperators.Count);
            Assert.Contains(Operator.Equals, propertyRef.SupportedOperators);
            Assert.Contains(Operator.Like, propertyRef.SupportedOperators);
        }

        [Fact]
        public void PropertyRefImpl_SupportsOperator_ValidOperator_ShouldReturnTrue()
        {
            // Arrange
            var propertyRef = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });

            // Act & Assert
            Assert.True(propertyRef.SupportsOperator(Operator.Equals));
            Assert.True(propertyRef.SupportsOperator(Operator.Like));
        }

        [Fact]
        public void PropertyRefImpl_SupportsOperator_InvalidOperator_ShouldReturnFalse()
        {
            // Arrange
            var propertyRef = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });

            // Act & Assert
            Assert.False(propertyRef.SupportsOperator(Operator.GreaterThan));
            Assert.False(propertyRef.SupportsOperator(Operator.In));
        }

        [Fact]
        public void PropertyRefImpl_ValidateOperator_ValidOperator_ShouldNotThrow()
        {
            // Arrange
            var propertyRef = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });

            // Act & Assert
            var exception = Record.Exception(() => propertyRef.ValidateOperator(Operator.Equals));
            Assert.Null(exception);
        }

        [Fact]
        public void PropertyRefImpl_ValidateOperator_InvalidOperator_ShouldThrowArgumentException()
        {
            // Arrange
            var propertyRef = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });

            // Act & Assert
            var exception = Assert.Throws<ArgumentException>(() => propertyRef.ValidateOperator(Operator.GreaterThan));
            Assert.Contains("Operator 'GreaterThan' is not supported", exception.Message);
        }

        [Fact]
        public void PropertyRefImpl_GetDescription_ShouldReturnCorrectDescription()
        {
            // Arrange
            var propertyRef = new PropertyRefImpl("string", new[] { Operator.Equals });

            // Act
            var description = propertyRef.GetDescription();

            // Assert
            Assert.Equal("PropertyRef(string)", description);
        }

        [Fact]
        public void PropertyRefImpl_ToString_ShouldReturnCorrectString()
        {
            // Arrange
            var propertyRef = new PropertyRefImpl("string", new[] { Operator.Equals, Operator.Like });

            // Act
            var result = propertyRef.ToString();

            // Assert
            Assert.Contains("PropertyRef{Type=string", result);
            Assert.Contains("SupportedOperators=[Equals, Like]", result);
        }

        [Fact]
        public void PropertyRefImpl_Equals_SameType_ShouldReturnTrue()
        {
            // Arrange
            var propertyRef1 = new PropertyRefImpl("string", new[] { Operator.Equals });
            var propertyRef2 = new PropertyRefImpl("string", new[] { Operator.Like });

            // Act & Assert
            Assert.Equal(propertyRef1, propertyRef2);
        }

        [Fact]
        public void PropertyRefImpl_Equals_DifferentType_ShouldReturnFalse()
        {
            // Arrange
            var propertyRef1 = new PropertyRefImpl("string", new[] { Operator.Equals });
            var propertyRef2 = new PropertyRefImpl("int", new[] { Operator.Equals });

            // Act & Assert
            Assert.NotEqual(propertyRef1, propertyRef2);
        }

        [Fact]
        public void PropertyRefImpl_GetHashCode_SameType_ShouldReturnSameHashCode()
        {
            // Arrange
            var propertyRef1 = new PropertyRefImpl("string", new[] { Operator.Equals });
            var propertyRef2 = new PropertyRefImpl("string", new[] { Operator.Like });

            // Act & Assert
            Assert.Equal(propertyRef1.GetHashCode(), propertyRef2.GetHashCode());
        }
    }
}

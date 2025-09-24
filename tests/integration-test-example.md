# Tests d'Intégration - Exemples

## Test du Parseur DSL

### Test Java
```java
@Test
void testParseComplexExpression() throws DSLSyntaxException {
    DSLParser parser = new DSLParser();
    FilterTree tree = parser.parse("(f1 & f2) | !f3");
    assertNotNull(tree);
}

@Test
void testParseInvalidExpression() {
    DSLParser parser = new DSLParser();
    assertThrows(DSLSyntaxException.class, () -> parser.parse("(f1 & f2"));
}
```

### Test TypeScript
```typescript
describe('DSLParser', () => {
  test('should parse complex expression', () => {
    const parser = new DSLParser();
    const tree = parser.parse('(f1 & f2) | !f3');
    expect(tree).toBeDefined();
  });

  test('should throw error for invalid expression', () => {
    const parser = new DSLParser();
    expect(() => parser.parse('(f1 & f2')).toThrow(DSLSyntaxException);
  });
});
```

### Test Python
```python
def test_parse_complex_expression():
    parser = DSLParser()
    tree = parser.parse("(f1 & f2) | !f3")
    assert tree is not None

def test_parse_invalid_expression():
    parser = DSLParser()
    with pytest.raises(DSLSyntaxException):
        parser.parse("(f1 & f2")
```

## Test de Validation des Propriétés

### Test Java
```java
@Test
void testPropertyRegistry() {
    PropertyRegistry registry = new PropertyRegistry();
    registry.registerProperty("USER_NAME", String.class);
    
    assertTrue(registry.hasProperty("USER_NAME"));
    PropertyRef ref = registry.getProperty("USER_NAME");
    assertEquals(String.class, ref.getType());
}

@Test
void testInvalidProperty() {
    PropertyRegistry registry = new PropertyRegistry();
    assertFalse(registry.hasProperty("INVALID_PROPERTY"));
    assertNull(registry.getProperty("INVALID_PROPERTY"));
}
```

## Test des Adaptateurs

### Test JPA
```java
@Test
void testJpaConditionCombination() {
    // Setup
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    Predicate predicate1 = mock(Predicate.class);
    Predicate predicate2 = mock(Predicate.class);
    Predicate combinedPredicate = mock(Predicate.class);
    
    when(cb.and(predicate1, predicate2)).thenReturn(combinedPredicate);
    
    // Test
    JpaConditionAdapter condition1 = new JpaConditionAdapter(predicate1, cb);
    JpaConditionAdapter condition2 = new JpaConditionAdapter(predicate2, cb);
    
    Condition result = condition1.and(condition2);
    
    // Verify
    assertTrue(result instanceof JpaConditionAdapter);
    verify(cb).and(predicate1, predicate2);
}
```

### Test Prisma
```typescript
describe('PrismaConditionAdapter', () => {
  test('should combine conditions with AND', () => {
    const condition1 = new PrismaConditionAdapter({ name: 'John' });
    const condition2 = new PrismaConditionAdapter({ status: 'ACTIVE' });
    
    const result = condition1.and(condition2);
    
    expect(result.getWhereClause()).toEqual({
      AND: [{ name: 'John' }, { status: 'ACTIVE' }]
    });
  });
});
```

### Test Django
```python
def test_django_condition_combination():
    condition1 = DjangoCondition(Q(name='John'))
    condition2 = DjangoCondition(Q(status='ACTIVE'))
    
    result = condition1.and_(condition2)
    
    assert isinstance(result, DjangoCondition)
    assert result.get_q_object() == Q(name='John') & Q(status='ACTIVE')
```

## Test End-to-End

### Test Java - Service Complet
```java
@Test
void testCompleteFilteringFlow() {
    // Setup
    EntityManager em = createTestEntityManager();
    PropertyRegistry registry = createTestPropertyRegistry();
    DSLParser parser = new DSLParser();
    
    JpaFilterService service = new JpaFilterService(parser, registry, em);
    
    // Create test data
    createTestUsers(em);
    
    // Test
    FilterRequest request = new FilterRequest(
        Map.of(
            "f1", new FilterDefinition("USER_NAME", "LIKE", "Smith"),
            "f2", new FilterDefinition("STATUS", "=", "ACTIVE")
        ),
        "f1 & f2"
    );
    
    List<User> result = service.executeFilter(request, User.class);
    
    // Verify
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(u -> u.getUserName().contains("Smith")));
    assertTrue(result.stream().allMatch(u -> "ACTIVE".equals(u.getStatus())));
}
```

### Test TypeScript - Service Complet
```typescript
describe('PrismaFilterService', () => {
  test('should execute complete filtering flow', async () => {
    // Setup
    const prisma = new PrismaClient();
    const propertyRegistry = createTestPropertyRegistry();
    const parser = new DSLParser();
    
    const service = new PrismaFilterService(parser, propertyRegistry, prisma, 'User');
    
    // Create test data
    await createTestUsers(prisma);
    
    // Test
    const filterRequest = {
      filters: {
        f1: { ref: 'USER_NAME', operator: 'LIKE', value: 'Smith' },
        f2: { ref: 'STATUS', operator: '=', value: 'ACTIVE' }
      },
      combineWith: 'f1 & f2'
    };
    
    const result = await service.executeFilter(filterRequest, User);
    
    // Verify
    expect(result).toHaveLength(2);
    expect(result.every(u => u.userName.includes('Smith'))).toBe(true);
    expect(result.every(u => u.status === 'ACTIVE')).toBe(true);
  });
});
```

## Test de Performance

### Test Java
```java
@Test
void testPerformanceWithLargeDataset() {
    // Setup large dataset
    createLargeUserDataset(10000);
    
    // Measure execution time
    long startTime = System.currentTimeMillis();
    
    FilterRequest request = createComplexFilterRequest();
    List<User> result = filterService.executeFilter(request, User.class);
    
    long endTime = System.currentTimeMillis();
    long executionTime = endTime - startTime;
    
    // Verify performance (should complete within reasonable time)
    assertTrue(executionTime < 1000); // Less than 1 second
    assertTrue(result.size() > 0);
}
```

## Test de Sécurité

### Test Java
```java
@Test
void testSecurityWithInvalidProperty() {
    FilterRequest request = new FilterRequest(
        Map.of(
            "f1", new FilterDefinition("INVALID_PROPERTY", "=", "value")
        ),
        "f1"
    );
    
    assertThrows(IllegalArgumentException.class, () -> {
        filterService.executeFilter(request, User.class);
    });
}

@Test
void testSecurityWithInvalidOperator() {
    FilterRequest request = new FilterRequest(
        Map.of(
            "f1", new FilterDefinition("USER_NAME", "INVALID_OPERATOR", "value")
        ),
        "f1"
    );
    
    assertThrows(IllegalArgumentException.class, () -> {
        filterService.executeFilter(request, User.class);
    });
}
```

## Test de Conversion de Types

### Test Java
```java
@Test
void testTypeConversion() {
    FilterRequest request = new FilterRequest(
        Map.of(
            "f1", new FilterDefinition("AGE", ">=", "25"), // String to Integer
            "f2", new FilterDefinition("CREATED_DATE", ">=", "2024-01-01T00:00:00") // String to LocalDateTime
        ),
        "f1 & f2"
    );
    
    List<User> result = filterService.executeFilter(request, User.class);
    
    // Verify type conversion worked correctly
    assertTrue(result.stream().allMatch(u -> u.getAge() >= 25));
    assertTrue(result.stream().allMatch(u -> u.getCreatedDate().isAfter(LocalDateTime.of(2024, 1, 1, 0, 0))));
}
```

Ces tests couvrent tous les aspects critiques de l'architecture :
- Parsing DSL avec validation des erreurs
- Validation des propriétés et opérateurs
- Génération correcte des requêtes pour chaque adaptateur
- Performance avec de gros volumes de données
- Sécurité contre les injections
- Conversion de types automatique

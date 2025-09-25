/**
 * Example usage of TypeScript adapters with the new type-safe architecture
 */

import { FilterDefinition, FilterRequest } from '@cyfko/dynamic-filter-core';
import { Operator } from '@cyfko/dynamic-filter-core';
import { TestPropertyRef, TestPropertyRefImpl } from '../../core/typescript/src/test-types';

// TypeORM imports
import { TypeORMContextAdapter } from './typeorm/src/typeorm-context-adapter';
import { TypeORMConditionAdapterBuilder } from './typeorm/src/typeorm-condition-adapter-builder';
import { TypeORMConditionAdapter } from './typeorm/src/typeorm-condition-adapter';

// Prisma imports
import { PrismaContextAdapter } from './prisma/src/prisma-context-adapter';
import { PrismaConditionAdapterBuilder } from './prisma/src/prisma-condition-adapter-builder';
import { PrismaConditionAdapter } from './prisma/src/prisma-condition-adapter';

// Example entity type
interface User {
  id: number;
  name: string;
  age: number;
  email: string;
  status: string;
}

// Example TypeORM condition adapter builder implementation
class ExampleTypeORMConditionAdapterBuilder implements TypeORMConditionAdapterBuilder<User, typeof TestPropertyRefImpl> {
  build(ref: keyof typeof TestPropertyRefImpl, op: Operator, value: any): TypeORMConditionAdapter<User> {
    return new TypeORMConditionAdapter<User>((qb) => {
      const propertyName = String(ref).toLowerCase(); // Convert enum key to entity field
      
      switch (op) {
        case Operator.EQUALS:
          return qb.andWhere(`entity.${propertyName} = :${propertyName}`, { [propertyName]: value });
        case Operator.LIKE:
          return qb.andWhere(`entity.${propertyName} LIKE :${propertyName}`, { [propertyName]: value });
        case Operator.GREATER_THAN:
          return qb.andWhere(`entity.${propertyName} > :${propertyName}`, { [propertyName]: value });
        default:
          throw new Error(`Unsupported operator: ${op}`);
      }
    });
  }
}

// Example Prisma condition adapter builder implementation
class ExamplePrismaConditionAdapterBuilder implements PrismaConditionAdapterBuilder<User, typeof TestPropertyRefImpl> {
  build(ref: keyof typeof TestPropertyRefImpl, op: Operator, value: any): PrismaConditionAdapter<User> {
    const propertyName = String(ref).toLowerCase(); // Convert enum key to entity field
    
    switch (op) {
      case Operator.EQUALS:
        return new PrismaConditionAdapter<User>({ [propertyName]: { equals: value } });
      case Operator.LIKE:
        return new PrismaConditionAdapter<User>({ [propertyName]: { contains: value } });
      case Operator.GREATER_THAN:
        return new PrismaConditionAdapter<User>({ [propertyName]: { gt: value } });
      default:
        throw new Error(`Unsupported operator: ${op}`);
    }
  }
}

// Example usage
function demonstrateAdapters() {
  // Create filter definitions
  const nameFilter: FilterDefinition<typeof TestPropertyRefImpl> = {
    ref: TestPropertyRef.USER_NAME,
    operator: Operator.LIKE,
    value: "John%"
  };

  const ageFilter: FilterDefinition<typeof TestPropertyRefImpl> = {
    ref: TestPropertyRef.USER_AGE,
    operator: Operator.GREATER_THAN,
    value: 18
  };

  // TypeORM adapter usage
  const typeormBuilder = new ExampleTypeORMConditionAdapterBuilder();
  const typeormAdapter = new TypeORMContextAdapter<User, typeof TestPropertyRefImpl>(
    typeormBuilder, 
    TestPropertyRefImpl
  );

  typeormAdapter.addCondition("nameFilter", nameFilter);
  typeormAdapter.addCondition("ageFilter", ageFilter);

  // Prisma adapter usage
  const prismaBuilder = new ExamplePrismaConditionAdapterBuilder();
  const prismaAdapter = new PrismaContextAdapter<User, typeof TestPropertyRefImpl>(
    prismaBuilder,
    TestPropertyRefImpl
  );

  prismaAdapter.addCondition("nameFilter", nameFilter);
  prismaAdapter.addCondition("ageFilter", ageFilter);

  console.log("✅ TypeScript adapters validation successful!");
  
  return { typeormAdapter, prismaAdapter };
}

export { demonstrateAdapters, ExampleTypeORMConditionAdapterBuilder, ExamplePrismaConditionAdapterBuilder };


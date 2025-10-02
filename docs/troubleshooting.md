---
layout: default
title: Troubleshooting
---

# Troubleshooting

All solutions are based on actual codebase error handling and validation.

## Common Issues

### Invalid Operator for Property
**Symptom:** `FilterValidationException: Operator GT is not supported for property NAME...`
**Solution:** Check your property reference enum and ensure the operator is listed in `getSupportedOperators()`.

### Null Value for Non-Null Operator
**Symptom:** `FilterValidationException: Operator EQ requires a non-null value`
**Solution:** Provide a valid value for the filter definition.

### Unknown Filter Key
**Symptom:** `IllegalArgumentException: filterKey not found`
**Solution:** Ensure you add the filter definition to the context before referencing it.

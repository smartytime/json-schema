package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CombinedSchemaValidator extends SchemaValidator<CombinedSchema> {

    public CombinedSchemaValidator(CombinedSchema schema, SchemaValidatorFactory factory) {
        super(schema, factory);
    }

    public CombinedSchemaValidator(CombinedSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue toBeValidated) {
        Collection<Schema> subschemas = schema.getSubSchemas();
        List<ValidationError> failures = subschemas.stream()
                .map(schema ->
                        factory.createValidator(schema)
                                .validate(toBeValidated)
                                .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        int matchingCount = subschemas.size() - failures.size();
        int subschemaCount = subschemas.size();

        switch (schema.getCombinedSchemaType()) {
            case ANY_OF:
                if (matchingCount == 0) {
                    return buildKeywordFailure(toBeValidated, JsonSchemaKeyword.ANY_OF)
                            .message("no subschema matched out of the total %d subschemas", subschemaCount)
                            .causingExceptions(failures)
                            .buildOptional();
                }
                break;
            case ALL_OF:
                if (matchingCount < subschemaCount) {
                    return buildKeywordFailure(toBeValidated, JsonSchemaKeyword.ALL_OF)
                            .message("only %d subschema matches out of %d", matchingCount, subschemaCount)
                            .causingExceptions(failures)
                            .buildOptional();
                }
                break;
            case ONE_OF:
                if (matchingCount != 1) {
                    return buildKeywordFailure(toBeValidated, JsonSchemaKeyword.ONE_OF)
                            .message("%d subschemas matched instead of one", matchingCount)
                            .causingExceptions(failures)
                            .buildOptional();
                }
                break;
        }
        return Optional.empty();
    }
}

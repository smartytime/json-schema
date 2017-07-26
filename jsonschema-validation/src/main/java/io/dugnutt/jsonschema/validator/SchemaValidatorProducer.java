package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import java.util.Optional;

public interface SchemaValidatorProducer {
    Optional<PartialSchemaValidator> getValidatorIfApplicable(PathAwareJsonValue value, JsonSchema schema);
}

package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.SchemaKeywords;

import java.util.Optional;

public interface KeywordValidator<K extends SchemaKeywords> {
    Optional<ValidationError> validate(PathAwareJsonValue input, JsonSchema schema, K keywords, SchemaValidatorFactory factory);


}

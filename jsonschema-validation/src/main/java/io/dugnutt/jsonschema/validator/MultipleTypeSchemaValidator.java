package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.MultipleTypeSchema;
import io.dugnutt.jsonschema.utils.JsonUtils;

import javax.json.JsonValue;
import java.util.Optional;

public class MultipleTypeSchemaValidator extends SchemaValidator<MultipleTypeSchema> {

    public MultipleTypeSchemaValidator(MultipleTypeSchema schema) {
        super(schema);
    }

    @Override
    public Optional<ValidationError> validate(JsonValue subject) {
        JsonSchemaType inputType = JsonUtils.schemaTypeFor(subject);
        return this.schema.getSchemaForType(inputType)
                .map(schema -> context.getFactory().createValidator(schema).validate(subject))
                .orElseGet(()->{
                    if (schema.isRequireOne()) {
                        return Optional.of(failure("invalid.type", JsonSchemaKeyword.TYPE));
                    }
                    return Optional.empty();
                });

    }
}

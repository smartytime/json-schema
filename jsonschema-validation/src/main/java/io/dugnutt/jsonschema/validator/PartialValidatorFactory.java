package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface PartialValidatorFactory {
    default boolean appliesToSchema(Schema schema) {
        return true;
    }

    default Set<JsonValue.ValueType> appliesToTypes() {
        return new HashSet<>(Arrays.asList(JsonValue.ValueType.values()));
    }

    SchemaValidator forSchema(Schema schema, SchemaValidatorFactory factory);
}

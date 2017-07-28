package io.dugnutt.jsonschema.validator.builders;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;

import javax.json.JsonValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface KeywordValidatorBuilder {
    default boolean appliesToSchema(Schema schema) {
        return true;
    }

    default Set<JsonValue.ValueType> appliesToTypes() {
        return new HashSet<>(Arrays.asList(JsonValue.ValueType.values()));
    }

    KeywordValidators getKeywordValidators(Schema schema, SchemaValidatorFactory factory);
}

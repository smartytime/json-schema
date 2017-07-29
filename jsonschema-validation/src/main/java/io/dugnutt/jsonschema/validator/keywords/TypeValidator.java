package io.dugnutt.jsonschema.validator.keywords;

import com.google.common.collect.ImmutableSet;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.json.JsonValue;
import java.util.Set;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TYPE;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildTypeMismatchError;

public class TypeValidator extends KeywordValidator {

    @Singular
    @NonNull
    private final Set<JsonSchemaType> requiredTypes;
    private boolean requiresInteger;

    @Builder
    public TypeValidator(Schema schema, Set<JsonSchemaType> requiredTypes) {
        super(TYPE, schema);
        this.requiredTypes = ImmutableSet.copyOf(requiredTypes);
        this.requiresInteger = this.requiredTypes.contains(JsonSchemaType.INTEGER) &&
                !this.requiredTypes.contains(JsonSchemaType.NUMBER);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final JsonValue.ValueType valueType = subject.getValueType();
        final JsonSchemaType schemaType;
        if (valueType == JsonValue.ValueType.NUMBER && requiresInteger) {
            schemaType = subject.asJsonNumber().isIntegral() ? JsonSchemaType.INTEGER : JsonSchemaType.NUMBER;
        } else {
            schemaType = JsonUtils.schemaTypeFor(subject);
        }
        if (!requiredTypes.contains(schemaType)) {
            report.addError(buildTypeMismatchError(subject, schema, schema.getTypes()).build());
        }
        return report.isValid();
    }
}

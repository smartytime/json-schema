package io.sbsp.jsonschema.validator.keywords;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.TypeKeyword;
import io.sbsp.jsonschema.utils.JsonUtils;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.json.JsonValue;
import java.util.EnumSet;
import java.util.Set;

import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildTypeMismatchError;

public class TypeValidator extends KeywordValidator<TypeKeyword> {

    @Singular
    @NonNull
    private final EnumSet<JsonSchemaType> requiredTypes;
    private boolean requiresInteger;

    @Builder
    public TypeValidator(Schema schema, Set<JsonSchemaType> requiredTypes) {
        super(SchemaKeyword.type, schema);
        this.requiredTypes = EnumSet.copyOf(requiredTypes);
        this.requiresInteger = this.requiredTypes.contains(JsonSchemaType.INTEGER) &&
                !this.requiredTypes.contains(JsonSchemaType.NUMBER);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final JsonValue.ValueType valueType = subject.getValueType();
        final JsonSchemaType schemaType;
        if (requiresInteger && valueType == JsonValue.ValueType.NUMBER) {
            schemaType = subject.asJsonNumber().isIntegral() ? JsonSchemaType.INTEGER : JsonSchemaType.NUMBER;
        } else {
            schemaType = JsonUtils.schemaTypeFor(subject);
        }
        if (!requiredTypes.contains(schemaType)) {
            report.addError(buildTypeMismatchError(subject, schema, requiredTypes).build());
        }
        return report.isValid();
    }
}

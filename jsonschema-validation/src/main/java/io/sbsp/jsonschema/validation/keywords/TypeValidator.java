package io.sbsp.jsonschema.validation.keywords;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.TypeKeyword;
import io.sbsp.jsonschema.utils.JsonUtils;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;

import javax.json.JsonValue;
import java.util.Set;

import static io.sbsp.jsonschema.validation.ValidationErrorHelper.buildTypeMismatchError;

public class TypeValidator extends KeywordValidator<TypeKeyword> {

    private final Set<JsonSchemaType> requiredTypes;
    private boolean requiresInteger;

    public TypeValidator(TypeKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.TYPE, schema);
        this.requiredTypes = keyword.getTypes();
        this.requiresInteger = this.requiredTypes.contains(JsonSchemaType.INTEGER) &&
                !this.requiredTypes.contains(JsonSchemaType.NUMBER);
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
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

package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationError;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import io.sbsp.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;

import javax.json.JsonString;
import javax.json.spi.JsonProvider;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PROPERTY_NAMES;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class PropertyNameValidator extends KeywordValidator {

    @NonNull
    private final SchemaValidator propertyNameValidator;

    @NonNull
    private final JsonProvider jsonProvider;

    @Builder
    public PropertyNameValidator(Schema schema, SchemaValidator propertyNameValidator, JsonProvider jsonProvider) {
        super(JsonSchemaKeyword.PROPERTY_NAMES, schema);
        this.propertyNameValidator = checkNotNull(propertyNameValidator);
        this.jsonProvider = MoreObjects.firstNonNull(jsonProvider, JsonProvider.provider());
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {

            ValidationReport report = parentReport.createChildReport();
            final Set<String> subjectProperties = subject.asJsonObject().keySet();
            for (String subjectProperty : subjectProperties) {
                JsonString value = jsonProvider.createValue(subjectProperty);
                propertyNameValidator.validate(JsonValueWithLocation.fromJsonValue(value, subject.getLocation()), report);
            }

            List<ValidationError> errors = report.getErrors();
            if (!errors.isEmpty()) {
                parentReport.addError(buildKeywordFailure(subject, schema, PROPERTY_NAMES)
                        .message("Invalid property names")
                        .causingExceptions(errors)
                        .build());
            }
            return parentReport.isValid();

    }
}

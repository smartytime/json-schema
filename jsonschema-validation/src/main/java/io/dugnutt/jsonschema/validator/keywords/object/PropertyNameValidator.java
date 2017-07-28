package io.dugnutt.jsonschema.validator.keywords.object;

import com.google.common.base.MoreObjects;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationError;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;

import javax.json.JsonString;
import javax.json.spi.JsonProvider;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTY_NAMES;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class PropertyNameValidator extends KeywordValidator {

    @NonNull
    private final SchemaValidator propertyNameValidator;

    @NonNull
    @Builder.Default
    private final JsonProvider jsonProvider;

    @Builder
    public PropertyNameValidator(Schema schema, SchemaValidator propertyNameValidator, JsonProvider jsonProvider) {
        super(JsonSchemaKeyword.PROPERTY_NAMES, schema);
        this.propertyNameValidator = checkNotNull(propertyNameValidator);
        this.jsonProvider = MoreObjects.firstNonNull(jsonProvider, JsonProvider.provider());
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport parentReport) {

            ValidationReport report = new ValidationReport();
            final Set<String> subjectProperties = subject.asJsonObject().keySet();
            for (String subjectProperty : subjectProperties) {
                JsonString value = jsonProvider.createValue(subjectProperty);
                propertyNameValidator.validate(new PathAwareJsonValue(value, subject.getPath()), report);
            }

            List<ValidationError> errors = report.getErrors();
            if (!errors.isEmpty()) {
                return parentReport.addError(buildKeywordFailure(subject, schema, PROPERTY_NAMES)
                        .message("Invalid property names")
                        .causingExceptions(errors)
                        .build());
            }
            return true;

    }
}

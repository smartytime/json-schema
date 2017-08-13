package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationError;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import javax.json.JsonString;
import javax.json.spi.JsonProvider;
import java.util.List;
import java.util.Set;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PROPERTY_NAMES;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class PropertyNameValidator extends KeywordValidator<SingleSchemaKeyword> {

    private final SchemaValidator propertyNameValidator;
    private final JsonProvider jsonProvider;

    public PropertyNameValidator(SingleSchemaKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.propertyNames, schema);
        this.propertyNameValidator = factory.createValidator(keyword.getSchema());
        this.jsonProvider = MoreObjects.firstNonNull(factory.getProvider(), JsonProvider.provider());
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

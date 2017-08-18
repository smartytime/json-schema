package io.sbsp.jsonschema.validation.keywords.object;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationError;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import javax.json.JsonString;
import javax.json.spi.JsonProvider;
import java.util.List;
import java.util.Set;

import static io.sbsp.jsonschema.JsonValueWithPath.*;

public class PropertyNameValidator extends KeywordValidator<SingleSchemaKeyword> {

    private final SchemaValidator propertyNameValidator;
    private final JsonProvider jsonProvider;

    public PropertyNameValidator(SingleSchemaKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.PROPERTY_NAMES, schema);
        this.propertyNameValidator = factory.createValidator(keyword.getSchema());
        this.jsonProvider = MoreObjects.firstNonNull(factory.getJsonProvider(), JsonProvider.provider());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport parentReport) {

            ValidationReport report = parentReport.createChildReport();
            final Set<String> subjectProperties = subject.asJsonObject().keySet();
            for (String subjectProperty : subjectProperties) {
                JsonString value = jsonProvider.createValue(subjectProperty);
                propertyNameValidator.validate(fromJsonValue(subject.getRoot(), value, subject.getLocation()), report);
            }

            List<ValidationError> errors = report.getErrors();
            if (!errors.isEmpty()) {
                parentReport.addError(buildKeywordFailure(subject)
                        .message("Invalid property names")
                        .causingExceptions(errors)
                        .build());
            }
            return parentReport.isValid();

    }
}

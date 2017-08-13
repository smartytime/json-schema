package io.sbsp.jsonschema.validator.keywords.string;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import io.sbsp.jsonschema.validator.keywords.string.formatValidators.FormatValidator;

import java.util.Optional;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.FORMAT;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringFormatValidator extends KeywordValidator<StringKeyword> {

    private final FormatValidator formatValidator;

    public StringFormatValidator(StringKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.format, schema);
        this.formatValidator = factory.getFormatValidator(keyword.getKeywordValue()).orElse(null);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        if (formatValidator == null) {
            return true;
        }
        String stringSubject = subject.asString();
        Optional<String> error = formatValidator.validate(stringSubject);
        if (error.isPresent()) {
            report.addError(buildKeywordFailure(subject, schema, FORMAT)
                    .message(error.get())
                    .build());
        }
        return report.isValid();
    }
}

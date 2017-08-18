package io.sbsp.jsonschema.validation.keywords.string;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import io.sbsp.jsonschema.validation.FormatValidator;

import java.util.Optional;

public class StringFormatValidator extends KeywordValidator<StringKeyword> {

    private final FormatValidator formatValidator;

    public StringFormatValidator(StringKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.FORMAT, schema);
        this.formatValidator = factory.getFormatValidator(keyword.getKeywordValue()).orElse(null);
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        if (formatValidator == null) {
            return true;
        }
        String stringSubject = subject.asString();
        Optional<String> error = formatValidator.validate(stringSubject);
        if (error.isPresent()) {
            report.addError(buildKeywordFailure(subject)
                    .message(error.get())
                    .build());
        }
        return report.isValid();
    }
}

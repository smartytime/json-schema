package io.sbsp.jsonschema.validator.keywords.string;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.string.formatValidators.FormatValidator;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.FORMAT;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringFormatValidator extends KeywordValidator<StringKeyword> {

    @NonNull
    private final FormatValidator formatValidator;

    @Builder
    public StringFormatValidator(Schema schema, FormatValidator formatValidator) {
        super(SchemaKeyword.format, schema);
        this.formatValidator = checkNotNull(formatValidator);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
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

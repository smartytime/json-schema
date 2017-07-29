package io.dugnutt.jsonschema.validator.keywords.string;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.string.formatValidators.FormatValidator;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.FORMAT;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringFormatValidator extends KeywordValidator {

    @NonNull
    private final FormatValidator formatValidator;

    @Builder
    public StringFormatValidator(Schema schema, FormatValidator formatValidator) {
        super(FORMAT, schema);
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

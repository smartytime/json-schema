package io.sbsp.jsonschema.validator.keywords.number;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MULTIPLE_OF;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberMultipleOfValidator extends KeywordValidator {

    @NonNull
    private final BigDecimal multipleOf;

    @Builder
    public NumberMultipleOfValidator(Schema schema, Number multipleOf) {
        super(JsonSchemaKeyword.MULTIPLE_OF, schema);
        checkNotNull(multipleOf, "multipleOf must not be null");
        this.multipleOf = BigDecimal.valueOf(multipleOf.doubleValue());
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {

        final BigDecimal subjectDecimal = subject.asJsonNumber().bigDecimalValue();
        BigDecimal remainder = subjectDecimal.remainder(multipleOf);
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            report.addError(buildKeywordFailure(subject, schema, MULTIPLE_OF)
                    .message("Value is not a multiple of %s", multipleOf)
                    .build());
        }
        return report.isValid();
    }
}

package io.dugnutt.jsonschema.validator.keywords.number;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MULTIPLE_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

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
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {

        final BigDecimal subjectDecimal = subject.asJsonNumber().bigDecimalValue();
        BigDecimal remainder = subjectDecimal.remainder(multipleOf);
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            return report.addError(buildKeywordFailure(subject, schema, MULTIPLE_OF)
                    .message("Value is not a multiple of %s", multipleOf)
                    .build());
        }
        return true;
    }
}

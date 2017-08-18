package io.sbsp.jsonschema.validation.keywords.number;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

public class NumberMultipleOfValidator extends KeywordValidator<NumberKeyword> {

    @NonNull
    private final BigDecimal multipleOf;

    @Builder
    public NumberMultipleOfValidator(NumberKeyword numberKeyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.MULTIPLE_OF, schema);
        this.multipleOf = BigDecimal.valueOf(numberKeyword.getDouble());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {

        final BigDecimal subjectDecimal = subject.asJsonNumber().bigDecimalValue();
        BigDecimal remainder = subjectDecimal.remainder(multipleOf);
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            report.addError(buildKeywordFailure(subject)
                    .message("Value is not a multiple of %s", multipleOf)
                    .build());
        }
        return report.isValid();
    }
}

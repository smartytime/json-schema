package io.sbsp.jsonschema.validator.keywords.number;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MULTIPLE_OF;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class NumberMultipleOfValidator extends KeywordValidator<NumberKeyword> {

    @NonNull
    private final BigDecimal multipleOf;

    @Builder
    public NumberMultipleOfValidator(NumberKeyword numberKeyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.multipleOf, schema);
        this.multipleOf = BigDecimal.valueOf(numberKeyword.getDouble());
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

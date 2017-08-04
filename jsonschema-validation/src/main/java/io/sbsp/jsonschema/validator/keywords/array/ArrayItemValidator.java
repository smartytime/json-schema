package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import io.sbsp.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ITEMS;

public class ArrayItemValidator extends KeywordValidator {
    @NonNull
    private final SchemaValidator allItemValidator;

    @Builder
    public ArrayItemValidator(Schema schema, SchemaValidator allItemValidator) {
        super(ITEMS, schema);
        this.allItemValidator = checkNotNull(allItemValidator);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        AtomicBoolean success = new AtomicBoolean(true);
        subject.forEachIndex((idx, item)->{
            boolean valid = allItemValidator.validate(item, report);
            success.compareAndSet(true, valid);
        });
        return success.get();
    }
}

package io.dugnutt.jsonschema.validator.keywords.array;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ITEMS;

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

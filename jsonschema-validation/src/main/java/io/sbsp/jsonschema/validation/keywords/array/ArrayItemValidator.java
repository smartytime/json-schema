package io.sbsp.jsonschema.validation.keywords.array;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

public class ArrayItemValidator extends KeywordValidator<ItemsKeyword> {

    @NonNull
    private final SchemaValidator allItemValidator;

    @Builder
    public ArrayItemValidator(Schema parentSchema, SchemaValidator allItemValidator) {
        super(Keywords.ITEMS, parentSchema);
        this.allItemValidator = checkNotNull(allItemValidator);
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        AtomicBoolean success = new AtomicBoolean(true);
        subject.forEachIndex((idx, item)->{
            boolean valid = allItemValidator.validate(item, report);
            success.compareAndSet(true, valid);
        });
        return success.get();
    }
}

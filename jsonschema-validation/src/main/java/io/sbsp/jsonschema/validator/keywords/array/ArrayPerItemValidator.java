package io.sbsp.jsonschema.validator.keywords.array;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArrayPerItemValidator extends KeywordValidator<ItemsKeyword> {
    @NonNull
    private final ImmutableList<SchemaValidator> indexedValidators;

    @Nullable
    private final SchemaValidator additionalItemValidator;

    @Builder
    public ArrayPerItemValidator(Schema schema, List<SchemaValidator> indexedValidators, SchemaValidator additionalItemValidator) {
        super(Keywords.items, schema);
        this.indexedValidators = ImmutableList.copyOf(indexedValidators);
        this.additionalItemValidator = additionalItemValidator;
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        AtomicBoolean success = new AtomicBoolean(true);
        final int indexedValidatorCount = indexedValidators.size();
        subject.forEachIndex((idx, item) -> {
            final boolean valid;
            if (indexedValidatorCount > idx) {
                final SchemaValidator indexValidator = indexedValidators.get(idx);
                valid = indexValidator.validate(item, report);
                success.compareAndSet(true, valid);
            } else if (additionalItemValidator != null) {
                valid = additionalItemValidator.validate(item, report);
            } else {
                valid = true;
            }
            success.compareAndSet(true, valid);
        });
        return success.get();
    }
}

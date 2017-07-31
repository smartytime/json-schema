package io.dugnutt.jsonschema.validator.keywords.array;

import com.google.common.collect.ImmutableList;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.ITEMS;

public class ArrayPerItemValidator extends KeywordValidator {
    @NonNull
    private final ImmutableList<SchemaValidator> indexedValidators;

    @Nullable
    private final SchemaValidator additionalItemValidator;

    @Builder
    public ArrayPerItemValidator(Schema schema, List<SchemaValidator> indexedValidators, SchemaValidator additionalItemValidator) {
        super(ITEMS, schema);
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

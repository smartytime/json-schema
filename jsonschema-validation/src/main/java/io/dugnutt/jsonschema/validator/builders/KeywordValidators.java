package io.dugnutt.jsonschema.validator.builders;

import com.google.common.collect.ImmutableList;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

@Getter
public class KeywordValidators implements Iterable<SchemaValidator> {

    @NonNull
    @Singular
    private final List<SchemaValidator> validators;

    @NonNull
    private final Schema schema;

    @NonNull
    private final SchemaValidatorFactory validatorFactory;

    @Builder
    public KeywordValidators(List<SchemaValidator> validators, Schema schema, SchemaValidatorFactory validatorFactory) {
        this.validators = ImmutableList.copyOf(validators);
        this.schema = schema;
        this.validatorFactory = validatorFactory;
    }

    @Override
    public Iterator<SchemaValidator> iterator() {
        return validators.iterator();
    }

    @Override
    public void forEach(Consumer<? super SchemaValidator> action) {
        validators.forEach(action);
    }

    @Override
    public Spliterator<SchemaValidator> spliterator() {
        return validators.spliterator();
    }

    public static KeywordValidatorsBuilder builder() {
        return new KeywordValidatorsBuilder();
    }

    public static class KeywordValidatorsBuilder {
        private List<SchemaValidator> validators = new ArrayList<>();

        private KeywordValidatorsBuilder() {
        }

        public KeywordValidatorsBuilder addValidator(SchemaValidator validator) {
            this.validators.add(validator);
            return this;
        }

        public KeywordValidators build() {
            return new KeywordValidators(validators, schema, validatorFactory);
        }
    }
}

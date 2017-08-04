package io.sbsp.jsonschema.validator.extractors;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
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
public class KeywordValidators implements Iterable<KeywordValidator> {

    @NonNull
    @Singular
    private final List<KeywordValidator> validators;

    @NonNull
    private final Schema schema;

    @NonNull
    private final SchemaValidatorFactory validatorFactory;

    @Builder
    public KeywordValidators(List<KeywordValidator> validators, Schema schema, SchemaValidatorFactory validatorFactory) {
        this.validators = ImmutableList.copyOf(validators);
        this.schema = schema;
        this.validatorFactory = validatorFactory;
    }

    @Override
    public Iterator<KeywordValidator> iterator() {
        return validators.iterator();
    }

    @Override
    public void forEach(Consumer<? super KeywordValidator> action) {
        validators.forEach(action);
    }

    @Override
    public Spliterator<KeywordValidator> spliterator() {
        return validators.spliterator();
    }

    public static KeywordValidatorsBuilder builder() {
        return new KeywordValidatorsBuilder();
    }

    public static class KeywordValidatorsBuilder {
        private List<KeywordValidator> validators = new ArrayList<>();

        private KeywordValidatorsBuilder() {
        }

        public KeywordValidatorsBuilder addValidator(KeywordValidator validator) {
            this.validators.add(validator);
            return this;
        }

        public KeywordValidators build() {
            return new KeywordValidators(validators, schema, validatorFactory);
        }
    }
}

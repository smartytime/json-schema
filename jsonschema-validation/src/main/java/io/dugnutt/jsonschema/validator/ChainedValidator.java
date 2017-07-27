package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
public class ChainedValidator implements SchemaValidator {

    @NonNull
    private Schema schema;

    @NonNull
    private SchemaValidatorFactory factory;

    @NonNull
    @Singular
    List<SchemaValidator> validators;

    public static ChainedValidatorBuilder builder() {
        return new ChainedValidatorBuilder();
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        boolean success= true;
        for (SchemaValidator validator : validators) {
            success = success && validator.validate(subject, report);
            if (validator instanceof NamedSchemaValidator) {
                System.out.println("VALIDATOR: " + validator.toString());
            }
        }
        return success;
    }

    public static class ChainedValidatorBuilder {
        private ChainedValidatorBuilder() {
        }

        private final List<SchemaValidator> validators = new ArrayList<>();
        private Schema schema;
        private SchemaValidatorFactory factory = SchemaValidatorFactory.DEFAULT_VALIDATOR_FACTORY;

        // public ChainedValidatorBuilder addValidator(SchemaValidator validator) {
        //     checkNotNull(validator, "validator must not be null");
        //     this.validators.add(validator);
        //     return this;
        // }

        public ChainedValidatorBuilder addValidator(JsonSchemaKeyword keyword, SchemaValidator validator) {
            checkNotNull(validator, "validator must not be null");
            checkNotNull(keyword, "keyword must not be null");
            final String name;
            if (schema != null) {
                name = schema.getLocation().getJsonPointerFragment() + " -> (" +  keyword + ") keyword: " + schema.toString();
            } else {
                name = "#? -> " + keyword;
            }
            this.validators.add(NamedSchemaValidator.builder()
                    .name(name)
                    .wrapped(validator)
                    .build());
            return this;
        }

        public ChainedValidatorBuilder schema(Schema schema) {
            this.schema = schema;
            return this;
        }

        public ChainedValidatorBuilder factory(SchemaValidatorFactory factory) {
            this.factory = factory;
            return this;
        }

        public SchemaValidator build() {
            if (validators.size() == 0) {
                return NOOP_VALIDATOR;
            } else {
                return new ChainedValidator(this.schema, factory, validators);
            }
        }
    }
}

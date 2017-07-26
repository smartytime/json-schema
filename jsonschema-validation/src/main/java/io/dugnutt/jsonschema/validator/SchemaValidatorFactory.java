package io.dugnutt.jsonschema.validator;

import com.google.common.base.Strings;
import io.dugnutt.jsonschema.six.FormatType;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.StreamUtils;
import io.dugnutt.jsonschema.validator.formatValidators.FormatValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.json.spi.JsonProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.dugnutt.jsonschema.validator.ArrayKeywordsValidator.arrayKeywordsValidator;
import static io.dugnutt.jsonschema.validator.NumberKeywordsValidator.numberKeywordsValidator;
import static io.dugnutt.jsonschema.validator.ObjectKeywordsValidator.objectKeywordsValidator;
import static io.dugnutt.jsonschema.validator.StringKeywordsValidator.stringKeywordsValidator;

@Builder(builderClassName = "Builder", toBuilder = true)
public class SchemaValidatorFactory {

    static final SchemaValidatorFactory DEFAULT_VALIDATOR = builder().build();

    @NonNull
    private final Map<String, FormatValidator> customFormatValidators;

    @NonNull
    @Singular
    private final List<PartialSchemaValidator> validators;

    private final JsonProvider provider;

    public static JsonSchemaValidator createValidatorForSchema(JsonSchema schema) {
        return DEFAULT_VALIDATOR.createValidator(schema);
    }

    public JsonSchemaValidator createValidator(JsonSchema schema) {
        checkNotNull(schema, "schema must not be null when creating validator");
        List<PartialSchemaValidator> applicableValidators = validators.stream()
                .filter(validator -> validator.appliesToSchema(schema))
                .collect(StreamUtils.toImmutableList());
        checkState(applicableValidators.size() > 0, "Need at least one validator");

        return JsonSchemaValidator.jsonSchemaValidator()
                .factory(this)
                .schema(schema)
                .childValidators(applicableValidators)
                .build();
    }

    public Optional<FormatValidator> getFormatValidator(String input) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(customFormatValidators.get(input));
    }

    public JsonProvider getProvider() {
        return provider;
    }

    @FunctionalInterface
    interface Factory {
        PartialSchemaValidator createValidator(JsonSchema schema, SchemaValidatorFactory factory);
    }

    public static class Builder {
        public Builder() {
            this.provider = JsonProvider.provider();
            this.customFormatValidators = new HashMap<>();
            this.validators = new ArrayList<>();
            initCoreSchemaValidators();
            initCoreFormatValidators();
        }

        public Builder customFormatValidator(String format, FormatValidator formatValidator) {
            checkArgument(!Strings.isNullOrEmpty(format), "format must not be blank");
            checkNotNull(formatValidator, "formatValidator must not be null");
            customFormatValidators.put(format, formatValidator);
            return this;
        }

        private void initCoreSchemaValidators() {
            // ##########################################
            // BASE VALIDATOR
            // ##########################################
            validator(BaseSchemaValidator.baseSchemaValidator());

            // ##########################################
            // KEYWORD VALIDATORS
            // ##########################################
            validator(stringKeywordsValidator());
            validator(numberKeywordsValidator());
            validator(arrayKeywordsValidator());
            validator(objectKeywordsValidator());
        }

        private void initCoreFormatValidators() {
            for (FormatType formatType : FormatType.values()) {
                customFormatValidators.put(formatType.toString(), FormatValidator.forFormat(formatType));
            }
        }
    }
}

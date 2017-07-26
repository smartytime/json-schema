package io.dugnutt.jsonschema.validator;

import com.google.common.base.Strings;
import io.dugnutt.jsonschema.six.FormatType;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.formatValidators.FormatValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.validator.ArrayKeywordsValidator.arrayKeywordsValidator;
import static io.dugnutt.jsonschema.validator.NumberKeywordsValidator.numberKeywordsValidator;
import static io.dugnutt.jsonschema.validator.ObjectKeywordsValidator.objectKeywordsValidator;
import static io.dugnutt.jsonschema.validator.StringKeywordsValidator.stringKeywordsValidator;

@Builder(builderClassName = "Builder", toBuilder = true)
public class SchemaValidatorFactory {

    public static final SchemaValidatorFactory DEFAULT_VALIDATOR = builder().build();

    private final Map<URI, JsonSchemaValidator> validatorCache = new HashMap<>();

    private final Map<PartialValidatorKey, PartialSchemaValidator> partialValidatorCache = new HashMap<>();

    @NonNull
    private final Map<String, FormatValidator> customFormatValidators;

    @NonNull
    @Singular
    private final List<PartialSchemaValidator> validators;

    private final JsonProvider provider;

    public void cacheSchema(URI absoluteURI, JsonSchemaValidator validator) {
        validatorCache.putIfAbsent(absoluteURI, validator);
    }

    public static JsonSchemaValidator createValidatorForSchema(Schema schema) {
        return DEFAULT_VALIDATOR.createValidator(schema);
    }

    void cacheValidator(URI schemaURI, JsonSchemaValidator validator) {
        validatorCache.putIfAbsent(schemaURI, validator);
    }

    public JsonSchemaValidator createValidator(Schema schema) {
        checkNotNull(schema, "schema must not be null when creating validator");
        final URI schemaURI = schema.getLocation().getAbsoluteURI();
        final JsonSchemaValidator cachedValue = validatorCache.get(schemaURI);
        if (cachedValue != null) {
            return cachedValue;
        } else {
            final JsonSchemaValidator validator = JsonSchemaValidator.jsonSchemaValidator()
                    .factory(this)
                    .schema(schema)
                    .childValidators(validators)
                    .build();
            this.cacheValidator(schemaURI, validator);
            return validator;
        }
    }

    PartialSchemaValidator createPartialValidator(PartialSchemaValidator existing, Schema schema) {
        final PartialValidatorKey key = new PartialValidatorKey(schema.getLocation().getAbsoluteURI(), existing.getClass().getSimpleName());
        return partialValidatorCache.computeIfAbsent(key, k -> existing.forSchema(schema, this));
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
        PartialSchemaValidator createValidator(Schema schema, SchemaValidatorFactory factory);
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

    @Value
    static class PartialValidatorKey {
        URI uri;
        String partialValidatorType;
    }
}

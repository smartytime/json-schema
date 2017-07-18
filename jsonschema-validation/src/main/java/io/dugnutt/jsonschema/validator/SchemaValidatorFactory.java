package io.dugnutt.jsonschema.validator;

import com.google.common.base.Strings;
import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.EnumSchema;
import io.dugnutt.jsonschema.six.MultipleTypeSchema;
import io.dugnutt.jsonschema.six.NotSchema;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.NumberSchema;
import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StringSchema;
import io.dugnutt.jsonschema.validator.formatValidators.FormatValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Builder(builderClassName = "Builder", toBuilder = true)
public class SchemaValidatorFactory {

    static final SchemaValidatorFactory DEFAULT_VALIDATOR = builder().build();

    @NonNull
    private final Map<String, FormatValidator> customFormatValidators;

    @NonNull
    private final Map<Class<Schema>, Function<Schema, SchemaValidator<Schema>>> schemaValidators;

    public <S extends Schema> SchemaValidator<S> createValidator(S schema) {
        checkNotNull(schema, "schema must not be null");
        final Function<Schema, SchemaValidator<Schema>> validatorFunction = schemaValidators.get(schema.getClass());
        if (validatorFunction == null) {
            throw new IllegalArgumentException("Unable to locate validator for schema: " + schema.getClass());
        }
        return (SchemaValidator<S>) validatorFunction.apply(schema);
    }

    public static SchemaValidator<?> createValidatorForSchema(Schema schema) {
        return DEFAULT_VALIDATOR.createValidator(schema);
    }

    public static class Builder {
        public Builder() {
            this.customFormatValidators = new HashMap<>();
            this.schemaValidators = new HashMap<>();
            initCoreSchemaValidators();
        }

        public Builder customFormatValidator(String format, FormatValidator formatValidator) {
            checkArgument(!Strings.isNullOrEmpty(format), "format must not be blank");
            checkNotNull(formatValidator, "formatValidator must not be null");
            customFormatValidators.put(format, formatValidator);
            return this;
        }

        public <X extends Schema> Builder schemaValidator(Class<X> schemaClass, Function<X, SchemaValidator<X>> factory) {
            //todo:ericm What the heck???
            Class<Schema> classOfSchema = (Class<Schema>) schemaClass;
            this.schemaValidators.put(classOfSchema, schema -> {
                SchemaValidator<X> validator = factory.apply((X) schema);
                return (SchemaValidator<Schema>) validator;
            });
            return this;
        }

        private void initCoreSchemaValidators() {
            schemaValidator(ObjectSchema.class, ObjectSchemaValidator::new);
            schemaValidator(ArraySchema.class, ArraySchemaValidator::new);
            schemaValidator(BooleanSchema.class, BooleanSchemaValidator::new);
            schemaValidator(CombinedSchema.class, CombinedSchemaValidator::new);
            schemaValidator(EmptySchema.class, EmptySchemaValidator::new);
            schemaValidator(EnumSchema.class, EnumSchemaValidator::new);
            schemaValidator(NotSchema.class, NotSchemaValidator::new);
            schemaValidator(NullSchema.class, NullSchemaValidator::new);
            schemaValidator(NumberSchema.class, NumberSchemaValidator::new);
            schemaValidator(ReferenceSchema.class, ReferenceSchemaValidator::new);
            schemaValidator(StringSchema.class, StringSchemaValidator::new);
            schemaValidator(MultipleTypeSchema.class, MultipleTypeSchemaValidator::new);
        }
    }
}

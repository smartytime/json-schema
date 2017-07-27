package io.dugnutt.jsonschema.validator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Builder(builderMethodName = "jsonSchemaValidator")
public class JsonSchemaValidator implements SchemaValidator {
    @NonNull
    private final Multimap<JsonValue.ValueType, SchemaValidator> childValidators;

    @NonNull
    @Singular
    private final List<PartialValidatorFactory> factories;

    @NonNull
    private final Schema schema;

    @NonNull
    @Builder.Default
    private final JsonProvider provider;

    @NotNull
    private final SchemaValidatorFactory validatorFactory;

    public JsonSchemaValidator(Multimap<JsonValue.ValueType, SchemaValidator> childValidators, List<PartialValidatorFactory> factories, Schema schema, JsonProvider provider, @NotNull SchemaValidatorFactory validatorFactory) {
        this.schema = schema;
        this.provider = MoreObjects.firstNonNull(provider, JsonProvider.provider());
        this.factories = factories;
        this.validatorFactory = validatorFactory;
        this.validatorFactory.cacheSchema(schema.getLocation().getAbsoluteURI(), this);
        ImmutableMultimap.Builder<JsonValue.ValueType, SchemaValidator> builder = ImmutableMultimap.builder();
        factories.stream()
                .filter(validator -> validator.appliesToSchema(schema))
                .forEach(f -> {
                    SchemaValidator schemaValidator = f.forSchema(schema, validatorFactory);
                    if (schemaValidator instanceof ChainedValidator) {
                        ((ChainedValidator) schemaValidator).validators.forEach(validator->{
                            f.appliesToTypes().forEach(type-> builder.put(type, validator));
                        });
                    } else if(schemaValidator != SchemaValidator.NOOP_VALIDATOR) {
                        f.appliesToTypes().forEach(type-> builder.put(type, schemaValidator));
                    }
                });
        this.childValidators = builder.build();
    }

    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        JsonValue.ValueType valueType = subject.getValueType();
        boolean success = true;
        for (SchemaValidator schemaValidator : childValidators.get(valueType)) {
            success = success && schemaValidator.validate(subject, report);
            report.log(schemaValidator);
        }
        return success;
    }

    public Optional<ValidationError> validate(JsonValue subject) {
        PathAwareJsonValue pathAwareSubject = new PathAwareJsonValue(subject, schema.getLocation().getJsonPath());
        return validate(pathAwareSubject);
    }

    @Deprecated
    @Override
    public Optional<ValidationError> validate(PathAwareJsonValue subject) {
        ValidationReport report = new ValidationReport();
        validate(subject, report);
        return ValidationError.collectErrors(schema, subject.getPath(), report.getErrors());
    }

    @VisibleForTesting
    public Schema schema() {
        return schema;
    }
}

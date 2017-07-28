package io.dugnutt.jsonschema.validator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.builders.KeywordValidatorBuilder;
import io.dugnutt.jsonschema.validator.keywords.TypeValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.annotation.Nullable;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonSchemaValidator implements SchemaValidator {
    @NonNull
    private final Multimap<JsonValue.ValueType, SchemaValidator> childValidators;

    @Nullable
    private final TypeValidator typeValidator;

    @NonNull
    private final Schema schema;

    @NonNull
    @Builder.Default
    private final JsonProvider provider;

    @NotNull
    private final SchemaValidatorFactory validatorFactory;

    private final boolean noop;

    @Builder(builderMethodName = "jsonSchemaValidator")
    public JsonSchemaValidator(@Singular List<KeywordValidatorBuilder> factories, Schema schema, JsonProvider provider,
                               SchemaValidatorFactory validatorFactory) {
        checkNotNull(factories, "factories must not be null");
        this.schema = schema;
        this.provider = MoreObjects.firstNonNull(provider, JsonProvider.provider());
        this.validatorFactory = validatorFactory;
        ImmutableMultimap.Builder<JsonValue.ValueType, SchemaValidator> validatorsByType = ImmutableMultimap.builder();

        if (schema.getTypes().size() > 0) {
            typeValidator = TypeValidator.builder()
                    .schema(schema)
                    .requiredTypes(schema.getTypes())
                    .build();
        } else {
            typeValidator = null;
        }

        this.validatorFactory.cacheValidator(schema.getAbsoluteURI(), this);
        factories.stream()
                .filter(validator -> validator.appliesToSchema(schema))
                .forEach(f -> {
                    f.getKeywordValidators(schema, validatorFactory).forEach(validator->{
                        f.appliesToTypes().forEach(type -> validatorsByType.put(type, validator));
                    });
                });
        this.childValidators = validatorsByType.build();
        this.noop = this.childValidators.isEmpty() && this.typeValidator == null;
    }

    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        if (noop) {
            return true;
        }
        JsonValue.ValueType valueType = subject.getValueType();
        ValidationReport thisReport = new ValidationReport();

        boolean success = true;
        if (typeValidator != null) {
            final boolean typeValid = typeValidator.validate(subject, report);
            success = typeValid;
        }

        final Collection<SchemaValidator> validatorsForType = childValidators.get(valueType);
        for (SchemaValidator schemaValidator : validatorsForType) {
            boolean thisSuccess = schemaValidator.validate(subject, thisReport);
            success = success && thisSuccess;
        }
        report.addReport(schema, subject, thisReport);
        return success;
    }

    @VisibleForTesting
    public Schema getSchema() {
        return schema;
    }

    public static class JsonSchemaValidatorBuilder {
        public JsonSchemaValidator build() {
            return new JsonSchemaValidator(this.factories, this.schema, this.provider, this.validatorFactory);
        }
    }
}

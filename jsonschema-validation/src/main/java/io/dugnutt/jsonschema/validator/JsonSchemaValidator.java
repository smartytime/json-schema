package io.dugnutt.jsonschema.validator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StreamUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

@Builder(builderMethodName = "jsonSchemaValidator")
public class JsonSchemaValidator {
    @NonNull
    @Singular
    private final List<PartialSchemaValidator> childValidators;

    @NonNull
    private final Schema schema;

    @NonNull
    @Builder.Default
    private final JsonProvider provider;

    @NotNull
    private final SchemaValidatorFactory factory;

    public JsonSchemaValidator(List<PartialSchemaValidator> validators, Schema schema, JsonProvider provider, @NotNull SchemaValidatorFactory factory) {
        this.schema = schema;
        this.provider = MoreObjects.firstNonNull(provider, JsonProvider.provider());
        this.factory = factory;
        this.factory.cacheSchema(schema.getLocation().getAbsoluteURI(), this);
        this.childValidators = validators.stream()
                .filter(validator -> validator.appliesToSchema(schema))
                .map(validator -> factory.createPartialValidator(validator, schema))
                .collect(StreamUtils.toImmutableList());

        checkState(childValidators.size() > 0, "Need at least one validator");
    }

    public Optional<ValidationError> validate(PathAwareJsonValue subject) {
        List<ValidationError> errors = new ArrayList<>();
        for (PartialSchemaValidator childValidator : childValidators) {
            if (childValidator.appliesToValue(subject)) {
                childValidator.validate(subject, schema, factory).ifPresent(errors::add);
            }
        }
        return ValidationError.collectErrors(schema, subject.getPath(), errors);
    }

    public Optional<ValidationError> validate(JsonValue subject) {
        PathAwareJsonValue pathAwareSubject = new PathAwareJsonValue(subject, schema.getLocation().getJsonPath());
        return validate(pathAwareSubject);
    }

    @VisibleForTesting
    Schema schema() {
        return schema;
    }
}

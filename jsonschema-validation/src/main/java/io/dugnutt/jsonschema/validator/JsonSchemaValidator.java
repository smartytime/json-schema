package io.dugnutt.jsonschema.validator;

import com.google.common.annotations.VisibleForTesting;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.StreamUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Builder(builderMethodName = "jsonSchemaValidator")
public class JsonSchemaValidator {
    @NonNull
    @Singular
    private final List<PartialSchemaValidator> childValidators;

    @NonNull
    private final JsonSchema schema;

    @NonNull
    @Builder.Default
    private final JsonProvider provider = JsonProvider.provider();

    @NotNull
    private final SchemaValidatorFactory factory;

    public Optional<ValidationError> validate(PathAwareJsonValue subject) {
        List<ValidationError> allErrors = childValidators.stream()
                .filter(val -> val.appliesToValue(subject))
                .map(val -> val.validate(subject, schema, factory))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(StreamUtils.toImmutableList());

        return ValidationError.collectErrors(schema, subject.getPath(), allErrors);
    }

    public Optional<ValidationError> validate(JsonValue subject) {
        PathAwareJsonValue pathAwareSubject = new PathAwareJsonValue(subject, schema.getLocation().getJsonPath());
        return validate(pathAwareSubject);
    }

    @VisibleForTesting
    JsonSchema schema() {
        return schema;
    }
}

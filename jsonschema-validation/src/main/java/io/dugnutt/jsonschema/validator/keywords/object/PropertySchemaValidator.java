package io.dugnutt.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableMap;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
@EqualsAndHashCode
public class PropertySchemaValidator implements SchemaValidator {
    private final ImmutableMap<String, SchemaValidator> propertyValidators;
    private final Schema schema;

    public PropertySchemaValidator(ImmutableMap<String, SchemaValidator> propertyValidators, Schema schema) {
        this.propertyValidators = checkNotNull(propertyValidators);
        this.schema = checkNotNull(schema);
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport report) {
        final Set<String> subjectProperties = subject.asJsonObject().keySet();
        boolean success = true;
        for (Map.Entry<String, SchemaValidator> propertySchemas : propertyValidators.entrySet()) {
            final String propertyName = propertySchemas.getKey();
            if (subjectProperties.contains(propertyName)) {
                SchemaValidator propValidator = propertySchemas.getValue();
                PathAwareJsonValue pathAwareSubject = subject.getPathAware(propertyName);
                final boolean validated = propValidator.validate(pathAwareSubject, report);
                success = success && validated;
            }
        }
        return success;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }
}

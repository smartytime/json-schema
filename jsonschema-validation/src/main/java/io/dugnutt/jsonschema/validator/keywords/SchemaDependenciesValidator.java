package io.dugnutt.jsonschema.validator.keywords;

import com.google.common.collect.ImmutableMap;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;

public class SchemaDependenciesValidator extends KeywordValidator {

    @NonNull
    private final Map<String, SchemaValidator> dependencyValidators;

    @Builder
    public SchemaDependenciesValidator(Schema schema, Map<String, SchemaValidator> dependencyValidators) {
        super(JsonSchemaKeyword.DEPENDENCIES, schema);
        this.dependencyValidators = ImmutableMap.copyOf(dependencyValidators);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        boolean success = true;
        for (Map.Entry<String, SchemaValidator> dependencyValidatorEntries : dependencyValidators.entrySet()) {
            String propName = dependencyValidatorEntries.getKey();
            SchemaValidator dependencyValidator = dependencyValidatorEntries.getValue();
            if (subject.containsKey(propName)) {
                boolean validated = dependencyValidator.validate(subject, report);
                success = success && validated;
            }
        }
        return success;
    }
}

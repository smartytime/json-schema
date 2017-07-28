package io.dugnutt.jsonschema.validator.keywords;

import com.google.common.collect.ImmutableSet;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;

import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;

public class AdditionalPropertiesValidator extends KeywordValidator {

    @NonNull
    private final SchemaValidator additionalPropertiesValidator;

    @NonNull
    private final Set<String> propertySchemaKeys;

    @NonNull
    private final Set<Pattern> patternProperties;

    @Builder
    public AdditionalPropertiesValidator(Schema schema, SchemaValidator additionalPropertiesValidator, Set<String> propertySchemaKeys, Set<Pattern> patternProperties) {
        super(ADDITIONAL_PROPERTIES, schema);
        this.additionalPropertiesValidator = checkNotNull(additionalPropertiesValidator);
        this.propertySchemaKeys = ImmutableSet.copyOf(propertySchemaKeys);
        this.patternProperties = ImmutableSet.copyOf(patternProperties);
    }

    @Override
    public boolean validate(PathAwareJsonValue subject, ValidationReport parentReport) {
        ValidationReport report = new ValidationReport();

        boolean success = true;
        for (String propertyName : getAdditionalProperties(subject)) {
            PathAwareJsonValue propertyValue = subject.getPathAware(propertyName);
            boolean valid = additionalPropertiesValidator.validate(propertyValue, report);
            success = success && valid;
        }
        if (!success) {
            parentReport.addReport(schema, subject, ADDITIONAL_PROPERTIES, "Additional properties were invalid", report);
        }
        return success;
    }

    Set<String> getAdditionalProperties(final PathAwareJsonValue subject) {
        ImmutableSet.Builder<String> additionalProps = ImmutableSet.builder();
        prop: for (String propName : subject.propertyNames()) {
            for (Pattern pattern : patternProperties) {
                if(pattern.matcher(propName).find()) {
                    continue prop;
                }
            }
            if (!propertySchemaKeys.contains(propName)) {
                additionalProps.add(propName);
            }
        }
        return additionalProps.build();
    }
}

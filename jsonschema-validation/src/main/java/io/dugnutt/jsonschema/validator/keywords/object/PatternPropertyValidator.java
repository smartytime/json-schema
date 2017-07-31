package io.dugnutt.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableMap;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.PATTERN_PROPERTIES;

public class PatternPropertyValidator extends KeywordValidator {

    @NonNull
    @Singular
    private final Map<Pattern, SchemaValidator> patternValidators;

    @Builder
    public PatternPropertyValidator(Schema schema, Map<Pattern, SchemaValidator> patternValidators) {
        super(PATTERN_PROPERTIES, schema);
        this.patternValidators = ImmutableMap.copyOf(patternValidators);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        Set<String> subjectProperties = subject.propertyNames();
        if (subjectProperties.isEmpty()) {
            return true;
        }
        boolean success = true;
        ValidationReport report = parentReport.createChildReport();
        for (Map.Entry<Pattern, SchemaValidator> patternValidatorEntries : patternValidators.entrySet()) {
            Pattern pattern = patternValidatorEntries.getKey();
            SchemaValidator patternValidator = patternValidatorEntries.getValue();
            for (String propertyName : subjectProperties) {
                if (pattern.matcher(propertyName).matches()) {
                    final JsonValueWithLocation propertyValue = subject.getPathAwareObject(propertyName);
                    success = success && patternValidator.validate(propertyValue, report);
                }
            }
        }
        return parentReport.addReport(schema, subject, report);
    }
}

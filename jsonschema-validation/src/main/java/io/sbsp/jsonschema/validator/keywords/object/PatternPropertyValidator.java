package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import io.sbsp.jsonschema.validator.SchemaValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PATTERN_PROPERTIES;

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
                if (pattern.matcher(propertyName).find()) {
                    final JsonValueWithLocation propertyValue = subject.getPathAwareObject(propertyName);
                    success = success && patternValidator.validate(propertyValue, report);
                }
            }
        }
        return parentReport.addReport(schema, subject, report);
    }
}

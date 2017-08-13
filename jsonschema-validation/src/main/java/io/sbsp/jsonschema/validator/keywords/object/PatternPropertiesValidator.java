package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PatternPropertiesValidator extends KeywordValidator<SchemaMapKeyword> {

    private final List<PatternPropertyValidator> patternValidators;

    public PatternPropertiesValidator(SchemaMapKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.patternProperties, schema);
        this.patternValidators = keyword.getSchemas().entrySet().stream()
                .map(entry -> {
                    final Pattern pattern = Pattern.compile(entry.getKey());
                    final SchemaValidator validator = factory.createValidator(entry.getValue());
                    return new PatternPropertyValidator(pattern, validator);
                })
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        Set<String> subjectProperties = subject.propertyNames();
        if (subjectProperties.isEmpty()) {
            return true;
        }
        boolean success = true;
        ValidationReport report = parentReport.createChildReport();
        for (PatternPropertyValidator patternValidator : patternValidators) {
            Pattern pattern = patternValidator.pattern;
            final SchemaValidator validator = patternValidator.validator;
            for (String propertyName : subjectProperties) {
                if (pattern.matcher(propertyName).find()) {
                    final JsonValueWithLocation propertyValue = subject.getPathAwareObject(propertyName);
                    success = success && validator.validate(propertyValue, report);
                }
            }
        }
        return parentReport.addReport(schema, subject, report);
    }

    public static class PatternPropertyValidator {
        private final Pattern pattern;
        private final SchemaValidator validator;

        public PatternPropertyValidator(Pattern pattern, SchemaValidator validator) {
            this.pattern = pattern;
            this.validator = validator;
        }
    }
}

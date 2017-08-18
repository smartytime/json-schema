package io.sbsp.jsonschema.validation.keywords.object;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PatternPropertiesValidator extends KeywordValidator<SchemaMapKeyword> {

    private final List<PatternPropertyValidator> patternValidators;

    public PatternPropertiesValidator(SchemaMapKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.PATTERN_PROPERTIES, schema);
        this.patternValidators = keyword.getSchemas().entrySet().stream()
                .map(entry -> {
                    final Pattern pattern = Pattern.compile(entry.getKey());
                    final SchemaValidator validator = factory.createValidator(entry.getValue());
                    return new PatternPropertyValidator(pattern, validator);
                })
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport parentReport) {
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
                    final JsonValueWithPath propertyValue = subject.path(propertyName);
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

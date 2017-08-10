package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PatternPropertiesValidator extends KeywordValidator<SchemaMapKeyword> {

    @NonNull
    private final List<PatternPropertyValidator> patternValidators;

    @Builder
    public PatternPropertiesValidator(Schema schema, @Singular List<PatternPropertyValidator> patternValidators) {
        super(SchemaKeyword.patternProperties, schema);
        this.patternValidators = ImmutableList.copyOf(patternValidators);
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

    public static class PatternPropertiesValidatorBuilder {
        public PatternPropertiesValidatorBuilder addPatternValidator(String pattern, SchemaValidator validator) {
            return this.patternValidator(new PatternPropertyValidator(Pattern.compile(pattern), validator));
        }
    }
}

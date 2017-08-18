package io.sbsp.jsonschema.validation.keywords;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import lombok.NonNull;

import java.util.Set;
import java.util.regex.Pattern;

public class AdditionalPropertiesValidator extends KeywordValidator<SingleSchemaKeyword> {

    @NonNull
    private final SchemaValidator additionalPropertiesValidator;

    @NonNull
    private final Set<String> propertySchemaKeys;

    @NonNull
    private final Set<Pattern> patternProperties;

    public AdditionalPropertiesValidator(SingleSchemaKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.ADDITIONAL_PROPERTIES, schema);
        final Draft6Schema draft6Schema = schema.asDraft6();
        this.additionalPropertiesValidator = factory.createValidator(keyword.getSchema());
        this.patternProperties = draft6Schema.getPatternProperties().keySet().stream()
                .map(Pattern::compile)
                .collect(ImmutableSet.toImmutableSet());
        this.propertySchemaKeys = draft6Schema.getProperties().keySet();
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport parentReport) {
        ValidationReport report = parentReport.createChildReport();

        prop: for (String propName : subject.propertyNames()) {
            for (Pattern pattern : patternProperties) {
                if(pattern.matcher(propName).find()) {
                    continue prop;
                }
            }
            if (!propertySchemaKeys.contains(propName)) {
                JsonValueWithPath propertyValue = subject.path(propName);
                additionalPropertiesValidator.validate(propertyValue, report);
            }
        }
        if (!report.isValid()) {
            parentReport.addReport(schema, subject, Keywords.ADDITIONAL_PROPERTIES, "Additional properties were invalid", report);
        }
        return parentReport.isValid();
    }
}

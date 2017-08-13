package io.sbsp.jsonschema.validator.keywords;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.NonNull;

import java.util.Set;
import java.util.regex.Pattern;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_PROPERTIES;

public class AdditionalPropertiesValidator extends KeywordValidator<SingleSchemaKeyword> {

    @NonNull
    private final SchemaValidator additionalPropertiesValidator;

    @NonNull
    private final Set<String> propertySchemaKeys;

    @NonNull
    private final Set<Pattern> patternProperties;

    public AdditionalPropertiesValidator(SingleSchemaKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.additionalProperties, schema);
        final Draft6Schema draft6Schema = schema.asDraft6();
        this.additionalPropertiesValidator = factory.createValidator(keyword.getSchema());
        this.patternProperties = draft6Schema.getPatternProperties().keySet().stream()
                .map(Pattern::compile)
                .collect(ImmutableSet.toImmutableSet());
        this.propertySchemaKeys = draft6Schema.getProperties().keySet();
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        ValidationReport report = parentReport.createChildReport();

        prop: for (String propName : subject.propertyNames()) {
            for (Pattern pattern : patternProperties) {
                if(pattern.matcher(propName).find()) {
                    continue prop;
                }
            }
            if (!propertySchemaKeys.contains(propName)) {
                JsonValueWithLocation propertyValue = subject.getPathAwareObject(propName);
                additionalPropertiesValidator.validate(propertyValue, report);
            }
        }
        if (!report.isValid()) {
            parentReport.addReport(schema, subject, ADDITIONAL_PROPERTIES, "Additional properties were invalid", report);
        }
        return parentReport.isValid();
    }
}

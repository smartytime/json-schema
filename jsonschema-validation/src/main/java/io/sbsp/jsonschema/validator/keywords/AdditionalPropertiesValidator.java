package io.sbsp.jsonschema.validator.keywords;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import lombok.Builder;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_PROPERTIES;

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
        this.propertySchemaKeys = Collections.unmodifiableSet(new HashSet<>(propertySchemaKeys));
        this.patternProperties = ImmutableSet.copyOf(patternProperties);
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

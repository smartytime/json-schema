package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PROPERTIES;

@ToString
@EqualsAndHashCode(callSuper = true)
public class PropertySchemaValidator extends KeywordValidator {
    private final Map<String, SchemaValidator> propertyValidators;
    private final Set<String> validatedProperties;
    private final int propertyLength;
    private final Schema schema;

    public PropertySchemaValidator(ImmutableMap<String, SchemaValidator> propertyValidators, Schema schema) {
        super(PROPERTIES, schema);
        checkNotNull(propertyValidators);
        this.propertyValidators = Collections.unmodifiableMap(new HashMap<>(propertyValidators));
        this.validatedProperties = new HashSet<>(propertyValidators.keySet());
        this.propertyLength = this.validatedProperties.size();
        this.schema = checkNotNull(schema);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final Set<String> subjectProperties = subject.asJsonObject().keySet();

        final Set<String> a;
        final Set<String> b;
        boolean bSmaller = subjectProperties.size() < this.propertyLength;
        a = bSmaller ? subjectProperties : validatedProperties;
        b = bSmaller ? validatedProperties : subjectProperties;

        for (String property : a) {
            if (!b.contains(property)) {
                continue;
            }
            SchemaValidator propValidator = propertyValidators.get(property);
            JsonValueWithLocation pathAwareSubject = subject.getPathAwareObject(property);
            propValidator.validate(pathAwareSubject, report);
        }

        return report.isValid();
    }

}

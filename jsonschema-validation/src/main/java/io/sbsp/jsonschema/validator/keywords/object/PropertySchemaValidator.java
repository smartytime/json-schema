package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
@EqualsAndHashCode(callSuper = true)
public class PropertySchemaValidator extends KeywordValidator<SchemaMapKeyword> {
    private final Map<String, SchemaValidator> propertyValidators;
    private final Set<String> validatedProperties;
    private final int propertyLength;
    private final Schema schema;

    public PropertySchemaValidator(SchemaMapKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.properties, schema);

        this.propertyValidators = keyword.getSchemas().entrySet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(
                        e -> e.getKey(),
                        e -> factory.createValidator(e.getValue())));

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

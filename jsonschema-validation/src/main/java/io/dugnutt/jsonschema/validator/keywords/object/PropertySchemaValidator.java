package io.dugnutt.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.*;

@ToString
@EqualsAndHashCode
public class PropertySchemaValidator extends KeywordValidator {
    private final ImmutableMap<String, SchemaValidator> propertyValidators;
    private final Schema schema;

    public PropertySchemaValidator(ImmutableMap<String, SchemaValidator> propertyValidators, Schema schema) {
        super(PROPERTIES, schema);
        this.propertyValidators = checkNotNull(propertyValidators);
        this.schema = checkNotNull(schema);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        final Set<String> subjectProperties = subject.asJsonObject().keySet();
        final Set<String> schemaNames = propertyValidators.keySet();
        for (String property : Sets.intersection(subjectProperties, schemaNames)) {
            SchemaValidator propValidator = propertyValidators.get(property);
            JsonValueWithLocation pathAwareSubject = subject.getPathAwareObject(property);
            propValidator.validate(pathAwareSubject, report);
        }
        return report.isValid();
    }

    @Override
    public Schema getSchema() {
        return schema;
    }
}

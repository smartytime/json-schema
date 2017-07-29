package io.dugnutt.jsonschema.validator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.extractors.KeywordValidatorExtractor;
import io.dugnutt.jsonschema.validator.extractors.KeywordValidators;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import io.dugnutt.jsonschema.validator.keywords.TypeValidator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.annotation.Nullable;
import javax.json.JsonValue.ValueType;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main validation processor... wraps keyword validators and provides them all a gateway to process through.
 */
public class JsonSchemaValidator implements SchemaValidator {
    @NonNull
    private final Multimap<ValueType, KeywordValidator> childValidators;

    @Nullable
    private final TypeValidator typeValidator;

    @NonNull
    private final Schema schema;

    private final boolean noop;

    @Builder(builderMethodName = "jsonSchemaValidator")
    public JsonSchemaValidator(@Singular List<KeywordValidatorExtractor> factories, Schema schema, SchemaValidatorFactory validatorFactory) {
        checkNotNull(factories, "factories must not be null");
        this.schema = schema;

        if (schema.getTypes().size() > 0) {
            typeValidator = TypeValidator.builder()
                    .schema(schema)
                    .requiredTypes(schema.getTypes())
                    .build();
        } else {
            typeValidator = null;
        }

        ImmutableMultimap.Builder<ValueType, KeywordValidator> validatorsByType = ImmutableMultimap.builder();
        validatorFactory.cacheValidator(schema.getAbsoluteURI(), this);
        for (KeywordValidatorExtractor factory : factories) {
            if (factory.appliesToSchema(schema)) {
                KeywordValidators keywordValidators = factory.getKeywordValidators(schema, validatorFactory);
                for (ValueType valueType : factory.getApplicableTypes()) {
                    keywordValidators.forEach(validator -> validatorsByType.put(valueType, validator));
                }
            }
        }

        this.childValidators = validatorsByType.build();
        this.noop = this.childValidators.isEmpty() && this.typeValidator == null;
    }

    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        if (noop) {
            return true;
        }

        ValidationReport childReport = parentReport.createChildReport();

        if (typeValidator != null) {
            typeValidator.validate(subject, childReport);
        }

        for (SchemaValidator validator : getApplicableValidators(subject)) {
            validator.validate(subject, childReport);
        }

        if (!childReport.isValid()) {
            parentReport.addReport(schema, subject, childReport);
        }
        return childReport.isValid();
    }

    public Collection<KeywordValidator> getApplicableValidators(JsonValueWithLocation subject) {
        return childValidators.get(subject.getValueType());
    }

    @VisibleForTesting
    public Schema getSchema() {
        return schema;
    }

    public static class JsonSchemaValidatorBuilder {
        public JsonSchemaValidator build() {
            return new JsonSchemaValidator(this.factories, this.schema, this.validatorFactory);
        }
    }
}

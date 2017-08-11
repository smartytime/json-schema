package io.sbsp.jsonschema.validator;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.validator.factory.KeywordToValidatorTransformer;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonValue.ValueType;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main validation processing controller.  There will be a single instance of this class for each json-schema, but each
 * keyword is broken out into a separate processor.
 */
public class JsonSchemaValidator implements SchemaValidator {
    @NonNull
    private final List<KeywordValidator> arrayValidators;
    private final List<KeywordValidator> objectValidators;
    private final List<KeywordValidator> numberValidators;
    private final List<KeywordValidator> stringValidators;
    private final List<KeywordValidator> nullValidators;
    private final List<KeywordValidator> trueValidators;
    private final List<KeywordValidator> falseValidators;

    /**
     * The underlying schema being validated.  This instance isn't actually used for validation, it's primarily
     * here for metadata when recording errors.
     */
    @NonNull
    private final Schema schema;

    /**
     * Whether this validator has any validation to perform.
     */
    private final boolean noop;

    @Builder(builderMethodName = "jsonSchemaValidator")
    public JsonSchemaValidator(SetMultimap<KeywordMetadata, KeywordToValidatorTransformer> factories, Schema schema, SchemaValidatorFactory validatorFactory) {
        checkNotNull(factories, "factories must not be null");
        this.schema = schema;

        // Cache the validator to avoid infinite recursion
        validatorFactory.cacheValidator(schema.getAbsoluteURI(), this);

        final ListMultimap<ValueType, KeywordValidator> validators = mapValidatorsToType(schema, validatorFactory, factories);
        this.arrayValidators = validators.get(ValueType.ARRAY);
        this.objectValidators = validators.get(ValueType.OBJECT);
        this.numberValidators = validators.get(ValueType.NUMBER);
        this.stringValidators = validators.get(ValueType.STRING);
        this.trueValidators = validators.get(ValueType.TRUE);
        this.falseValidators = validators.get(ValueType.FALSE);
        this.nullValidators = validators.get(ValueType.NULL);

        this.noop = validators.isEmpty();
    }

    /**
     * Executes this validator for the provided {@code subject} and appends any errors to the provided {@code parentReport}
     * @param subject The JsonValue to be validated against this schema
     * @param parentReport The report to append any errors to
     * @return true if the {@code subject} passed validation
     */
    public boolean validate(JsonValueWithLocation subject, ValidationReport parentReport) {
        if (noop) {
            return true;
        }

        ValidationReport childReport = null;

        final List<KeywordValidator> applicableValidators = findValidators(subject);
        if (applicableValidators != null) {
            childReport = parentReport.createChildReport();
            final int size = applicableValidators.size();
            for (int i = 0; i < size; i++) {
                applicableValidators.get(i).validate(subject, childReport);
            }
        }

        if (childReport != null && !childReport.isValid()) {
            parentReport.addReport(schema, subject, childReport);
        }
        return childReport == null || childReport.isValid();
    }

    public Schema getSchema() {
        return schema;
    }

    /**
     * Finds all {@link KeywordValidator} that are applicable for the given subject. Certain keywords are only applicable
     * if the subject is of a specific type.
     *
     * @param subject The instance to be validated.
     * @return A list of {@link KeywordValidator} that apply to the given subject.
     */
    @Nullable
    List<KeywordValidator> findValidators(JsonValueWithLocation subject) {
        final List<KeywordValidator> validators;
        switch (subject.getValueType()) {
            case ARRAY:
                validators = arrayValidators;
                break;
            case OBJECT:
                validators = objectValidators;
                break;
            case STRING:
                validators = stringValidators;
                break;
            case NUMBER:
                validators = numberValidators;
                break;
            case TRUE:
                validators = trueValidators;
                break;
            case FALSE:
                validators = falseValidators;
                break;
            case NULL:
                validators = nullValidators;
                break;
            default:
                validators = null;
        }
        return validators;
    }

    /**
     * Internal helper method that sorts out keyword validators based on their applicable type.
     *
     * @param schema The schema that is to be validated
     * @param validatorFactory A validatorFactory used to construct new {@link KeywordValidator} instances
     * @param factories A list of {@link KeywordToValidatorTransformer} - these inspect the provided schema, and return the
     *                  necessary keyword validators based on the schema.
     *
     * @return A {@link ListMultimap} with the keywords sorted by their applicable types.
     */
    @SuppressWarnings("unchecked")
    private static ListMultimap<ValueType, KeywordValidator> mapValidatorsToType(Schema schema, SchemaValidatorFactory validatorFactory,
                                                                                 SetMultimap<KeywordMetadata, KeywordToValidatorTransformer> factories) {
        ImmutableListMultimap.Builder<ValueType, KeywordValidator> validatorsByType = ImmutableListMultimap.builder();
        schema.getKeywords().forEach((keyword, keywordValue)->{
            factories.get(keyword).forEach(tx-> {
                final KeywordValidator keywordValidator = tx.getKeywordValidator(schema, keyword, keywordValue, validatorFactory);
                if (keywordValidator != null) {
                    if (keyword.getApplicableTypes().isEmpty()) {
                        for (ValueType applicableType : ValueType.values()) {
                            validatorsByType.put(applicableType, keywordValidator);
                        }
                    } else {
                        keyword.getApplicableTypes().forEach(applicableType-> {
                            validatorsByType.put(applicableType, keywordValidator);
                        });
                    }

                }
            });
        });

        return validatorsByType.build();
    }

    public static class JsonSchemaValidatorBuilder {
        public JsonSchemaValidator build() {
            return new JsonSchemaValidator(this.factories, this.schema, this.validatorFactory);
        }
    }
}

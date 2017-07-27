package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.ObjectComparator;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StreamUtils;
import io.dugnutt.jsonschema.validator.ChainedValidator.ChainedValidatorBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static io.dugnutt.jsonschema.loader.SchemaLoadingContext.COMBINED_SCHEMA_KEYWORDS;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class BaseValidatorFactory implements PartialValidatorFactory {

    private static final BaseValidatorFactory INSTANCE = new BaseValidatorFactory();

    private BaseValidatorFactory() {
    }

    @Override
    public SchemaValidator forSchema(Schema schema, SchemaValidatorFactory factory) {
        ChainedValidatorBuilder validationBuilder = ChainedValidator.builder()
                .factory(factory)
                .schema(schema);

        // ########################################
        // ENUM
        // ########################################
        schema.getEnumValues().ifPresent(enumValues-> {
            validationBuilder.addValidator(ENUM, ((subject, report) -> {
                boolean foundMatch = enumValues.stream()
                        .anyMatch(val -> ObjectComparator.lexicalEquivalent(val, subject.getWrapped()));
                if (!foundMatch) {
                    return report.addError(buildKeywordFailure(subject, schema, ENUM)
                            .message("%s does not match the enum values", subject)
                            .model(enumValues)
                            .build());
                }
                return true;
            }));
        });

        // ########################################
        // NOT
        // ########################################
        schema.getNotSchema().ifPresent(notSchema -> {
            SchemaValidator notValidator = factory.createValidator(notSchema);
            validationBuilder.addValidator(NOT, (subject, report) -> {
                if (notValidator.validate(subject, report)) {
                    return report.addError(buildKeywordFailure(subject, schema, NOT)
                            .message("subject must not be valid against schema", notSchema)
                            .build());
                }
                return true;
            });
        });

        // ########################################
        // CONST
        // ########################################
        schema.getConstValue().ifPresent(constValue -> {
            validationBuilder.addValidator(CONST, (subject, report)-> {
                if (!constValue.equals(subject)) {
                    return report.addError(buildKeywordFailure(subject, schema, CONST)
                            .message("%s does not match the const value", subject)
                            .build());
                }
                return true;
            });
        });

        // ########################################
        // ALL OF
        // ########################################
        if (!schema.getAllOfSchemas().isEmpty()) {
            List<SchemaValidator> allOfValidators = schema.getAllOfSchemas().stream()
                    .map(factory::createValidator)
                    .collect(StreamUtils.toImmutableList());
            validationBuilder.addValidator(ALL_OF, (subject, report) ->
                    validateSubschemas(ALL_OF, subject, schema, report, allOfValidators));

        }

        // ########################################
        // ANY OF
        // ########################################
        if (!schema.getAnyOfSchemas().isEmpty()) {
            List<SchemaValidator> anyOfValidators = schema.getAnyOfSchemas().stream()
                    .map(factory::createValidator)
                    .collect(StreamUtils.toImmutableList());
            validationBuilder.addValidator(ANY_OF, (subject, parentReport) -> {
                ValidationReport report = new ValidationReport();
                for (SchemaValidator anyOfValidator : anyOfValidators) {
                    if (anyOfValidator.validate(subject, report)) {
                        return true;
                    }
                }
                return parentReport.addError(buildKeywordFailure(subject, schema, ANY_OF)
                        .message("no subschema matched out of the total %d subschemas", anyOfValidators.size())
                        .causingExceptions(report.getErrors())
                        .build());

            });
        }
        
        // ########################################
        // ONE OF
        // ########################################
        if (!schema.getOneOfSchemas().isEmpty()) {
            List<SchemaValidator> oneOfValidators = schema.getOneOfSchemas().stream()
                    .map(factory::createValidator)
                    .collect(StreamUtils.toImmutableList());
            validationBuilder.addValidator(ONE_OF, (subject, report) ->
                    validateSubschemas(ONE_OF, subject, schema, report, oneOfValidators));

        }
        return validationBuilder.build();
    }

    private boolean validateSubschemas(JsonSchemaKeyword combinedKeyword, PathAwareJsonValue subject, Schema parentSchema, ValidationReport parentReport, List<SchemaValidator> subschemaValidators) {
        checkArgument(COMBINED_SCHEMA_KEYWORDS.contains(combinedKeyword), "Should contain this item");
        ValidationReport report = new ValidationReport();
        for (SchemaValidator validator : subschemaValidators) {
            validator.validate(subject, report);
        }

        List<ValidationError> failures = report.getErrors();

        int matchingCount = subschemaValidators.size() - failures.size();
        int subschemaCount = subschemaValidators.size();

        switch (combinedKeyword) {
            case ALL_OF:
                if (matchingCount < subschemaCount) {
                    return parentReport.addError(buildKeywordFailure(subject, parentSchema, ALL_OF)
                            .message("only %d subschema matches out of %d", matchingCount, subschemaCount)
                            .causingExceptions(failures)
                            .build());
                }
                break;
            case ONE_OF:
                if (matchingCount != 1) {
                    return parentReport.addError(buildKeywordFailure(subject, parentSchema, ONE_OF)
                            .message("%d subschemas matched instead of one", matchingCount)
                            .causingExceptions(failures)
                            .build());
                }
                break;
        }
        return true;
    }

    public Optional<ValidationError> validate(PathAwareJsonValue subject, Schema schema, SchemaValidatorFactory factory) {

        // Do all the core validations
        List<ValidationError> allErrors = new ArrayList<>();

        return ValidationError.collectErrors(schema, subject.getPath(), allErrors);
    }



    public static BaseValidatorFactory baseSchemaValidator() {
        return INSTANCE;
    }
}

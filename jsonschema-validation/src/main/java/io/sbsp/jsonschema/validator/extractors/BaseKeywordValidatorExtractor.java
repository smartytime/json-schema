package io.sbsp.jsonschema.validator.extractors;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.utils.StreamUtils;
import io.sbsp.jsonschema.validator.extractors.KeywordValidators.KeywordValidatorsBuilder;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.keywords.AllOfValidator;
import io.sbsp.jsonschema.validator.keywords.AnyOfValidator;
import io.sbsp.jsonschema.validator.keywords.ConstValidator;
import io.sbsp.jsonschema.validator.keywords.EnumValidator;
import io.sbsp.jsonschema.validator.keywords.NotKeywordValidator;
import io.sbsp.jsonschema.validator.keywords.OneOfValidator;

import java.util.List;

public class BaseKeywordValidatorExtractor implements KeywordValidatorExtractor {

    private BaseKeywordValidatorExtractor() {
    }

    @Override
    public KeywordValidators getKeywordValidators(Schema schema, SchemaValidatorFactory factory) {
        KeywordValidatorsBuilder validationBuilder = KeywordValidators.builder()
                .validatorFactory(factory)
                .schema(schema);

        // ########################################
        // ENUM
        // ########################################
        schema.getEnumValues().ifPresent(enumValues -> {
            validationBuilder.addValidator(EnumValidator.builder()
                    .schema(schema)
                    .enumValues(enumValues)
                    .build());
        });

        // ########################################
        // NOT
        // ########################################
        schema.getNotSchema().ifPresent(notSchema -> {
            SchemaValidator notValidator = factory.createValidator(notSchema);
            validationBuilder.addValidator(NotKeywordValidator.builder()
                    .schema(schema)
                    .notSchema(notSchema)
                    .notValidator(notValidator)
                    .build());
        });

        // ########################################
        // CONST
        // ########################################
        schema.getConstValue().ifPresent(constValue -> {
            validationBuilder.addValidator(ConstValidator.builder()
                    .parentSchema(schema)
                    .constValue(constValue)
                    .build());
        });

        // ########################################
        // ALL OF
        // ########################################
        if (!schema.getAllOfSchemas().isEmpty()) {
            List<SchemaValidator> allOfValidators = schema.getAllOfSchemas().stream()
                    .map(factory::createValidator)
                    .collect(StreamUtils.toImmutableList());

            validationBuilder.addValidator(
                    AllOfValidator.builder()
                            .schema(schema)
                            .allOfValidators(allOfValidators)
                            .build());
        }

        // ########################################
        // ANY OF
        // ########################################
        if (!schema.getAnyOfSchemas().isEmpty()) {
            List<SchemaValidator> anyOfValidators = schema.getAnyOfSchemas().stream()
                    .map(factory::createValidator)
                    .collect(StreamUtils.toImmutableList());
            validationBuilder.addValidator(
                    AnyOfValidator.builder()
                            .anyOfValidators(anyOfValidators)
                            .schema(schema)
                            .build());
        }

        // ########################################
        // ONE OF
        // ########################################
        if (!schema.getOneOfSchemas().isEmpty()) {
            List<SchemaValidator> oneOfValidators = schema.getOneOfSchemas().stream()
                    .map(factory::createValidator)
                    .collect(StreamUtils.toImmutableList());

            validationBuilder.addValidator(
                    OneOfValidator.builder()
                            .schema(schema)
                            .oneOfValidators(oneOfValidators)
                            .build());
        }
        return validationBuilder.build();
    }

    public static BaseKeywordValidatorExtractor baseSchemaValidator() {
        return new BaseKeywordValidatorExtractor();
    }
}

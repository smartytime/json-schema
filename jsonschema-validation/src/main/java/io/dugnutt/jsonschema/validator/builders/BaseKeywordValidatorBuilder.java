package io.dugnutt.jsonschema.validator.builders;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StreamUtils;
import io.dugnutt.jsonschema.validator.builders.KeywordValidators.KeywordValidatorsBuilder;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import io.dugnutt.jsonschema.validator.keywords.AnyOfValidator;
import io.dugnutt.jsonschema.validator.keywords.CombinedKeywordValidator;
import io.dugnutt.jsonschema.validator.keywords.ConstValidator;
import io.dugnutt.jsonschema.validator.keywords.EnumValidator;
import io.dugnutt.jsonschema.validator.keywords.NotKeywordValidator;

import java.util.List;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;

public class BaseKeywordValidatorBuilder implements KeywordValidatorBuilder {

    private BaseKeywordValidatorBuilder() {
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
                    CombinedKeywordValidator.builder()
                            .combinedKeyword(ALL_OF)
                            .parentSchema(schema)
                            .subschemaValidators(allOfValidators)
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
                    CombinedKeywordValidator.builder()
                            .combinedKeyword(ONE_OF)
                            .parentSchema(schema)
                            .subschemaValidators(oneOfValidators)
                            .build());
        }
        return validationBuilder.build();
    }

    public static BaseKeywordValidatorBuilder baseSchemaValidator() {
        return new BaseKeywordValidatorBuilder();
    }
}

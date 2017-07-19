package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.MultipleTypeSchema;
import io.dugnutt.jsonschema.six.Schema;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class MultipleTypeSchemaFactory {
    private static final Set<JsonSchemaKeyword> TYPE_SPECIFIC_KEYWORDS = Arrays.stream(JsonSchemaKeyword.values())
            .filter(k -> k.hasSpecificType())
            .collect(Collectors.toSet());

    private final SchemaLoadingContext schemaModel;
    private final JsonSchemaFactory schemaFactory;

    public MultipleTypeSchemaFactory(SchemaLoadingContext schemaModel, JsonSchemaFactory schemaFactory) {
        this.schemaModel = checkNotNull(schemaModel);
        this.schemaFactory = checkNotNull(schemaFactory);
    }

    public static Schema.Builder createExplicitTypeBuilder(SchemaLoadingContext schemaModel, JsonSchemaFactory schemaFactory) {
        return new MultipleTypeSchemaFactory(schemaModel, schemaFactory).createExplicitTypeBuilder();
    }

    public static Schema.Builder createSchemaBuilderFromProperties(SchemaLoadingContext schemaModel, JsonSchemaFactory schemaFactory) {
        return new MultipleTypeSchemaFactory(schemaModel, schemaFactory).createSchemaBuilderFromProperties();
    }

    public Schema.Builder createExplicitTypeBuilder() {
        MultipleTypeSchema.Builder builder = new MultipleTypeSchema.Builder(schemaModel.getLocation());
        Set<JsonSchemaType> typeArray = schemaModel.getTypeArray();
        return createSchemaBuilder(typeArray, true);
    }

    public Schema.Builder createSchemaBuilderFromProperties() {
        //This set represents all the types that have keywords present in this
        //specific schema.  We'll load up an individual schema for each type.
        Set<JsonSchemaType> typesWithKeywords = TYPE_SPECIFIC_KEYWORDS.stream()
                .filter(schemaModel::has)
                .map(JsonSchemaKeyword::getAppliesToType)
                .distinct()
                .collect(Collectors.toSet());

        return createSchemaBuilder(typesWithKeywords, false);
    }

    private Schema.Builder createSchemaBuilder(Set<JsonSchemaType> types, boolean explicitlyDeclared) {
        MultipleTypeSchema.Builder builder = new MultipleTypeSchema.Builder(schemaModel.getLocation());

        if (types.size() > 1) {
            types.forEach(schemaType -> {
                builder.addPossibleSchema(schemaType,
                        schemaFactory.createBuilderForSchemaType(schemaModel, schemaType, explicitlyDeclared)
                                .build());
            });
        } else if (types.size() == 1) {
            JsonSchemaType schemaType = types.iterator().next();
            return schemaFactory.createBuilderForSchemaType(schemaModel, schemaType, explicitlyDeclared);
        } else {
            return EmptySchema.builder(schemaModel.getLocation());
        }

        return builder;
    }
}

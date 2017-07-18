package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.NumberSchema;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MULTIPLE_OF;

public class NumberSchemaFactory {

    private final SchemaLoaderModel schemaModel;

    public NumberSchemaFactory(SchemaLoaderModel schemaModel) {
        this.schemaModel = checkNotNull(schemaModel);
    }

    public static NumberSchema.Builder createNumberSchemaBuilder(SchemaLoaderModel schemaLoaderModel) {
        return new NumberSchemaFactory(schemaLoaderModel).createNumberSchemaBuilder();
    }

    public NumberSchema.Builder createNumberSchemaBuilder() {
        NumberSchema.Builder builder = NumberSchema.builder();
        final FluentJsonObject schemaJson = schemaModel.schemaJson;
        schemaJson.findNumber(MINIMUM).ifPresent(builder::minimum);
        schemaJson.findNumber(MAXIMUM).ifPresent(builder::maximum);
        schemaJson.findNumber(MULTIPLE_OF).ifPresent(builder::multipleOf);
        schemaJson.findNumber(EXCLUSIVE_MINIMUM).ifPresent(builder::exclusiveMinimum);
        schemaJson.findNumber(EXCLUSIVE_MAXIMUM).ifPresent(builder::exclusiveMaximum);
        return builder;
    }
}

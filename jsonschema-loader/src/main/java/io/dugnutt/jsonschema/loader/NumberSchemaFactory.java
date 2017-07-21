package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.NumberSchema;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MULTIPLE_OF;

public class NumberSchemaFactory {

    private final SchemaLoadingContext schemaModel;

    public NumberSchemaFactory(SchemaLoadingContext schemaModel) {
        this.schemaModel = checkNotNull(schemaModel);
    }

    public static NumberSchema.Builder createNumberSchemaBuilder(SchemaLoadingContext schemaLoadingContext) {
        return new NumberSchemaFactory(schemaLoadingContext).createNumberSchemaBuilder();
    }

    public NumberSchema.Builder createNumberSchemaBuilder() {
        NumberSchema.Builder builder = NumberSchema.builder();
        builder.location(schemaModel.getLocation());
        final PathAwareJsonValue schemaJson = schemaModel.schemaJson;
        schemaJson.findNumber(MINIMUM).ifPresent(builder::minimum);
        schemaJson.findNumber(MAXIMUM).ifPresent(builder::maximum);
        schemaJson.findNumber(MULTIPLE_OF).ifPresent(builder::multipleOf);
        schemaJson.findNumber(EXCLUSIVE_MINIMUM).ifPresent(builder::exclusiveMinimum);
        schemaJson.findNumber(EXCLUSIVE_MAXIMUM).ifPresent(builder::exclusiveMaximum);
        return builder;
    }
}

package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.StringSchema;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
public class StringSchemaFactory {

    private SchemaLoadingContext schemaModel;

    private StringSchemaFactory(SchemaLoadingContext schemaModel) {
        this.schemaModel = requireNonNull(schemaModel, "ls cannot be null");
    }

    public static StringSchema.Builder createStringSchemaBuilder(SchemaLoadingContext schemaLoadingContext) {
        return new StringSchemaFactory(schemaLoadingContext).createStringSchemaBuilder();
    }

    public StringSchema.Builder createStringSchemaBuilder() {
        StringSchema.Builder builder = StringSchema.builder(schemaModel.getLocation());
        schemaModel.schemaJson.findInt(JsonSchemaKeyword.MIN_LENGTH).ifPresent(builder::minLength);
        schemaModel.schemaJson.findInt(JsonSchemaKeyword.MAX_LENGTH).ifPresent(builder::maxLength);
        schemaModel.schemaJson.findString(JsonSchemaKeyword.PATTERN).ifPresent(builder::pattern);
        schemaModel.schemaJson.findString(JsonSchemaKeyword.FORMAT).ifPresent(builder::format);
        return builder;
    }
}

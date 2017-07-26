package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import io.dugnutt.jsonschema.six.PathAwareJsonValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MULTIPLE_OF;

public class NumberKeywordsFactoryHelper {

    public static void appendNumberKeywords(PathAwareJsonValue schemaJson, JsonSchemaBuilder schemaBuilder) {
        checkNotNull(schemaBuilder, "schemaBuilder must not be null");
        checkNotNull(schemaJson, "schemaJson must not be null");

        schemaJson.findNumber(MINIMUM).ifPresent(schemaBuilder::minimum);
        schemaJson.findNumber(MAXIMUM).ifPresent(schemaBuilder::maximum);
        schemaJson.findNumber(MULTIPLE_OF).ifPresent(schemaBuilder::multipleOf);
        schemaJson.findNumber(EXCLUSIVE_MINIMUM).ifPresent(schemaBuilder::exclusiveMinimum);
        schemaJson.findNumber(EXCLUSIVE_MAXIMUM).ifPresent(schemaBuilder::exclusiveMaximum);
    }
}
